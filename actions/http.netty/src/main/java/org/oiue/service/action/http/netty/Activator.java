package org.oiue.service.action.http.netty;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
//			private String url = getProperty("org.oiue.service.action.http.root") + "/action";
			
			@Override
			public void removedService() {
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				
				Logger log = logService.getLogger(this.getClass());
				if (log.isInfoEnabled()) {
				}
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			}
		},  ActionService.class, LogService.class);
	}
	
	@Override
	public void stop() {}
}
