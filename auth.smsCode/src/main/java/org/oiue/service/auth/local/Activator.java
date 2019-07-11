package org.oiue.service.auth.local;

import java.util.Map;

import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			AuthSmsCodeServiceImpl authService;
			
			@Override
			public void removedService() {
				authService.unregister();
			}
			
			@SuppressWarnings("unused")
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				FactoryService factoryService = getService(FactoryService.class);
				AuthServiceManager authServiceManager = getService(AuthServiceManager.class);
				CacheServiceManager cacheServiceManager = getService(CacheServiceManager.class);
				IResource iResource = getService(IResource.class);
				
				authService = new AuthSmsCodeServiceImpl(logService, factoryService, authServiceManager,cacheServiceManager);
				
				// registerService(AuthService.class, authService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				authService.updated(props);
			}
		}, LogService.class, AuthServiceManager.class, FactoryService.class, IResource.class,CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
