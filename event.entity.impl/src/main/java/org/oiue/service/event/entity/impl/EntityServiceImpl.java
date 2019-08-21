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

	private int corePoolSize=1;
	private int maximumPoolSize=3;
	private long keepAliveTime=30;
	private String dbName = "postgis";
	
	public void userDefinedEntity(Map data, Map event, String tokenid) throws Throwable {
		long startutc = System.currentTimeMillis();
		IResource iresource;
		boolean process = false;
		if(!data.containsKey("processKey"))
			process=true;
		String processKey = MapUtil.getString(data, "processKey",UUID.randomUUID().toString().replaceAll("-", ""));
		try {
			data.put("processKey", processKey);
			logger.debug("1、elapsed time:{} ", System.currentTimeMillis()-startutc);
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			Map m=iresource.callEvent("2d901cb4-84ea-46bd-bf49-5809cae70dac", data_source_name, data);
			data.putAll(m);
			logger.debug("2、elapsed time:{} ", System.currentTimeMillis()-startutc);
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

	
	public void createEntityView(Map data, Map oevent, String tokenid) throws Throwable {
		String entity_id = MapUtil.getString(data, "entity_id");
		String relation_entity_id = MapUtil.getString(data, "relation_entity_id");
		String processKey = MapUtil.getString(data, "processKey");
		boolean iscommit=false;
		if(StringUtil.isEmpty(processKey)) {
			processKey=UUID.randomUUID().toString();
			iscommit=true;
		}

		IResource iresource;
		Map relation_entity = null;
		String relation_entity_name = null;
		try {
			if (!StringUtil.isEmptys(relation_entity_id)) {
				Map<String, Object> dp = new HashMap<>();
				dp.put("entity_id", relation_entity_id);
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);// select relation entity
				relation_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
				relation_entity_name = MapUtil.getString(relation_entity, "table_name");
			}
			
			Map source_entity = null;
			String source_entity_name = null;
			if (!StringUtil.isEmptys(entity_id)) {
				Map<String, Object> dp = new HashMap<>();
				dp.put("entity_id", entity_id);
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);// select relation entity
				source_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
				source_entity_name = MapUtil.getString(source_entity, "table_name");
			}
			
			List<Map> fields;
			try {
				fields = (List) data.get("fields");
				String schema = MapUtil.getString(data, "schema","public");
				String n_table_name = "v_" + UUID.randomUUID().toString().replace("-", "");
				data.put("entity_id", n_table_name);
				data.put("table_type", "view");
				data.put("table_schema", "public");
				data.put("data_source_id", "fm_data_type_postgresql");
				data.put("table_name", n_table_name);
				data.put("typecode", 0);
				
				List<String> rtnField = new ArrayList<>();
				List<String> whereStr = new ArrayList<>();
				List<String> comments = new ArrayList();
				for (Map<String, Object> field : fields) {
					String old_field_name = MapUtil.getString(field, "name");
					String old_relation_field_name = MapUtil.getString(field, "relation_name");
					String n_field_name = "f_" + UUID.randomUUID().toString().replace("-", "");
					comments.add("COMMENT ON COLUMN \""+schema+"\".\""+n_table_name+"\".\""+n_field_name+"\"IS '"+ MapUtil.getString(field, "column_desc")+"'");
					if (!StringUtil.isEmptys(old_relation_field_name)) {
						if(!StringUtil.isEmptys(old_field_name))
							whereStr.add("r." + old_field_name + " = l." + old_relation_field_name);
						rtnField.add("l." + old_relation_field_name + " as " + n_field_name);
					} else if(MapUtil.getString(field, "entity_id","entity_id").equals(relation_entity_id)){
						rtnField.add("l." + old_field_name + " as " + n_field_name);
					}else {
						rtnField.add("r." + old_field_name + " as " + n_field_name);
					}
				}
				data.put("relation", JSONUtil.parserToStr(fields));
				
				StringBuffer sql = new StringBuffer("select ").append(ListUtil.ListJoin(rtnField, ",")).append(" from ");
				if (relation_entity_id != null) {
					sql.append(relation_entity_name).append(" as l left join ").append(source_entity_name)
					.append(" as r on ").append(ListUtil.ListJoin(whereStr, " and "));
				} else {
					sql.append(source_entity_name).append(" as r ");
				}
				String createView = null;
				EventConvertService convert = null;
				try {
					convert = factoryService.getDmo(EventConvertService.class.getName(),MapUtil.getString(source_entity, "dbtype"));
					Map event = new HashMap<>();
					logger.debug("sql:{}",sql.toString() );
					event.put("content", sql.toString());
					event.put("event_type", "select");
					event.put("rule", "_intelligent");
					convert.setConn(factoryService.getProxyFactory().getOp().getDs().getConn(dbName));
					List<Map<?, ?>> events = convert.convert(event, data);
					createView = events.get(0).get(EventField.content) + "";
					PreparedStatement pstmt = convert.getConn().prepareStatement(createView);
					convert.getIdmo().setPstmt(pstmt);
					List pers = (List) events.get(0).get(EventField.contentList);
					convert.getIdmo().setQueryParams(pers);
					
					createView = "CREATE OR REPLACE VIEW \""+schema+"\".\""+n_table_name+"\" as " + pstmt.toString();
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
				createView=createView+";"+ListUtil.ListJoin(comments, ";")+";COMMENT ON VIEW \"public\".\""+n_table_name+"\" IS '"+MapUtil.getString(data, "entity_desc")+"';";
				events.get(0).put("content",createView);
				oevent.put("EVENTS", JSONUtil.parserToStr(events));
				oevent.put("EVENT_TYPE", "update");
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
				iresource.executeEvent(oevent, data_source_name, new HashMap(),null);// insert entity
				iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
				List<Map> rtn = iresource.callEvent("5a664dec-2cd9-4caf-8cdf-29b5308e7e68", null, data);
//			IServicesEvent se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
//			data.put("addOperation", false);
//			se.insertServiceEvent(data);
			} catch (OIUEException e) {
				throw e;
			} catch (Throwable e) {
				throw new OIUEException(StatusResult._conn_error, data, e);
			} finally {
			}
			if(iscommit)
				factoryService.CommitByProcess(processKey);
		} catch (OIUEException e) {
			if(iscommit)
			factoryService.RollbackByProcess(processKey);
			throw e;
		}catch(Throwable e) {
			if(iscommit)
			factoryService.RollbackByProcess(processKey);
			throw new OIUEException(StatusResult._conn_error, data, e);
		}
	}
	
	@Override
	public void loadEntity(Map data, Map event, String tokenid) {
		
	}

	@Override
	public Object convertToGeometry(Map data, Map event, String tokenid) throws Throwable {
		String type = MapUtil.getString(data, "operation_type");
		IResource iresource;
		IServicesEvent se;
		int geo_type = 100;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");
		Map params = new HashMap();
		params.putAll(data);
		params.put("task_name", "空间转换");
		params.put("status", 0);
		params.put("component_instance_event_id",MapUtil.get(event,"component_instance_event_id"));
		Map content = new HashMap();
		content.put("entity_column_id", data.get("entity_column_id"));
		content.put("entity_id", data.get("entity_id"));
		params.put("content", content);
		params.putAll(taskDataService.addTask(params, event, tokenid));
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
//				se = factoryService.getBmoByProcess(IServicesEvent.class.getName(), processKey);
//				se.updateServiceEvent(data);
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
		}finally{
			params.put("status", 100);
			taskDataService.updateTaskInfo(params, null,  tokenid);
		}
	}
	@Override
	public Object convertColumnType(Map data, Map event, String tokenid) throws Throwable {
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
			sql = MapUtil.getString(events.get(0), "content","alter table [table_name] alter [column_name] type [column_type] using [column_name]::[column_type]");
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
	public void geo(Map data, Map event, String tokenid) throws Throwable {
		IResource iresource;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");

		String query_event_id ="" ;
		String update_event_id ="";
		String update_column_name= "";
		
		String column_address =MapUtil.getString(data, "column_address");
		String column_province =MapUtil.getString(data, "column_province");
		String column_city =MapUtil.getString(data, "column_city");
		String column_district =MapUtil.getString(data, "column_district");
		try {
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			data.put("entity_column_id", column_address);
			data.put("geo_type", 10);
			Map rto = iresource.callEvent("5597bbf0-9550-46ce-9163-0dbb1e6a7048", data_source_name, data); // locked entity by entity_column_id
			if(MapUtil.getInt(rto, "count",0)==0) {
				throw new OIUEException(StatusResult._data_locked,"数据源处于锁定状态，无法执行此操作！");
			}
			
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			rto = iresource.callEvent("85e4edc8-dcad-4af5-aa69-f64128c520a5", data_source_name, data); // add geo column
			update_column_name=MapUtil.getString(rto, "entity_column_id");
			factoryService.CommitByProcess(processKey);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			factoryService.RollbackByProcess(processKey);
			throw e;
		}

		iresource = factoryService.getBmo(IResource.class.getName());
		Map entity_column = iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27", data_source_name, data); //query by  entity_column_id
		
		String entity_id = MapUtil.getString(entity_column, "entity_id");
		String table_name =  MapUtil.getString(entity_column, "table_name");
		data.put("entity_id", entity_id);
		data.put("table_name", table_name);

		iresource = factoryService.getBmo(IResource.class.getName());
		Map columns = iresource.callEvent("00525d76-8d02-467e-969b-c87cc3df6c74", data_source_name, data); //query entity columns by  entity_id
		columns=(Map) columns.get("columns");
		
		iresource = factoryService.getBmo(IResource.class.getName());
		List<Map> operation = iresource.callEvent("54d3cc4d-28ba-45aa-899a-3b0016ddb55d", data_source_name, entity_column); //query entity event
		
		for(Map d:operation) {
			if("query".equals(MapUtil.getString(d, "operation_type"))) {
				query_event_id = MapUtil.getString(d, "id");
			}else if("update".equals(MapUtil.getString(d, "operation_type"))) {
				update_event_id= MapUtil.getString(d, "id");
			}
		}
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
		logger.debug("add task start");
		params.putAll(taskDataService.addTask(params, event, tokenid));
		logger.debug("add task over");
		Map taskConfig = new HashMap();
		taskConfig.putAll(data);
		taskConfig.put("user_task_id", MapUtil.get(params,"user_task_id"));
		taskConfig.put("entity_id", entity_id);
		taskConfig.put("user_id", MapUtil.getString(data, "user_id"));
		taskConfig.put("limit", -1);
		taskConfig.put("table_name", table_name);
		taskConfig.put("geo_column_name", MapUtil.get(columns,update_column_name));
		taskConfig.put("query_event_id", query_event_id);
		taskConfig.put("update_event_id", update_event_id);
		taskConfig.put("column_address", MapUtil.get(columns,column_address));
		taskConfig.put("column_province", column_province==null?null:MapUtil.get(columns,column_province));
		taskConfig.put("column_city", column_city==null?null:MapUtil.get(columns,column_city));
		taskConfig.put("column_district", column_district==null?null:MapUtil.get(columns,column_district));
		taskConfig.put("tokenid", tokenid);
		taskConfig.put("content", content);
		new Thread(new ConvetMTask(taskConfig,logger)).start();
		
	}
	
	
	class ConvetMTask implements Runnable {
		private Map taskConfig;
		private Logger logger;
		private String entity_id="";
		private String query_event_id="";
		public ConvetMTask(Map taskConfig, Logger logger) {
			this.taskConfig=taskConfig;
			this.entity_id=MapUtil.getString(taskConfig, "entity_id");
			this.query_event_id=MapUtil.getString(taskConfig, "query_event_id");
			this.logger=logger;
		}
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50000);
			try {
				logger.debug("start convert ...");
				taskConfig.put("status", 1);
				taskDataService.updateTaskInfo(taskConfig, null, MapUtil.getString(taskConfig, "tokenid"));
				taskService.registerThreadPool(entity_id, corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				CallBack cb = new CallBack() {
					private static final long serialVersionUID = -7983694577768487061L;

					@Override
					public boolean callBack(Map paramMap) {
						logger.debug("addTask ...");
						taskService.addTask(entity_id, new ConvetTask(paramMap,gc_url,taskConfig,logger));
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
			}catch(Throwable e) {
				taskConfig.put("status", -1);
				MapUtil.put(taskConfig, "content.error",e.getMessage());
				taskDataService.updateTaskInfo(taskConfig, null,  MapUtil.getString(taskConfig, "tokenid"));
				logger.error("taskConfig:{} ,error:{}", taskConfig,e.getMessage());
				logger.error(e.getMessage(), e);
			}finally{}
			
		}
		
	}
	class ConvetTask implements Runnable {
		private Map paramMap;
		private Logger logger;
		private String url;
		private Map taskConfig;
		public ConvetTask(Map paramMap,String url,Map taskConfig, Logger logger) {
			this.paramMap=paramMap;
			this.taskConfig=taskConfig;
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
				logger.debug("taskConfig:{}", taskConfig);
				rtn = httpClientService.getGetData(url + (StringUtil.isEmptys(column_province)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_province)))
						+ (StringUtil.isEmptys(column_city)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_city)))
						+ (StringUtil.isEmptys(column_district)?"":URLEncoder.encode(MapUtil.getString(paramMap, column_district)))
						+URLEncoder.encode(MapUtil.getString(paramMap, column_address)));
				if (200 == MapUtil.getInt(rtn, "status")) {
					Map datajson = JSONUtil.parserStrToMap(rtn.get("data") + "");
					if ("OK".equals(MapUtil.getString(datajson, "status"))) {
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
					}
				} else {
					logger.error("data convert error row:{} convert:{}", paramMap, rtn);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage()+" row:{} convert:{} taskConfig:{}", paramMap, rtn,taskConfig);
				logger.error(e.getMessage(), e);
			}
		}
		
	}
	

	@Override
	public void regeo(Map data, Map event, String tokenid) throws SQLException {
		IResource iresource;
		IServicesEvent se;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");

		String column_address = MapUtil.getString(data, "column_address");
		String column_province = MapUtil.getString(data, "column_province");
		String column_city = MapUtil.getString(data, "column_city");
		String column_district = MapUtil.getString(data, "column_district");
		String column_street = MapUtil.getString(data, "column_street");
		String column_poi = MapUtil.getString(data, "column_poi");
		try {
			iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
			data.put("entity_column_id", column_address);
			data.put("geo_type", 10);

			Map rto = iresource.callEvent("5597bbf0-9550-46ce-9163-0dbb1e6a7048", data_source_name, data); // locked entity by entity_column_id
			if (MapUtil.getInt(rto, "count", 0) == 0) {
				throw new OIUEException(StatusResult._data_locked, "数据源处于锁定状态，无法执行此操作！");
			}

			factoryService.CommitByProcess(processKey);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			factoryService.RollbackByProcess(processKey);
			throw e;
		}
		iresource = factoryService.getBmo(IResource.class.getName());
		Map entity_column = iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27", data_source_name, data); // query entity info by entity_column_id
		String entity_id = MapUtil.getString(entity_column, "entity_id");
		String table_name = MapUtil.getString(entity_column, "table_name");
		data.put("entity_id", entity_id);
		data.put("table_name", table_name);

		iresource = factoryService.getBmo(IResource.class.getName());
		Map columns = iresource.callEvent("00525d76-8d02-467e-969b-c87cc3df6c74", data_source_name, data); // query entity columns by entity_id
		columns = (Map) columns.get("columns");

		iresource = factoryService.getBmo(IResource.class.getName());
		Map rto = iresource.callEvent("12dfe04c-d125-4c95-a038-9c6cf471bd40", data_source_name, data); // query geo column
		String column_name = MapUtil.getString(rto, "entity_column_id");

		String update_event_id = "";
		String query_event_id = "";
		iresource = factoryService.getBmo(IResource.class.getName());
		List<Map> operation = iresource.callEvent("54d3cc4d-28ba-45aa-899a-3b0016ddb55d", data_source_name, entity_column); // query entity event

		for (Map d : operation) {
			if ("query".equals(MapUtil.getString(d, "operation_type"))) {
				query_event_id = MapUtil.getString(d, "id");
			} else if ("update".equals(MapUtil.getString(d, "operation_type"))) {
				update_event_id = MapUtil.getString(d, "id");
			}
		}

		Map params = new HashMap();
		params.putAll(data);
		params.put("task_name", "逆地理编码");
		params.put("status", 0);
		params.put("component_instance_event_id", MapUtil.get(event, "component_instance_event_id"));
		Map content = new HashMap();
		content.put("service_event_id", query_event_id);
		content.put("entity_id", entity_id);
		content.put("table_desc", MapUtil.getString(entity_column, "table_desc"));
		params.put("content", content);
		params.putAll(this.taskDataService.addTask(params, event, tokenid));

		Map taskConfig = new HashMap();
		taskConfig.putAll(data);
		taskConfig.put("user_task_id", MapUtil.get(params, "user_task_id"));
		taskConfig.put("entity_id", entity_id);
		taskConfig.put("user_id", MapUtil.getString(data, "user_id"));
		taskConfig.put("limit", -1);
		taskConfig.put("table_name", table_name);
		taskConfig.put("geo_column_name", MapUtil.get(columns, column_name));
		taskConfig.put("query_event_id", query_event_id);
		taskConfig.put("update_event_id", update_event_id);
		taskConfig.put("column_address", MapUtil.get(columns, column_address));
		taskConfig.put("column_province", column_province == null ? null : MapUtil.get(columns, column_province));
		taskConfig.put("column_city", column_city == null ? null : MapUtil.get(columns, column_city));
		taskConfig.put("column_district", column_district == null ? null : MapUtil.get(columns, column_district));
		taskConfig.put("column_street", column_street == null ? null : MapUtil.get(columns, column_street));
		taskConfig.put("column_poi", column_poi == null ? null : MapUtil.get(columns, column_poi));
		taskConfig.put("tokenid", tokenid);
		taskConfig.put("content", content);
		new Thread(new ConvetRMTask(taskConfig, logger)).start();

	}
	class ConvetRMTask implements Runnable {
		private Map taskConfig;
		private Logger logger;
		private String entity_id="";
		private String query_event_id="";
		public ConvetRMTask(Map taskConfig, Logger logger) {
			this.taskConfig=taskConfig;
			this.entity_id=MapUtil.getString(taskConfig, "entity_id");
			this.query_event_id=MapUtil.getString(taskConfig, "query_event_id");
			this.logger=logger;
		}
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50000);
			try {
				taskConfig.put("status", 1);
				taskDataService.updateTaskInfo(taskConfig, null, MapUtil.getString(taskConfig, "tokenid"));
				taskService.registerThreadPool(entity_id, corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				CallBack cb = new CallBack() {
					private static final long serialVersionUID = 8676257347203059354L;

					@Override
					public boolean callBack(Map paramMap) {
						taskService.addTask(entity_id, new ReGEOTask(paramMap,regc_url,taskConfig,logger));
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
	class ReGEOTask implements Runnable {
		private Map paramMap;
		private Logger logger;
		private String url;
		private Map taskConfig;
		public ReGEOTask(Map paramMap,String url,Map taskConfig, Logger logger) {
			this.paramMap=paramMap;
			this.taskConfig=taskConfig;
			this.logger=logger;
			this.url=url;
		}
		
		@SuppressWarnings("deprecation")
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
				String column_street =MapUtil.getString(taskConfig, "column_street");
				String column_poi =MapUtil.getString(taskConfig, "column_poi");
				logger.debug("taskConfig:{}", taskConfig);
				String location = MapUtil.getString(paramMap, column_name);
				location=location.substring(location.indexOf("[")+1, location.indexOf("]"));
				rtn = httpClientService.getGetData(url + (StringUtil.isEmptys(location)?"":URLEncoder.encode(location)));
				if (200 == MapUtil.getInt(rtn, "status")) {
					Map datajson = JSONUtil.parserStrToMap(rtn.get("data") + "");
					if ("OK".equals(MapUtil.getString(datajson, "status"))) {
						paramMap.put(column_address, MapUtil.getString(datajson, "result.points.0.address"));
						if (column_poi != null)
							paramMap.put(column_poi, MapUtil.getString(datajson, "result.points.0.name"));
						if (column_province != null)
							paramMap.put(column_province, MapUtil.getString(datajson, "result.province"));
						if (column_city != null)
							paramMap.put(column_city, MapUtil.getString(datajson, "result.city"));
						if (column_district != null)
							paramMap.put(column_district, MapUtil.getString(datajson, "result.district"));
						if (column_street != null)
							paramMap.put(column_street, MapUtil.getString(datajson, "result.roads.0.name"));
						logger.debug("convert access:{} convert:{}", paramMap, rtn);
						IResource iresource = factoryService.getBmo(IResource.class.getName());
						iresource.callEvent(update_event_id, null, paramMap);
					} else {
						logger.error("data convert error row:{} convert:{}", paramMap, rtn);
					}
				} else {
					logger.error("data convert error row:{} convert:{}", paramMap, rtn);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage()+" row:{} convert:{} taskConfig:{}", paramMap, rtn,taskConfig);
				logger.error(e.getMessage(), e);
			}
		}
		
	}

	static String gc_url;
	static String regc_url;
	@Override
	public void updatedConf(Map props) {
		gc_url=MapUtil.getString(props, "gc_url");
		regc_url=MapUtil.getString(props, "regc_url");
	}
}
