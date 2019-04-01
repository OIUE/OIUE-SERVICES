package org.oiue.service.action.http.imageCode;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/CheckCodeImage";
			private HttpService httpService;
			private ImageCodeServlet imageCode;
			private ActionService actionService;
			
			@Override
			public void removedService() {
				httpService.unregister(url);
				actionService.unregisterActionFilter("imageCode");
			}
			
			@Override
			public void addingService() {
				httpService = getService(HttpService.class);
				LogService logService = getService(LogService.class);
				actionService = getService(ActionService.class);
				
				actionService.registerActionFilter("imageCode", new ImageCodeFilter(), -502);
				imageCode = new ImageCodeServlet(logService);
				Logger log = logService.getLogger(this.getClass());
				log.debug("绑定url：" + url);
				try {
					httpService.registerServlet(url, imageCode, null, null);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				imageCode.updated(props);
			}
		}, HttpService.class, ActionService.class, LogService.class);
	}
	
	@Override
	public void stop() {}
}
