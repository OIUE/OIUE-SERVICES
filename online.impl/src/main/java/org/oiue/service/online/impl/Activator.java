package org.oiue.service.online.impl;

import java.util.Dictionary;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private OnlineService onlineService;
			private Logger logger;
			
			@Override
			public void removedService() {
				OnlineServiceImpl.clearClient = false;
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				CacheServiceManager cache = getService(CacheServiceManager.class);
				logger = logService.getLogger(this.getClass());
				onlineService = new OnlineServiceImpl(logService, cache);
				
				registerService(OnlineService.class, onlineService);
				
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				try {
					onlineService.updated(props);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, LogService.class, CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
