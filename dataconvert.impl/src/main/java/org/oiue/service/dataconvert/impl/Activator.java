package org.oiue.service.dataconvert.impl;

import java.util.Map;

import org.oiue.service.dataconvert.ConvertService;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private ConvertServiceManagerImpl convertServiceManager;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				
				convertServiceManager = new ConvertServiceManagerImpl(logService);
				registerService(ConvertServiceManager.class, convertServiceManager);
				registerService(ConvertService.class, convertServiceManager);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				convertServiceManager.updated(props);
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
