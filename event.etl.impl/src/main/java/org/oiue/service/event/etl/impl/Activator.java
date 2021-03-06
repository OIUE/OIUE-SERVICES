package org.oiue.service.event.etl.impl;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.entity.EntityService;
import org.oiue.service.event.etl.ETLService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.odp.structure.api.IServicesEvent;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

import com.lingtu.services.user.task.data.ITaskDataService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		FrameActivator tracker = this;
		this.start(new MulitServiceTrackerCustomizer() {
			private ETLService eventExecuteService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				EventETLServiceImpl.cache = getService(CacheServiceManager.class);
				EventETLServiceImpl.analyzerService = getService(AnalyzerService.class);
				EventETLServiceImpl.entityService = getService(EntityService.class);
				EventETLServiceImpl.factoryService = getService(FactoryService.class);
				EventETLServiceImpl.onlineService = getService(OnlineService.class);
				EventETLServiceImpl.logger = logService.getLogger(this.getClass());
				EventETLServiceImpl.logService = logService;
				EventETLServiceImpl.taskDataService = getService(ITaskDataService.class);
				
				ClassLoader ccl = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(Runnable.class.getClassLoader());
				try {
					try {
						eventExecuteService = new EventETLServiceImpl();
					} finally {
						Thread.currentThread().setContextClassLoader(ccl);
					}
					registerService(ETLService.class, eventExecuteService);
				} catch (Throwable e) {
					EventETLServiceImpl.logger.error(e.getMessage(), e);
				}
				
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				eventExecuteService.updated(props,tracker);
			}
		}, LogService.class, CacheServiceManager.class, AnalyzerService.class, FactoryService.class, OnlineService.class, IResource.class, IServicesEvent.class,EntityService.class,ITaskDataService.class);
	}
	
	@Override
	public void stop() {}
}
