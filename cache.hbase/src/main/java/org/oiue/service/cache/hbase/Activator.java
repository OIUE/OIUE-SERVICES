package org.oiue.service.cache.hbase;

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
			private HBaseCacheService cacheService;
			
			@Override
			public void removedService() {
				cacheServiceManager.unRegisterCacheService("hbase");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				cacheServiceManager = getService(CacheServiceManager.class);
				
				cacheService = new HBaseCacheService(logService);
				cacheServiceManager.registerCacheService("hbase", cacheService);
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
