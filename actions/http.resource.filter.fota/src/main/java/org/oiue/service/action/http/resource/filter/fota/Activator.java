package org.oiue.service.action.http.resource.filter.fota;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.action.http.resource.ResourceFilterManger;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.terminal.TerminalService;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private ResourceFilterManger rfm ;
			private FotaFilterServiceImpl upload;
			
			@Override
			public void removedService() {
				rfm.unregisterResourceFilter("HttpFotaFilterService");
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				TerminalService terminalService = getService(TerminalService.class);
				CacheServiceManager cacheServiceManager = getService(CacheServiceManager.class);
				OnlineService onlineService = getService(OnlineService.class);
				
				upload = new FotaFilterServiceImpl(actionService, onlineService, logService, cacheServiceManager, terminalService, getProperty("user.dir"));
				rfm = getService(ResourceFilterManger.class);
				rfm.registerResourceFilter("HttpFotaFilterService", upload, 2);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				upload.updated(props);
			}
		}, HttpService.class, ActionService.class, LogService.class, CacheServiceManager.class, TerminalService.class, OnlineService.class,ResourceFilterManger.class);
	}
	
	@Override
	public void stop() {}
}
