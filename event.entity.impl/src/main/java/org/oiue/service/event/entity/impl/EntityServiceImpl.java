package org.oiue.service.event.entity.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.entity.EntityService;
import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.dmo.CallBack;
import org.oiue.service.odp.event.api.EventConvertService;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.odp.structure.api.IServicesEvent;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.service.threadpool.ThreadPoolService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.list.ListUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringReplace;
import org.oiue.tools.string.StringUtil;

import com.lingtu.services.user.task.data.ITaskDataService;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityServiceImpl implements EntityService {
	private static final long serialVersionUID = 1L;
	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static ITaskDataService taskDataService;
	protected static CacheServiceManager cache;
	protected static FactoryService factoryService;
	private static String data_source_name = null;
	protected static HttpClientService httpClientService;
	protected static ThreadPoolService taskService;

	private int corePoolSize=5;
	private int maximumPoolSize=50;
	private long keepAliveTime=30;
	
	public void userDefinedEntity(Map data, Map event, String tokenid) throws Throwable {
		long startutc = System.currentTimeMillis();
		boolean process = false;
		if(!data.containsKey("processKey"))
			process=true;
		String processKey = MapUtil.getString(data, "processKey",UUID.randomUUID().toString().replaceAll("-", ""));
		try {
			data.put("processKey", processKey);
			logger.debug("1、elapsed time:{} ", System.currentTimeMillis()-startutc);
			createEntity(data, event, tokenid);
			logger.debug("2、elapsed time:{} ", System.currentTimeMillis()-startutc);
			insertEntity(data, event, tokenid);
			logger.debug("3、elapsed time:{} ", System.currentTimeMillis()-startutc);
			IServicesEvent se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
			data.put("addOperation", MapUtil.getBoolean(data, "addOperation",true));
			logger.debug("4、elapsed time:{} ", System.currentTimeMillis()-startutc);
			se.insertServiceEvent(data);
			logger.debug("5、elapsed time:{} ", System.currentTimeMillis()-startutc);
			if(process)
			factoryService.CommitByProcess(processKey);
		} catch (Throwable e) {
			if(process)
			factoryService.RollbackByProcess(processKey);
			throw e;
		}
		logger.debug("6、elapsed time:{} ", System.currentTimeMillis()-startutc);
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
			int sort = 1;
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
				Map field =new HashMap();
				String n_field_name = MapUtil.getString(field, "column_name","f_" + UUID.randomUUID().toString().replace("-", ""));
				field.put("entity_id", n_table_name);
				field.put("entity_column_id", n_field_name);
				field.put("column_name", _system_colnum);
				field.put("name", _system_colnum);
				field.put("column_desc", "系统ID");
				field.put("null_able", 0);
				field.put("status", 1);
				field.put("precision",36);
				field.put("scale", 0);
				field.put("sort", 0);
				field.put("encrypt_type", 0);
				field.put("type", "postgres_character_varying");
				field.put("user_id", user_id);
				field.put("ispk", true);
				field.put("update_time", update_time);
				fields.add(0, field);

				ctcsb.append("COMMENT ON COLUMN ").append(n_table_schema).append(".").append(n_table_name).append(".").append(_system_colnum).append(" is '").append("系统ID").append("';");
				ctsb.insert(ctsb.indexOf("(")+1,convert2Column(field)+",");
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
	
	public void createEntityView(Map data, Map oevent, String tokenid) {
		IResource iresource;
		String relation_service_event_id = MapUtil.getString(data, "relation_service_event_id");
		String service_event_id = MapUtil.getString(data, "service_event_id");
		String processKey = MapUtil.getString(data, "processKey");

		Map relation_entity = null;
		String relation_entity_name = null;
		if (!StringUtil.isEmptys(relation_service_event_id)) {
			Map<String, Object> dp = new HashMap<>();
			dp.put("service_event_id", relation_service_event_id);
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);// select relation entity
			relation_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
			relation_entity_name = MapUtil.getString(relation_entity, "name");
		}

		Map source_entity = null;
		String source_entity_name = null;
		if (!StringUtil.isEmptys(service_event_id)) {
			Map<String, Object> dp = new HashMap<>();
			dp.put("service_event_id", service_event_id);
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);// select relation entity
			source_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
			source_entity_name = MapUtil.getString(source_entity, "name");
		}

		List<Map> fields;
		fields = (List) data.get("fields");
		try {
			String n_table_name = "v_" + UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			data.put("table_type", "view");
			data.put("table_name", n_table_name);
			data.put("typecode", 0);
			
//			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
//			iresource.callEvent(insert_entity, data_source_name, data);// insert entity

			List<String> rtnField = new ArrayList<>();
			List<String> whereStr = new ArrayList<>();
			int i = 0;
			for (Map<String, Object> field : fields) {// entity_id,entity_column_id,entity_column_id,column_desc,primary_key,sort,user_id,o_entity_column_id

				String old_field_name = MapUtil.getString(field, "name");
				String n_field_name = "f_" + UUID.randomUUID().toString().replace("-", "");
				field.put("entity_id", n_table_name);

				field.put("o_entity_column_id", old_field_name);
				field.put("entity_column_id", n_field_name);
				field.put("column_name", n_field_name);
				field.put("sort", i++);
				field.put("user_id", data.get("user_id"));
				field.put("primary_key", MapUtil.getBoolean(field, "ispk", false) ? 1 : 0);
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
//				Map<String, Object> rf = (Map) iresource.callEvent("478925a7-3ce3-42dc-ae11-653e61a2a14c",data_source_name, field);// insert entity column
//
//				rf = ((List<Map>) rf.get("root")).get(0);
//				String fname = MapUtil.getString(rf, "remark", old_field_name);
//				String data_type_id = MapUtil.getString(rf, "data_type_id");
//				if ("postgres_point".equals(data_type_id)) {
//					data.put("geo_type", 10);
//					iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
//					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
//				} else if ("postgres_line".equals(data_type_id)) {
//					data.put("geo_type", 20);
//					iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
//					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
//				} else if ("postgres_polygon".equals(data_type_id)) {
//					data.put("geo_type", 30);
//					iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
//					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
//				}
//				MapUtil.mergeDifference(field, rf);
//				String relation_entity_column_id = MapUtil.getString(field, "relation_entity_column_id");
//
//				if (!StringUtil.isEmptys(relation_entity_column_id) && relation_entity != null) {
//					Map<String, Object> dp = new HashMap<>();
//					dp.put("entity_column_id", relation_entity_column_id);
//					iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);// query entity
//																										// column
//					dp = (Map<String, Object>) iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27",
//							data_source_name, dp);
//					whereStr.add("r." + fname + " = l." + MapUtil.getString(dp, "name", relation_entity_column_id));
//					rtnField.add(
//							"l." + MapUtil.getString(dp, "name", relation_entity_column_id) + " as " + n_field_name);
//				} else {
//					rtnField.add(fname + " as " + n_field_name);
//				}

			}
			data.put("relation", JSONUtil.parserToStr(fields));

			StringBuffer sql = new StringBuffer("select ").append(ListUtil.ListJoin(rtnField, ",")).append(" from ");
			if (relation_entity_name != null) {
				sql.append(relation_entity_name).append(" as l left join ").append(source_entity_name)
						.append(" as r on ").append(ListUtil.ListJoin(whereStr, " and "));
			} else {
				sql.append(source_entity_name);
			}
			String createView = null;
			EventConvertService convert = null;
			try {
				convert = factoryService.getDmo(EventConvertService.class.getName(),MapUtil.getString(source_entity, "dbtype"));
				Map event = new HashMap<>();
				event.put("content", sql.toString());
				event.put("event_type", "select");
				event.put("rule", "_intelligent");
				List<Map<?, ?>> events = convert.convert(event, data);
//				convert.setConn(localdb.getConnection());
				createView = events.get(0).get(EventField.content) + "";
				PreparedStatement pstmt = convert.getConn().prepareStatement(createView);
				convert.getIdmo().setPstmt(pstmt);
				List pers = (List) events.get(0).get(EventField.contentList);
				convert.getIdmo().setQueryParams(pers);

				createView = "CREATE OR REPLACE VIEW " + n_table_name + " as " + pstmt.toString();
			} finally {
				if (convert != null)
					convert.close();
			}
			logger.debug("sql:{}",createView );
			List<Map> events = null;
			String eventsstr = MapUtil.getString(oevent, "events");
			if (!StringUtil.isEmptys(eventsstr)) {
				events = (List<Map>) JSONUtil.parserStrToList(eventsstr, false);
			}
			events.get(0).put("content", createView);
			oevent.put("EVENTS", JSONUtil.parserToStr(events));
			oevent.put("EVENT_TYPE", "update");
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			iresource.executeEvent(oevent, data_source_name, data,null);// insert entity
			IServicesEvent se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
			data.put("addOperation", false);
			se.insertServiceEvent(data);
		} catch (OIUEException e) {
			throw e;
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, data, e);
		} finally {
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
				if (!data.containsKey("entity_column_id"))
					data.put("entity_column_id", MapUtil.getString(data, "x"));
				Map entity_column = iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27", data_source_name, data);
				data.put("entity_id", MapUtil.get(entity_column, "entity_id"));
				data.put("table_name", MapUtil.get(entity_column, "table_name"));
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

	@Override
	public void tm(Map data, Map event, String tokenid) throws SQLException {
		IResource iresource;
		IServicesEvent se;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");

		try {
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			String column_address =MapUtil.getString(data, "column_address");
			String column_province =MapUtil.getString(data, "column_province");
			String column_city =MapUtil.getString(data, "column_city");
			String column_district =MapUtil.getString(data, "column_district");
			data.put("entity_column_id", column_address);
			data.put("geo_type", 10);
			Map rto = iresource.callEvent("5597bbf0-9550-46ce-9163-0dbb1e6a7048", data_source_name, data); // locked entity by entity_column_id
			if(MapUtil.getInt(rto, "count",0)==0) {
				throw new OIUEException(StatusResult._data_locked,"数据源处于锁定状态，无法执行此操作！");
			}
			
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			iresource.callEvent("85e4edc8-dcad-4af5-aa69-f64128c520a5", data_source_name, data); // add geo column
			String column_name=MapUtil.getString(rto, "entity_column_id");
			
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			Map entity_column = iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27", data_source_name, data); //query by  entity_column_id
			String entity_id = MapUtil.getString(entity_column, "entity_id");
			String table_name =  MapUtil.getString(entity_column, "table_name");
			data.put("entity_id", entity_id);
			data.put("table_name", table_name);
			
			String update_event_id ="";
			String query_event_id ="" ;
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			List<Map> operation = iresource.callEvent("54d3cc4d-28ba-45aa-899a-3b0016ddb55d", data_source_name, entity_column); //query entity event
			
			for(Map d:operation) {
				if("query".equals(MapUtil.getString(d, "operation_type"))) {
					query_event_id = MapUtil.getString(d, "id");
				}else if("update".equals(MapUtil.getString(d, "operation_type"))) {
					update_event_id= MapUtil.getString(d, "id");
				}
			}
			
			se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
			se.updateServiceEvent(data);

			Map params = new HashMap();
			params.putAll(data);
			params.put("task_name", "地址匹配");
			params.put("status", 0);
			params.put("component_instance_event_id",MapUtil.get(event,"component_instance_event_id"));
			Map content = new HashMap();
			content.put("service_event_id", query_event_id);
			content.put("entity_id", entity_id);
			content.put("table_desc", MapUtil.getString(entity_column, "table_desc"));
			params.put("content", content);
			params = this.taskDataService.addTask(params, event, tokenid);
			
			Map taskConfig = new HashMap();
			taskConfig.put("user_task_id", MapUtil.get(params,"user_task_id"));
			taskConfig.put("entity_id", entity_id);
			taskConfig.put("user_id", MapUtil.getString(data, "user_id"));
			taskConfig.put("limit", -1);
			taskConfig.put("table_name", table_name);
			taskConfig.put("geo_column_name", column_name);
			taskConfig.put("query_event_id", query_event_id);
			taskConfig.put("update_event_id", update_event_id);
			taskConfig.put("column_address", column_address);
			taskConfig.put("tokenid", tokenid);
			taskConfig.put("content", content);
			new Thread(new ConvetMTask(taskConfig,logger)).start();
			
			factoryService.CommitByProcess(processKey);
		} catch (OIUEException e) {
			logger.error(e.getMessage(), e);
			factoryService.RollbackByProcess(processKey);
			throw e;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
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
	
	class ConvetMTask implements Runnable {
		private Map taskConfig;
		private Logger logger;
		private String url="http://172.17.13.22/gc?address=";
		private String entity_id="";
		private String query_event_id="";
		public ConvetMTask(Map taskConfig, Logger logger) {
			this.taskConfig=taskConfig;
			this.entity_id=MapUtil.getString(taskConfig, "entity_id");
			this.query_event_id=MapUtil.getString(taskConfig, "query_event_id");
			this.logger=logger;
		}
		@Override
		public void run() {
			BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50000);
			try {
				taskConfig.put("status", 1);
				taskDataService.updateTaskInfo(taskConfig, null, MapUtil.getString(taskConfig, "tokenid"));
				taskService.registerThreadPool(entity_id, corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				CallBack cb = new CallBack() {
					@Override
					public boolean callBack(Map paramMap) {
						taskService.addTask(entity_id, new ConvetTask(paramMap,url,taskConfig,logger));
						return true;
					}
				};
				iresource.callEvent(query_event_id, null,taskConfig , cb);
				while(workQueue.size()>0) {
					logger.debug("workQueue.size={} ,workQueue:{}",workQueue.size(), workQueue);
					Thread.currentThread().sleep(500);
				}
				taskConfig.put("status", 100);
				taskDataService.updateTaskInfo(taskConfig, null,  MapUtil.getString(taskConfig, "tokenid"));
				iresource = factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("7b198480-d215-48d7-b2f3-ea35141083b3", data_source_name, taskConfig); // unlocked entity by entity_id
//				if(MapUtil.getInt(rto, "count",0)==0) {
//					throw new OIUEException(StatusResult._data_locked,"数据源处于锁定状态，无法执行此操作！");
//				}
				
			}catch(Throwable e) {
				taskConfig.put("status", -1);
				MapUtil.put(taskConfig, "content.error",e.getMessage());
				taskDataService.updateTaskInfo(taskConfig, null,  MapUtil.getString(taskConfig, "tokenid"));
				logger.error("taskConfig:{} ,error:{}", taskConfig,e.getMessage());
				logger.error(e.getMessage(), e);
			}finally{
//				taskService.removeThreadPool(event_id);
			}
			
		}
		
	}
	class ConvetTask implements Runnable {
		private Map paramMap;
		private Logger logger;
		private String url;
		private Map taskConfig;
		public ConvetTask(Map paramMap,String url,Map taskConfig, Logger logger) {
			this.paramMap=paramMap;
			this.logger=logger;
			this.url=url;
		}

		@Override
		public void run() {
			Map rtn=null;
			try {
				String update_event_id=MapUtil.getString(taskConfig, "update_event_id");
				String column_name=MapUtil.getString(taskConfig, "geo_column_name");
				String column_address =MapUtil.getString(taskConfig, "column_address");
				String column_province =MapUtil.getString(taskConfig, "column_province");
				String column_city =MapUtil.getString(taskConfig, "column_city");
				String column_district =MapUtil.getString(taskConfig, "column_district");
				rtn = httpClientService.getGetData(url + (StringUtil.isEmptys(column_province)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_province)))
						+ (StringUtil.isEmptys(column_city)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_city)))
						+ (StringUtil.isEmptys(column_district)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_district)))
						+URLEncoder.encode(MapUtil.getString(paramMap, column_address)));
				if (200 == MapUtil.getInt(rtn, "status")) {
					Map datajson = JSONUtil.parserStrToMap(rtn.get("data")+"");
					if( "OK".equals(MapUtil.getString(datajson,"status"))){
					Map value = new HashMap();
					List coordinates = new ArrayList();
					coordinates.add(MapUtil.getDouble(datajson, "result.longitude"));
					coordinates.add(MapUtil.getDouble(datajson, "result.latitude"));
					value.put("coordinates", coordinates);
					value.put("type", "Point");
					paramMap.put(column_name, value);
					logger.debug("convert access:{}", paramMap);
					IResource iresource = factoryService.getBmo(IResource.class.getName());
					iresource.callEvent(update_event_id, null, paramMap);
				} else {
					logger.error("data convert error row:{} convert:{}", paramMap, rtn);
				}}
			} catch (Throwable e) {
				logger.error(e.getMessage()+" row:{} convert:{}", paramMap, rtn);
				logger.error(e.getMessage(), e);
			}
		}
		
	}
}
