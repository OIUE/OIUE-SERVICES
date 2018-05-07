package org.oiue.service.driver.listener.impl;

import java.util.Dictionary;

import org.oiue.service.driver.api.DriverListenerService;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverListenerServiceImpl driverListenerService;
			private DriverService driverService;
			
			@Override
			public void removedService() {
				driverListenerService.unregisterAllListener();
				driverService.unregisterListenerService(driverListenerService);
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				driverService = getService(DriverService.class);
				
				driverListenerService = new DriverListenerServiceImpl(logService);
				driverService.registerListenerService(driverListenerService, null);
				registerService(DriverListenerService.class, driverListenerService);
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				driverListenerService.updated(props);
			}
		}, LogService.class, DriverService.class);
	}
	
	@Override
	public void stop() {}
}
