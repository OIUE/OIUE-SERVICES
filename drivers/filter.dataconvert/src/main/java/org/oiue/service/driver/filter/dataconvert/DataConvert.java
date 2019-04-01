package org.oiue.service.driver.filter.dataconvert;

import java.util.Map;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverFilter;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })

public class DataConvert implements DriverFilter {
	private CacheService cache;
	private Logger logger;
	private ConvertServiceManager factory;
	private Map props;

	public DataConvert(CacheService cache, LogService logService, ConvertServiceManager factory) {
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
		try {

			String driverName = (String) MapUtil.get(data, DriverDataField.driverName);
			String type = (String) MapUtil.get(data, DriverDataField.type);
			String event = this.props.get("dataconvert." + driverName + "." + type) + "";
			if (StringUtil.isEmptys(event)) {
				sr = new StatusResult();
				sr.setResult(StatusResult._SUCCESS);
				logger.info("cannot read dataconvert :dataconvert." + driverName + "." + type+"|data>"+data);
				return sr;
			}
			try {
				return factory.convert(data);
			} catch (Exception e1) {
				logger.error("convert data error, driverName = {}, type = {}",driverName, type);
				sr.setResult(StatusResult._data_error);
				sr.setDescription("convert data error");
				return sr;
			}

		} catch (Throwable e) {
			sr.setResult(StatusResult._data_error);
			sr.setDescription("convert data error,error=" + e.getMessage());
			return sr;
		}
	}

	public void updateConfigure(Map<String, ?> props) {
		this.props=props;
	}
}
