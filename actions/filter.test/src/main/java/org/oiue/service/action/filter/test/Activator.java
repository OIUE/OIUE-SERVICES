package org.oiue.service.action.filter.test;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private AuthFilterServiceImpl actionFilter;
			private ActionService actionService;
			
			@Override
			public void removedService() {
				actionService.unregisterActionFilter("testFilter");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				actionService = getService(ActionService.class);
				
				actionFilter = new AuthFilterServiceImpl(logService, actionService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class);
	}
	
	@Override
	public void stop() {}
}
