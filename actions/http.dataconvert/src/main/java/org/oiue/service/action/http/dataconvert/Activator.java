package org.oiue.service.action.http.dataconvert;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.dataconvert.ConvertServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/dataconvert";
			private HttpService httpService;
			private PostServlet posServlet;
			
			@Override
			public void removedService() {
				httpService.unregister(url);
			}
			
			@Override
			public void addingService() {
				httpService = getService(HttpService.class);
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				ConvertServiceManager convertService = getService(ConvertServiceManager.class);
				
				posServlet = new PostServlet(actionService, logService,convertService);
				Logger log = logService.getLogger(this.getClass());
				log.debug("绑定url：" + url);
				try {
					httpService.registerServlet(url, posServlet, null, null);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				posServlet.updated(props);
			}
		}, HttpService.class, ActionService.class, LogService.class,ConvertServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
