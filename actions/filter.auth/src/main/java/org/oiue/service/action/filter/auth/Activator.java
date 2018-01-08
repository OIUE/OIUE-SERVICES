package org.oiue.service.action.filter.auth;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.permission.PermissionServiceManager;

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
				AuthServiceManager authService = getService(AuthServiceManager.class);
				PermissionServiceManager permissionService = getService(PermissionServiceManager.class);
				OnlineService onlineService = getService(OnlineService.class);

				actionFilter = new AuthFilterServiceImpl(logService, onlineService, permissionService, authService, actionService);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class, AuthServiceManager.class, PermissionServiceManager.class, OnlineService.class);
	}

	@Override
	public void stop() {
	}
}
