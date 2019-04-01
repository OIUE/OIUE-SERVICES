package org.oiue.service.event.entity.impl;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.entity.EntityService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private EntityService eventExecuteService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				EntityServiceImpl.cache = getService(CacheServiceManager.class);
				EntityServiceImpl.analyzerService = getService(AnalyzerService.class);
				EntityServiceImpl.factoryService = getService(FactoryService.class);
				EntityServiceImpl.logger = logService.getLogger(this.getClass());
				
				eventExecuteService = new EntityServiceImpl();
				
				registerService(EntityService.class, eventExecuteService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, CacheServiceManager.class, AnalyzerService.class, FactoryService.class);
	}
	
	@Override
	public void stop() {}
}
