package org.oiue.service.action.http.filter.fota;

import java.util.Dictionary;

import org.apache.felix.http.api.ExtHttpService;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.terminal.TerminalService;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/fota";
			private HttpService httpService;
			private FotaFilterServiceImpl upload;
			
			@Override
			public void removedService() {
				((ExtHttpService) httpService).unregisterFilter(upload);
			}
			
			@Override
			public void addingService() {
				httpService = getService(HttpService.class);
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				TerminalService terminalService =  getService(TerminalService.class);
				CacheServiceManager cacheServiceManager = getService(CacheServiceManager.class);
				OnlineService onlineService = getService(OnlineService.class);
				
				upload = new FotaFilterServiceImpl(actionService, onlineService, logService,cacheServiceManager,terminalService, getProperty("user.dir"));
				Logger log = logService.getLogger(this.getClass());
				log.debug("监听url：" + url);
				if (httpService instanceof ExtHttpService) {
					try {
						((ExtHttpService) httpService).registerFilter(upload, "/fota/.*", null, 0, httpService.createDefaultHttpContext());
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				upload.updated(props);
			}
		}, HttpService.class, ActionService.class, LogService.class, CacheServiceManager.class, TerminalService.class,OnlineService.class);
	}
	
	@Override
	public void stop() {}
}
