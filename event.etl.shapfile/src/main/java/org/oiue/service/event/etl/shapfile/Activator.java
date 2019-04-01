package org.oiue.service.event.etl.shapfile;

import java.util.Map;

import org.oiue.service.event.etl.impl.InputStepMetaManger;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				
				InputStepMetaManger.registerInputStepMeta("shapfile", new ShapInputStepMeta(logService));
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
