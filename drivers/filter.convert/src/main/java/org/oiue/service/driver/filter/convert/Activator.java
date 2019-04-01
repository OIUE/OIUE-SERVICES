package org.oiue.service.driver.filter.convert;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverFilterService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;


public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverFilterService driverFilterService;

			ConvertTerminalTarget convertTerminalTarget = null;
			ConvertTargetTerminal convertTargetTerminal = null;
			ConvertDriverCodeID convertDriverCodeID = null;
			ConvertConstants constants= null;
			
			@Override
			public void removedService() {
				driverFilterService.unregisterReceiveFilter(convertTerminalTarget);
				driverFilterService.unregisterSendFilter(convertTargetTerminal);
				driverFilterService.unregisterSendFilter(convertDriverCodeID);
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				CacheServiceManager cache = getService(CacheServiceManager.class);
				FactoryService factory = getService(FactoryService.class);
				
				constants = new ConvertConstants(logService);
				
				driverFilterService = getService(DriverFilterService.class);
				
				convertTerminalTarget = new ConvertTerminalTarget(cache,logService,factory);
				driverFilterService.registerReceiveFilter(convertTerminalTarget, Integer.MIN_VALUE);
				
				convertDriverCodeID = new ConvertDriverCodeID(cache, logService,factory);
				driverFilterService.registerReceiveFilter(convertDriverCodeID, Integer.MIN_VALUE);
				
				convertTargetTerminal = new ConvertTargetTerminal(cache, logService,factory);
				driverFilterService.registerSendFilter(convertTargetTerminal, Integer.MIN_VALUE+1);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				constants.updateConfigure(props);
			}
		}, LogService.class, CacheServiceManager.class,FactoryService.class,DriverFilterService.class);
	}
	
	@Override
	public void stop() {}
}