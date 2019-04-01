package org.oiue.service.cache.buffer;

import java.util.Map;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private CacheServiceManager cacheServiceManager;
			
			@Override
			public void removedService() {
				cacheServiceManager.unRegisterCacheService("buffer");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				BufferService bufferService = (BufferService) getService(BufferService.class);
				cacheServiceManager = (CacheServiceManager) getService(CacheServiceManager.class);
				
				CacheService cacheService = new BufferCacheServiceImpl(logService, bufferService);
				cacheServiceManager.registerCacheService("buffer", cacheService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, CacheServiceManager.class, BufferService.class);
	}
	
	@Override
	public void stop() {}
}
