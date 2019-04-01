package org.oiue.service.driver.httpaccess;

import java.util.Map;

import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;
/**
 * 接入服务
 * @author every
 */
public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/access";
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
				DriverService actionService = getService(DriverService.class);
				
				posServlet = new PostServlet(logService);
				Logger log = logService.getLogger(this.getClass());
				log.debug("绑定url：" + url);
				try {
					actionService.registerDriver(PostServlet.DriverName, posServlet);
					httpService.registerServlet(url, posServlet, null, null);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				posServlet.updated(props);
			}
		}, HttpService.class, DriverService.class, LogService.class);
	}
	
	@Override
	public void stop() {}
}
