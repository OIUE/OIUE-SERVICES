package org.oiue.service.dataconvert.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.oiue.service.dataconvert.ConvertService;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.threadpool.ThreadPoolService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * 数据转换服务
 * @author every
 *
 */
@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class ConvertServiceManagerImpl implements ConvertServiceManager, Serializable {
	private Logger logger;
	private String convert_type = "rectificationType";
	private String convert_default = "buffer";
	private FactoryService factoryService;
	private LogService logService;
	private HttpClientService httpClientService;
	private ThreadPoolService taskService;
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
		
		return null;
	}
	class Convert implements Runnable{
		@Override
		public void run() {
			
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