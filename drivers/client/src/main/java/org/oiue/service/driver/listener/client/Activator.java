package org.oiue.service.driver.listener.client;

import java.util.Map;

import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;


public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			@Override
			public void removedService() {
			}
			
			@Override
			public void addingService() {
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			}
		});
	}
	
	@Override
	public void stop() {}
}
