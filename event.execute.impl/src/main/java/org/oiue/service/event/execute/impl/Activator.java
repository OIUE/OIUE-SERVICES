package org.oiue.service.event.execute.impl;

import java.util.Dictionary;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.execute.EventExecuteService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;
public class Activator extends FrameActivator {

	@Override
	public void start() throws Exception {
		this.start(new MulitServiceTrackerCustomizer() {
			private EventExecuteService eventExecuteService;

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				EventExecuteServiceImpl.cache = getService(CacheServiceManager.class);
				EventExecuteServiceImpl.analyzerService = getService(AnalyzerService.class);
				EventExecuteServiceImpl.factoryService = getService(FactoryService.class);
				EventExecuteServiceImpl.onlineService = getService(OnlineService.class);
				EventExecuteServiceImpl.logger=logService.getLogger(this.getClass());

				eventExecuteService=new EventExecuteServiceImpl();

				registerService(EventExecuteService.class, eventExecuteService);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {

			}
		}, LogService.class,CacheServiceManager.class,AnalyzerService.class,FactoryService.class,OnlineService.class);
	}

	@Override
	public void stop() throws Exception {}
}
