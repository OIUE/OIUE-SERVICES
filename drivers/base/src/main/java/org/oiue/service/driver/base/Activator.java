package org.oiue.service.driver.base;

import java.util.Dictionary;

import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverServiceImpl driverService;
			
			@Override
			public void removedService() {
				driverService.unregisterAllDriver();
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				driverService = new DriverServiceImpl(logService);
				registerService(DriverService.class, driverService);
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				driverService.updated(props);
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
