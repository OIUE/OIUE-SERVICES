package org.oiue.service.cache.jdbc;

import java.util.Map;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.sql.SqlService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private CacheServiceManager cacheServiceManager;
			
			@Override
			public void removedService() {
				cacheServiceManager.unRegisterCacheService("storage");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				SqlService sqlService = getService(SqlService.class);
				cacheServiceManager = getService(CacheServiceManager.class);
				
				CacheService cacheService = new StorageServiceImpl(logService, sqlService,cacheServiceManager);
				cacheServiceManager.registerCacheService("storage", cacheService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, CacheServiceManager.class, SqlService.class);
	}
	
	@Override
	public void stop() {}
}
