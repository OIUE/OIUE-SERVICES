package org.oiue.service.online.filter;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				OnlineService onlineService = getService(OnlineService.class);
				CacheServiceManager cache = getService(CacheServiceManager.class);
//				actionService.registerActionFilter(UserInfoFilterServiceImpl.class.getName(), new UserInfoFilterServiceImpl(onlineService, cache,logService), 18);
				actionService.registerActionResultFilter(UserInfoFilterServiceImpl.class.getName(), new UserInfoFilterServiceImpl(onlineService, cache,logService), 18);
			}

			@Override
			public void updatedConf(Map<String, ?> props) {
			}
		}, LogService.class,ActionService.class,OnlineService.class,CacheServiceManager.class);
	}

	@Override
	public void stop()  {}
}
