package org.oiue.service.driver.listener.storage;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unused" })
public class DriverListenerStorageServiceImpl implements DriverListener {
	private static final long serialVersionUID = 1L;
	
	private IResource iResource;
	private AnalyzerService analyzerService;
	private CacheServiceManager cacheService;
	
	private List<DriverListener> receiveListenerList = new ArrayList<>();
	private HashMap<String, List<DriverListener>> receiveListenerMap = new HashMap<>();
	
	private Logger logger;
	private Map props;
	
	public DriverListenerStorageServiceImpl(LogService logService, IResource iResource, AnalyzerService analyzerService, CacheServiceManager cacheService) {
		logger = logService.getLogger(this.getClass());
		this.iResource = iResource;
		this.analyzerService = analyzerService;
		this.cacheService=cacheService;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public StatusResult receive(Map data) {
		StatusResult sr = null;
		if (data == null) {
			sr = new StatusResult();
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("data con not null");
			return sr;
		}
		if (props == null) {
			sr = new StatusResult();
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("props is null");
			return sr;
		}
		String driverName = MapUtil.getString(data, DriverDataField.driverName);
		String type = MapUtil.getString(data, DriverDataField.type);
		
		String event = this.props.get("storage." + driverName + "." + type) + "";
		if (StringUtil.isEmptys(event)) {
			sr = new StatusResult();
			sr.setResult(StatusResult._SUCCESS);
			logger.info("cannot read storage :storage." + driverName + "." + type+"|data>"+data);
			return sr;
		}
		
		String[] events = event.split("\\|");
		for (String _event : events) {
			try {
				String[] _events = _event.split(",");
				// this.iResource.callEvent(_events[0], _events.length==2?_events[1]:null, data);
				cacheService.getCacheService("storage").put(_events[0], data, Type.ONE);
			} catch (Throwable e) {
				logger.error("call event[" + _event + "] error:" + e.getMessage(), e);
			}
		}
		
		sr = new StatusResult();
		sr.setResult(StatusResult._SUCCESS);
		return sr;
	}
	
	public void updated(Map<String, ?> props) {
		this.props = props;
	}
}
