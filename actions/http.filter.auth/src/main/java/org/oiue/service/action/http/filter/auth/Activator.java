package org.oiue.service.action.http.filter.auth;

import java.util.Dictionary;

import org.apache.felix.http.api.ExtHttpService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;

@SuppressWarnings("deprecation")
public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {
			private HttpAuthFilterServiceImpl actionFilter;
			private HttpService httpService;

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				OnlineService onlineService = getService(OnlineService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);

				actionFilter = new HttpAuthFilterServiceImpl(logService, onlineService, cacheService);

				httpService = getService(HttpService.class);
				if (httpService instanceof ExtHttpService) {
					try {
						((ExtHttpService) httpService).registerFilter(actionFilter, "/.*", null, 1, httpService.createDefaultHttpContext());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class,OnlineService.class,HttpService.class,CacheServiceManager.class);
	}

	@Override
	public void stop()  {}
}
