package org.oiue.service.action.filter.execute.visit;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {
			private ChangeSEVisitFilterServiceImpl actionFilter;

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				CacheServiceManager cacheServiceManager =getService(CacheServiceManager.class);

				actionFilter = new ChangeSEVisitFilterServiceImpl(logService, actionService,cacheServiceManager);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class,CacheServiceManager.class);
	}

	@Override
	public void stop()  {}
}
