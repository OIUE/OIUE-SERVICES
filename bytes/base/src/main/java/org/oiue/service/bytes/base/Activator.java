package org.oiue.service.bytes.base;

import java.util.Map;

import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private BytesService bytesService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				bytesService = new BytesServiceImpl(logService);
				
				registerService(BytesService.class, bytesService);
			}
			
			@Override
			public void updatedConf(Map props) {
			
			}

		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
