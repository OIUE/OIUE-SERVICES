package org.oiue.service.cache.jedis;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		FrameActivator tracker = this;
		this.start(new MulitServiceTrackerCustomizer() {
			private CacheServiceManager cacheServiceManager;
			private JedisCacheService cacheService;
			
			@Override
			public void removedService() {
				cacheServiceManager.unRegisterCacheService("redis");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				cacheServiceManager = getService(CacheServiceManager.class);
				
				cacheService = new JedisCacheService(logService);
				cacheServiceManager.registerCacheService("redis", cacheService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				cacheService.updated(props,tracker);
			}
		}, LogService.class, CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
