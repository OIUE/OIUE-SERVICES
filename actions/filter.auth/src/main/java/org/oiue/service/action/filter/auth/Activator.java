package org.oiue.service.action.filter.auth;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
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
				actionService.unregisterActionFilter("authFilter");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				actionService = getService(ActionService.class);
				OnlineService onlineService = getService(OnlineService.class);
				AuthServiceManager auth = getService(AuthServiceManager.class);
				
				actionFilter = new AuthFilterServiceImpl(logService, onlineService, auth, actionService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class, OnlineService.class, AuthServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
