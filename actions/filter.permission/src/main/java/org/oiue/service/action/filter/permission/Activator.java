package org.oiue.service.action.filter.permission;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.permission.PermissionServiceManager;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private PermissionFilterServiceImpl actionFilter;
			private ActionService actionService;
			
			@Override
			public void removedService() {
				actionService.unregisterActionFilter("permissionFilter");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				actionService = getService(ActionService.class);
				PermissionServiceManager permissionService = getService(PermissionServiceManager.class);
				OnlineService onlineService = getService(OnlineService.class);
				
				actionFilter = new PermissionFilterServiceImpl(logService, onlineService, permissionService, actionService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class, PermissionServiceManager.class, OnlineService.class);
	}
	
	@Override
	public void stop() {}
}
