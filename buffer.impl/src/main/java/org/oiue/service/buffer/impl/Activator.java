package org.oiue.service.buffer.impl;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.buffer.*;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				registerService(BufferService.class, new BufferServiceImpl(logService));
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
