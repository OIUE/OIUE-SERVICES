package org.oiue.service.terminal.impl;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.terminal.TerminalService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				FactoryService factoryService = getService(FactoryService.class);
				CacheServiceManager cache = getService(CacheServiceManager.class);
				registerService(TerminalService.class, new TerminalServiceImpl(logService, factoryService, cache));
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, FactoryService.class, CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
