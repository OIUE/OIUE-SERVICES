package org.oiue.service.driver.filter.dataconvert;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.driver.api.DriverFilterService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;


public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverFilterService driverFilterService;
			private DataConvert dc;
			
			@Override
			public void removedService() {
				driverFilterService.unregisterReceiveFilter(dc);
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				CacheServiceManager cache = getService(CacheServiceManager.class);
				ConvertServiceManager factory = getService(ConvertServiceManager.class);
				driverFilterService = getService(DriverFilterService.class);
				
				dc = new DataConvert(cache, logService,factory);
				driverFilterService.registerReceiveFilter(dc, Integer.MIN_VALUE+2);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				dc.updateConfigure(props);
			}
		}, LogService.class, CacheServiceManager.class,ConvertServiceManager.class,DriverFilterService.class);
	}
	
	@Override
	public void stop() {}
}