package org.oiue.service.terminal.impl;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.terminal.TerminalService;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class TerminalServiceImpl implements TerminalService, Serializable {
	private Logger logger;
	private CacheServiceManager cache;

	public TerminalServiceImpl(LogService logService, FactoryService factoryService, CacheServiceManager cache) {
		logger = logService.getLogger(this.getClass());
		this.cache = cache;
	}

	@Override
	public String getNewVersion(Map data, Map event, String tokenid) {
		logger.debug("getNewVersion data:{}", data);
		Object c = cache.get("system_terminal_fota", MapUtil.getString(data,DriverDataField.type)+":"+MapUtil.getString(data,DriverDataField.TERMINAL_SN));
		if(c instanceof Map){
			return MapUtil.getString((Map<String, Object>) c, "version");
		}
		return null;
	}

	@Override
	public Map getFOTAInfo(Map data, Map event, String tokenid) {
		logger.debug("getFOTAInfo data:{}", data);
		Object c = cache.get("system_terminal_fota", MapUtil.getString(data,DriverDataField.type)+":"+MapUtil.getString(data,DriverDataField.TERMINAL_SN));
		if(c instanceof Map){
			return (Map) c;
		}
		return null;
	}
	
}
