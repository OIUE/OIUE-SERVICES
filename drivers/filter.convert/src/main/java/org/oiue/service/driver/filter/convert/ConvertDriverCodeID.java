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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConvertDriverCodeID implements DriverFilter {
	private CacheServiceManager cache;
	private Logger logger;

	public ConvertDriverCodeID(CacheServiceManager cache, LogService logService, FactoryService factory) {
		this.logger = logService.getLogger(this.getClass());
		this.cache = cache;
	}

	public Object getValue(Map map, String e) {
		String[] tmp = e.split("\\.");
		Map<String, Object> cursor = map;
		for (int i = 0; i < tmp.length - 1; i++) {
			Object tc = cursor.get(tmp[i]);
			if (tc instanceof Map) {
				cursor = (Map<String, Object>) tc;
			} else {
				cursor = null;
				break;
			}
		}
		return cursor.get(tmp[tmp.length - 1]);
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
		
		Map<String, String[]> filter = ConvertConstants.config.get(MapUtil.get(data, DriverDataField.driverName));

		if (filter == null || !filter.containsKey(MapUtil.get(data, DriverDataField.type))) {
			return sr;
		}
		Map map = (Map)MapUtil.get(data, "data");

		String[] kv = filter.get(MapUtil.get(data, DriverDataField.type));
		String driverCode = (String) this.getValue(map, kv[2]);

		if (driverCode == null) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("can't get driverCode key");
			return sr;
		}

		try {
			map.put(kv[4], cache.get(kv[3], driverCode));
		} catch (Exception e1) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("change terminal key to target id error");
			return sr;
		}
		return sr;
	}
}
