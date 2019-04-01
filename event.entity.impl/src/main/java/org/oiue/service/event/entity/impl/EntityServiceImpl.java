package org.oiue.service.event.entity.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.entity.EntityService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.odp.structure.api.IServicesEvent;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringReplace;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityServiceImpl implements EntityService {
	private static final long serialVersionUID = 1L;
	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static CacheServiceManager cache;
	protected static FactoryService factoryService;
	private static String data_source_name = null;
	
	public void userDefinedEntity(Map data, Map event, String tokenid) throws Throwable {
		String processKey = MapUtil.getString(data, "processKey",UUID.randomUUID().toString().replaceAll("-", ""));
		try {
			data.put("processKey", processKey);
			createEntity(data, event, tokenid);
			insertEntity(data, event, tokenid);
			IServicesEvent se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
			data.put("addOperation", MapUtil.getBoolean(data, "addOperation",true));
			se.insertServiceEvent(data);
			factoryService.CommitByProcess(processKey);
		} catch (Throwable e) {
			factoryService.RollbackByProcess(processKey);
			throw e;
		}
	}

	public void createEntity(Map data, Map event, String tokenid) {
		IResource iresource;
		List<Map> fields;
		fields = (List) data.get("fields");
		
		String user_id = MapUtil.getString(data, "user_id");
		Long update_time = MapUtil.getLong(data, "update_time",System.currentTimeMillis()/1000); 
		String processKey = MapUtil.getString(data, "processKey");
		data.put("update_time", update_time);
		try {//"entity_class_id","entity_id","table_catalog","table_schema","table_name","remark","short_code","status","islevel","sort","type","update_user_id","update_time","data_source_id","user_id","create_time"
			String n_table_name = MapUtil.getString(data, "table_name","t_" + UUID.randomUUID().toString().replace("-", ""));
			String n_table_catalog = MapUtil.getString(data, "table_catalog","ltmap");
			String n_table_schema = MapUtil.getString(data, "table_schema","public");
			
			if( MapUtil.getBoolean(data, "add_system_column",true)) {
				Map field =new HashMap();
				String n_field_name = MapUtil.getString(field, "column_name","f_" + UUID.randomUUID().toString().replace("-", ""));
				field.put("entity_column_id", n_field_name);
				field.put("column_name", _system_colnum);
				field.put("name", _system_colnum);
				field.put("column_desc", "系统ID");
				field.put("null_able", 0);
				field.put("status", 1);
				field.put("precision",36);
				field.put("scale", 0);
				field.put("type", "postgres_character_varying");
				field.put("user_id", user_id);
				field.put("ispk", true);
				field.put("update_time", update_time);
				fields.add(0, field);
			}
			
			data.put("entity_class_id",  MapUtil.getString(data, "entity_class_id","Sm@rtMapX_system"));
			data.put("entity_id", n_table_name);
			data.put("table_name", n_table_name);
			data.put("table_catalog", n_table_catalog);
			data.put("table_schema", n_table_schema);
			data.put("table_type", MapUtil.getString(data, "table_type","user"));
			data.put("data_source_id", MapUtil.getString(data, "data_source_id","fm_data_source_postgresql"));
			data.put("remark", MapUtil.getString(data, "remark"));
			data.put("short_code", MapUtil.getString(data, "short_code",null));
			data.put("islevel", MapUtil.getInt(data, "islevel",0));
			String entity_desc = MapUtil.getString(data, "entity_desc");
			
			StringBuilder ctsb = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(n_table_schema).append(".").append(n_table_name).append(" ( "); 
			StringBuilder ctcsb = new StringBuilder("COMMENT ON TABLE ").append(n_table_schema).append(".").append(n_table_name).append(" IS '").append(entity_desc).append("';");
			int sort = 0;
			for (Map field : fields) {//"entity_id","entity_column_id","column_name","remark","status","sort","encrypt_type","update_user_id","update_time"
				String n_field_name = MapUtil.getString(field, "column_name","f_" + UUID.randomUUID().toString().replace("-", ""));
				field.put("entity_id", n_table_name);
				field.put("entity_column_id", n_field_name);
				field.put("column_name", n_field_name);
				field.put("null_able", MapUtil.getInt(field, "null_able",1));
				field.put("status", MapUtil.getInt(field, "status",1));
				field.put("precision", MapUtil.getInt(field, "precision",0));
				field.put("scale", MapUtil.getInt(field, "scale",0));
				field.put("type", MapUtil.getString(field, "type","postgres_character_varying"));
				field.put("user_id", user_id);
				field.put("update_time", update_time);
				field.put("sort", sort++);
				field.put("encrypt_type", MapUtil.getInt(field, "encrypt_type",0));
				ctsb.append(convert2Column(field)).append(",");
				String comments = MapUtil.getString(field, "column_desc");
				if(comments!=null&&comments.length()!=0)
				ctcsb.append("COMMENT ON COLUMN ").append(n_table_schema).append(".").append(n_table_name).append(".").append(n_field_name).append(" is '").append(comments).append("';");
			}
			if( MapUtil.getBoolean(data, "add_system_column",true)) {
				ctsb.append("CONSTRAINT ").append(n_table_name).append("_pkey PRIMARY KEY (").append(_system_colnum).append(")");
			}else
			ctsb.deleteCharAt(ctsb.length()-1);
			ctsb.append(");");
			ctsb.append(ctcsb);
			logger.debug("sql:{}",ctsb );
			List<Map> events = null;
			String eventsstr = MapUtil.getString(event, "events");
			if (!StringUtil.isEmptys(eventsstr)) {
				events = (List<Map>) JSONUtil.parserStrToList(eventsstr, false);
			}
			events.get(0).put("content", ctsb.toString());
			event.put("EVENTS", JSONUtil.parserToStr(events));
			event.put("EVENT_TYPE", "update");
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			iresource.executeEvent(event, data_source_name, data,null);// insert entity
		} catch (OIUEException e) {
			throw e;
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, data, e);
		}
	}
	
	private String convert2Column(Map field) {
		StringBuilder csb = new StringBuilder(MapUtil.getString(field, "column_name"));
		csb.append(" ");
		String type = MapUtil.getString(field, "type");
		int length = MapUtil.getInt(field, "precision",0);
		String default_v = "";
		switch (type) {
		case "postgres_integer":
			csb.append("int4 ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::int4 ":"";
			break;
			
		case "postgres_bigint":
			csb.append("int8 ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::int8 ":"";
			break;
			
		case "postgres_double_precision":
			csb.append("float8 ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::float8 ":"";
			break;
			
		case "postgres_character_varying":
			if(length==0) {
				csb.append("varchar ");
			}else
				csb.append("varchar(").append(length).append(") ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::character varying ":"";
			break;
			
		case "postgres_date":
			csb.append("date ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::date ":"";
			break;
			
		case "postgres_timestamp":
			csb.append("timestamp ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::timestamp ":"";
			break;
			
		case "postgres_inet":
			csb.append("inet ");
			default_v=MapUtil.get(field, "default")!=null?"default '"+MapUtil.get(field, "default")+"'::inet ":"";
			break;
			
		case "postgres_point":
			csb.append("geometry(Point,4326) ");
			break;
			
		case "postgres_line":
			csb.append("geometry(LineString,4326) ");
			break;
			
		case "postgres_polygon":
			csb.append("geometry(Polygon,4326) ");
			break;
		}

		csb.append( MapUtil.getInt(field, "null_able",1)==1?"":"not null ");
		csb.append( default_v);
		
		return csb.toString();
	}
	
	public void insertEntity(Map data, Map event, String tokenid) {
		IResource iresource;

		List<Map> fields;
		fields = (List) data.get("fields");
		
		String processKey = MapUtil.getString(data, "processKey");
		try {//entity_class_id,entity_id,table_catalog,table_schema,table_name,remark,short_code,islevel,entity_class_id,table_type,user_id,data_source_id,user_id
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			iresource.callEvent("b046a610-48c7-46da-a472-2eed60f0f026", data_source_name, data);// insert entity
			for (Map field : fields) {//"entity_id","entity_column_id","column_name","remark","status","sort","encrypt_type","update_user_id","update_time"
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey); // entity_id,entity_column_id,column_name,remark,sort,encrypt_type,user_id
				iresource.callEvent("a6f0ad85-1c18-4f2f-a6f4-a83b7ac8cb52", data_source_name, field);// insert entity column
			}
		} catch (OIUEException e) {
			throw e;
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, data, e);
		}
	}

	@Override
	public void loadEntity(Map data, Map event, String tokenid) {
		
	}

	@Override
	public Object convertToGeometry(Map data, Map event, String tokenid) throws SQLException {
		String type = MapUtil.getString(data, "operation_type");
		IResource iresource;
		IServicesEvent se;
		int geo_type = 100;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");
		try {
			switch (type) {
			case "changeToGeometry":
				try {
					geo_type = MapUtil.getInt(data, "geo_type");
				} catch (Throwable e) {
					data.put("geo_type", geo_type);
				}
				iresource = factoryService.getBmo(IResource.class.getName());
				Object ro = iresource.callEvent("c901e524-4c44-4525-ab87-5ef0328261bb", data_source_name, data);
				if (ro instanceof Map) {
					int status = MapUtil.getInt((Map<String, Object>) ro, "status");
					if (status < StatusResult._ncriticalAbnormal) {
						return ro;
					}
				}
				se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
				se.updateServiceEvent(data);
				factoryService.CommitByProcess(processKey);
				return ro;

			case "createGeometry":
				try {
					geo_type = MapUtil.getInt(data, "geo_type");
				} catch (Throwable e) {
					data.put("geo_type", geo_type);
				}
				if (geo_type != 10) {
					throw new RuntimeException("目前暂只支持转点空间！");
				}
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
				Object roa = iresource.callEvent("bc13c8d3-29af-4420-b5bb-00f007a7ccd5", data_source_name, data);
				se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
				se.updateServiceEvent(data);
				factoryService.CommitByProcess(processKey);
				return roa;

			case "convertToGeometry":

				break;

			default:
				break;
			}

			return null;
		} catch (OIUEException e) {
			factoryService.RollbackByProcess(processKey);
			throw e;
		} catch (Throwable e) {
			factoryService.RollbackByProcess(processKey);
			if (e instanceof InvocationTargetException)
				e = ((InvocationTargetException) e).getTargetException();
			if (e instanceof UndeclaredThrowableException)
				e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
			if (e instanceof InvocationTargetException)
				e = ((InvocationTargetException) e).getTargetException();
			if (e instanceof UndeclaredThrowableException)
				e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
			
			if (e instanceof OIUEException) {
				throw (OIUEException)e;
			}else
				throw new OIUEException(StatusResult._data_error, "", e);
		}
	}
	@Override
	public Object convertColumnType(Map data, Map event, String tokenid) throws Throwable {
		// String entity_column_id = MapUtil.getString(data, "entity_column_id");
		IResource iresource = factoryService.getBmo(IResource.class.getName());
		Map data_type = iresource.callEvent("559d4daf-f7db-4d44-9f39-417f37488c85", data_source_name, data);
		Map entity_column = iresource.callEvent("997d62df-5735-4245-a2a8-e6b6a35eae2b", data_source_name, data);
		data.put("column_type", MapUtil.getString(data_type, "name"));
		data.putAll(entity_column);
		String eventsstr = MapUtil.getString(event, "events");
		List<Map> events = null;
		if (!StringUtil.isEmptys(eventsstr)) {
			events = (List<Map>) JSONUtil.parserStrToList(eventsstr, false);
		}
		String sql = null;
		if (events.size() == 1) {
			sql = MapUtil.getString(events.get(0), "content",
					"alter table [table_name] alter [column_name] type [column_type] using [column_name]::[column_type]");
		}

		if (!StringUtil.isEmptys(sql)) {
			Collection<String> per = StringUtil.analyzeStringPer(sql, "[", "]");
			for (String key : per) {
				sql = StringReplace.replace(sql, "[" + key + "]", MapUtil.getString(data, key, ""), false);
			}

			try {

				iresource = factoryService.getBmo(IResource.class.getName());
				return iresource.callEvent("0aa268b6-b04a-45dc-80f1-d17e93a03c3a", data_source_name, data);
			} catch (OIUEException e) {
				throw e;
			} catch (Throwable e) {
				throw new OIUEException(StatusResult._conn_error, data, e);
			} finally {
			}
		}
		return null;
	}
}
