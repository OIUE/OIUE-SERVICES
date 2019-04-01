package org.oiue.service.cache.tree.script;

import java.util.Map;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				CacheTreeService buffer = (CacheTreeService) getService(CacheTreeService.class);
				registerService(CacheTreeScriptService.class, new CacheTreeScriptServiceImpl(buffer));
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, CacheTreeService.class);
	}
	
	@Override
	public void stop() {}
}
