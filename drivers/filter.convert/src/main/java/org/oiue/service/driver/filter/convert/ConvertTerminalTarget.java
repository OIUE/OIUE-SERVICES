package org.oiue.service.driver.filter.convert;

import java.util.Map;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverFilter;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })

public class ConvertTerminalTarget implements DriverFilter {
	private CacheService cache;
	private Logger logger;
	private FactoryService factory;
	private String event_id = "773c0efd-c1b9-468a-97a0-b58085ec3f34";

	public ConvertTerminalTarget(CacheService cache, LogService logService, FactoryService factory) {
		this.logger = logService.getLogger(this.getClass());
		this.cache = cache;
		this.factory = factory;
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
		try {

			Map map = (Map) MapUtil.get(data, "data");
			if (map == null) {
				sr.setResult(StatusResult._data_error);
				sr.setDescription("data error");
				return sr;
			}
			String driverName = (String) MapUtil.get(data, DriverDataField.driverName);
			if(ConvertConstants.ignoreTargetTerminal.contains(driverName))
				return sr;
			String terminalKey = (String) MapUtil.get(map, DriverDataField.TERMINAL_SN);
			if (terminalKey == null) {
				sr.setResult(StatusResult._data_error);
				sr.setDescription("can't get terminal key :"+DriverDataField.TERMINAL_SN+"|"+data);
				return sr;
			}
			String sync = driverName + ":" + terminalKey;
			Object value = null;
			synchronized (sync) {
				value = cache.get("terminal_target", sync);
				anchor:if (value == null) {

					IResource iresource = factory.getBmo(IResource.class.getName());
					Map tar = iresource.callEvent(event_id, null, data);
					if (tar != null) {
						cache.put(sync, tar, Type.ONE);
						value=tar;
						break anchor;
					}

					sr.setResult(StatusResult._data_error);
					sr.setDescription("can't get target info");
					return sr;
				}
			}
			
			try {
				if (value instanceof Map) {
					Map<String, Object> dmap = (Map<String, Object>) value;
					map.putAll(dmap);
				}
			} catch (Exception e1) {
				logger.error("change terminal key to target id error, terminal indentity = {}, terminal key = {}",
						driverName, terminalKey);
				sr.setResult(StatusResult._data_error);
				sr.setDescription("change terminal key to target id error");
				return sr;
			}

		} catch (Throwable e) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("ConvertTerminalTarget is error,error=" + e.getMessage());
			return sr;
		}
		return sr;
	}
}
