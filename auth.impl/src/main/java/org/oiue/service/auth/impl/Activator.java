package org.oiue.service.auth.impl;

import java.util.Map;

import org.oiue.service.auth.AuthService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			AuthServiceManagerImpl authServiceManager;
			
			@Override
			public void removedService() {
				authServiceManager.unregister();
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				
				authServiceManager = new AuthServiceManagerImpl(logService);
				registerService(AuthService.class, authServiceManager);
				registerService(AuthServiceManager.class, authServiceManager);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				authServiceManager.updated(props);
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
