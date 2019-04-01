package org.oiue.service.driver.filter.convert;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverFilter;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "rawtypes" })
public class ConvertTargetTerminal implements DriverFilter {
	private CacheServiceManager cache;
	private Logger logger;

	public ConvertTargetTerminal(CacheServiceManager bufferService, LogService logService, FactoryService factory) {
		this.logger = logService.getLogger(this.getClass());
		this.cache = bufferService;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(int priority) {

	}

	@Override
	public StatusResult doFilter(Map data) {
		StatusResult sr = new StatusResult();
		sr.setResult(StatusResult._SUCCESS);

		String driverName = (String) MapUtil.get(data, DriverDataField.driverName);
		if(ConvertConstants.ignoreTargetTerminal.contains(driverName))
			return sr;
		Map map = (Map) MapUtil.get(data, "data");
		String targetID = null;
		try {
			targetID = MapUtil.getString(map, DriverDataField.TARGET_ID);
		} catch (Exception ex) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("can't get target key "+DriverDataField.TARGET_ID);
			return sr;
		}

		if (targetID == null) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("can't get target key "+DriverDataField.TARGET_ID);
			return sr;
		}
		
		Object value = cache.get("target_terminal",targetID);
		if (value == null) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("can't get target info");
			return sr;
		}
		
		try {
			if (value instanceof Map) {
				Map<String, Object> dmap = (Map<String, Object>) value;
				map.putAll(dmap);
			}
		} catch (Exception e1) {
			logger.error("change target key to terminal id error, target indentity = {}" ,targetID);
			sr.setResult(StatusResult._data_error);
			sr.setDescription("change terminal key to target id error");
			return sr;
		}
		return sr;
	}
}
