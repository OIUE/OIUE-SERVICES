package org.oiue.service.dataconvert.impl;

import java.util.Map;

import org.oiue.service.dataconvert.ConvertService;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.threadpool.ThreadPoolService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private ConvertServiceManagerImpl convertServiceManager;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				FactoryService factoryService = getService(FactoryService.class);
                HttpClientService httpClientService = getService(HttpClientService.class);
                ThreadPoolService taskService = getService(ThreadPoolService.class);
				convertServiceManager = new ConvertServiceManagerImpl(logService,factoryService,httpClientService,taskService);
				registerService(ConvertServiceManager.class, convertServiceManager);
				registerService(ConvertService.class, convertServiceManager);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				convertServiceManager.updated(props);
			}
		}, LogService.class,FactoryService.class,HttpClientService.class,ThreadPoolService.class);
	}
	
	@Override
	public void stop() {}
}
