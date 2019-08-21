package org.oiue.service.dataconvert.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.oiue.service.dataconvert.ConvertService;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.dmo.CallBack;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.threadpool.ThreadPoolService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

import com.lingtu.services.user.task.data.ITaskDataService;

/**
 * 数据转换服务
 * @author every
 *
 */
@SuppressWarnings({ "serial", "rawtypes", "unused" ,"unchecked", "static-access"})
public class ConvertServiceManagerImpl implements ConvertServiceManager, Serializable {
	private Logger logger;
	private String convert_type = "rectificationType";
	private String convert_default = "buffer";
	private FactoryService factoryService;
	private LogService logService;
	private HttpClientService httpClientService;
	private ITaskDataService taskDataService;
	private ThreadPoolService taskService;
	private static String data_source_name = null;
	private Map<String, ConvertService> converts = new HashMap<>();
	
	public ConvertServiceManagerImpl(LogService logService, FactoryService factoryService, HttpClientService httpClientService, ThreadPoolService taskService) {
		logger = logService.getLogger(getClass());
		this.factoryService=factoryService;
		this.logService=logService;
		this.taskService=taskService;
		this.httpClientService=httpClientService;
	}

	private Map props;
	public void updated(Map<String, ?> props) {
		this.props=props;
		String rectification_type = props.get("rectificationType") + "";
		if (!StringUtil.isEmptys(rectification_type)) {
			this.convert_type = rectification_type;
		}
		String rectification_default = props.get("rectificationCache") + "";
		if (!StringUtil.isEmptys(rectification_default)) {
			this.convert_default = rectification_default;
		}
	}
	
	@Override
	public ConvertService getConvertService(String name) {
		return converts.get(name);
	}
	
	@Override
	public boolean registerConvertService(String name, ConvertService convert) {
		logger.info("register ConvertService :{} ", name);
		if (converts.containsKey(name)) {
			return false;
		} else {
			converts.put(name, convert);
		}
		return true;
	}
	
	@Override
	public boolean unRegisterConvertService(String name) {
		logger.info("unregister ConvertService :{} ", name);
		if (converts.containsKey(name)) {
			converts.remove(name);
			return true;
		}
		return false;
	}

	@Override
	public StatusResult convert(Map data) {
		StatusResult sr = new StatusResult();
		sr.setResult(StatusResult._SUCCESS);
		String rectificationType=MapUtil.getString(data, convert_type);
		if(StringUtil.isEmptys(rectificationType)) {
			if (data == null) {
				sr = new StatusResult();
				sr.setResult(StatusResult._ncriticalAbnormal);
				sr.setDescription("data con not null");
				return sr;
			}
			if (props == null) {
				sr = new StatusResult();
				sr.setResult(StatusResult._ncriticalAbnormal);
				sr.setDescription(this.getClass().getName()+":props is null");
				return sr;
			}

			String driverName = (String) MapUtil.get(data, DriverDataField.driverName);
			String type = (String) MapUtil.get(data, DriverDataField.type);
			String event = this.props.get("dataconvert." + driverName + "." + type) + "";
			if (StringUtil.isEmptys(event)) {
				sr = new StatusResult();
				sr.setResult(StatusResult._SUCCESS);
				logger.info("cannot read dataconvert :dataconvert." + driverName + "." + type+"|data>"+data);
				return sr;
			}

			String[] events = event.split("\\|");
			for (String _event : events) {
				sr=converts.get(_event).convert(data);
				if (sr.getResult() == StatusResult._SUCCESS_OVER) {
					break;
				} else if (sr.getResult() == StatusResult._SUCCESS) {
					continue;
				} else if (sr.getResult() < StatusResult._NoncriticalAbnormal) {
					break;
				}
			}
			return sr;
		}else
		return converts.get(rectificationType).convert(data);
	}

	@Override
	public Object convert(Map data, Map event, String tokenid) {
		String type =  MapUtil.getString(data, DriverDataField.type,convert_default);
		
		StatusResult afr =converts.get(type).convert(data);
		if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
			throw new OIUEException(afr,data,null);
		}
		return data;
	}

	@Override
	public Object convert(List data, Map event, String tokenid) {
		return null;
	}
	
	@Override
	public Object entityConvert(Map data, Map event, String tokenid) {
		IResource iresource;
		String processKey = UUID.randomUUID().toString().replaceAll("-", "");
		iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
		Map rto = iresource.callEvent("5597bbf0-9550-46ce-9163-0dbb1e6a7048", data_source_name, data); // locked entity by entity_column_id
		if(MapUtil.getInt(rto, "count",0)==0) {
			throw new OIUEException(StatusResult._data_locked,"数据源处于锁定状态，无法执行此操作！");
		}
		return null;
	}
	private int corePoolSize=5;
	private int maximumPoolSize=50;
	private long keepAliveTime=30;
	class Convert implements Runnable{
		private Map taskConfig;
		private String type;
		private Logger logger;
		private String entity_id="";
		private String query_event_id="";
		public Convert(Map taskConfig, Logger logger) {
			this.taskConfig=taskConfig;
			this.entity_id=MapUtil.getString(taskConfig, "entity_id");
			this.query_event_id=MapUtil.getString(taskConfig, "query_event_id");
			this.logger=logger;
		}
		
		@Override
		public void run() {
			BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50000);
			try {
				taskService.registerThreadPool(entity_id, corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				CallBack cb = new CallBack() {
					private static final long serialVersionUID = -7983694577768487061L;
					@Override
					public boolean callBack(Map paramMap) {
						Future<StatusResult> f = taskService.addTask(entity_id, new ConvertTask(paramMap,type));
						return true;
					}
				};
				Map eventMap = iresource.getEventByIDName(query_event_id, data_source_name);
				String query_data_source_name = MapUtil.getString(taskConfig, "data_source_name");
				iresource.executeEvent(eventMap, StringUtil.isEmptys(query_data_source_name) ? data_source_name : query_data_source_name, taskConfig, cb);
//				iresource.callEvent(query_event_id, null,taskConfig , cb);
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
	class ConvertTask implements Callable<StatusResult>{
		Map data;String type;
		public ConvertTask(Map data,String type) {
			this.data=data;
			this.type=type;
		}
		@Override
		public StatusResult call() throws Exception {
			return converts.get(type).convert(data);
		}
	}
}