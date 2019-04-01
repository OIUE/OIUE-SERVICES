package org.oiue.service.event.system.time.impl;

import java.util.Map;

import org.oiue.service.event.system.time.EventSystemTimeService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private EventSystemTimeService eventSystemTimeService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				EventSystemTimeServiceImpl.logger = logService.getLogger(this.getClass());
				
				eventSystemTimeService = new EventSystemTimeServiceImpl();
				
				registerService(EventSystemTimeService.class, eventSystemTimeService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
