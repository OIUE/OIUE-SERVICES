package org.oiue.service.action.filter.security;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private SecurityFilterServiceImpl actionFilter;
			private ActionService actionService;
			
			@Override
			public void removedService() {
				actionService.unregisterActionFilter("securityFilter");
				actionService.unregisterActionResultFilter("securityFilter");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				OnlineService onlineService = getService(OnlineService.class);
				actionService = getService(ActionService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);
				
				actionFilter = new SecurityFilterServiceImpl(logService, cacheService, onlineService, actionService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class, CacheServiceManager.class, OnlineService.class);
	}
	
	@Override
	public void stop() {}
}
