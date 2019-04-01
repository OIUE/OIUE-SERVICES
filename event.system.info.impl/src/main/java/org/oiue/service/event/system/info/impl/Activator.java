package org.oiue.service.event.system.info.impl;

import java.util.Map;

import org.oiue.service.event.system.info.EventSystemInfoService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private EventSystemInfoService eventSystemTimeService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				EventSystemInfoServiceImpl.logger = logService.getLogger(this.getClass());
				
				eventSystemTimeService = new EventSystemInfoServiceImpl();
				
				registerService(EventSystemInfoService.class, eventSystemTimeService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
