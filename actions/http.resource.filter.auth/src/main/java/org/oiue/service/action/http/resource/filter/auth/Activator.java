package org.oiue.service.action.http.resource.filter.auth;

import java.util.Map;

import org.oiue.service.action.http.resource.ResourceFilterManger;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;

@SuppressWarnings("deprecation")
public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private HttpAuthFilterServiceImpl actionFilter;
			private ResourceFilterManger rfm ;
			
			@Override
			public void removedService() {
				rfm.unregisterResourceFilter("HttpAuthFilterService");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				OnlineService onlineService = getService(OnlineService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);
				
				actionFilter = new HttpAuthFilterServiceImpl(logService, onlineService, cacheService);
				rfm = getService(ResourceFilterManger.class);
				rfm.registerResourceFilter("HttpAuthFilterService", actionFilter, 1);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, OnlineService.class, HttpService.class, CacheServiceManager.class,ResourceFilterManger.class);
	}
	
	@Override
	public void stop() {}
}
