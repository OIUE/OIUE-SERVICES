package org.oiue.service.cache.mongodb;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private CacheServiceManager cacheServiceManager;
			private mongoCacheService cacheService;
			
			@Override
			public void removedService() {
				cacheServiceManager.unRegisterCacheService("mongodb");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				cacheServiceManager = getService(CacheServiceManager.class);
				
				cacheService = new mongoCacheService(logService);
				cacheServiceManager.registerCacheService("mongodb", cacheService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				cacheService.updated(props);
			}
		}, LogService.class, CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
