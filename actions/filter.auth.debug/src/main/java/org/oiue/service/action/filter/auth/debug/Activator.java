package org.oiue.service.action.filter.auth.debug;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			private ActionService actionService;
			
			@Override
			public void removedService() {
				actionService.unregisterActionFilter("debugAuthFilter");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				actionService = getService(ActionService.class);
				OnlineService onlineService = getService(OnlineService.class);
				FactoryService factoryService = getService(FactoryService.class);
				
				new AuthFilterServiceImpl(logService, onlineService, actionService, factoryService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, ActionService.class, OnlineService.class, IResource.class, FactoryService.class);
	}
	
	@Override
	public void stop() {}
}
