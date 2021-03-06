package org.oiue.service.cache.synchronization.db;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.task.TaskService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private SynchronizationDbRefresh refreshDb;
			
			@Override
			public void removedService() {
				if (refreshDb != null)
					refreshDb.shutdown();
			}
			
			@SuppressWarnings("unused")
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				FactoryService factoryService = getService(FactoryService.class);
				TaskService taskService = getService(TaskService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);
				IResource iResource = getService(IResource.class);
				
				refreshDb = new SynchronizationDbRefresh(cacheService, taskService, factoryService, logService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				refreshDb.updateProps(props);
			}
		}, LogService.class, FactoryService.class, TaskService.class, CacheServiceManager.class, IResource.class);
	}
	
	@Override
	public void stop() {}
}
