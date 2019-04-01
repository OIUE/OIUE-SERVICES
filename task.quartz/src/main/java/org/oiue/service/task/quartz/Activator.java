package org.oiue.service.task.quartz;

import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.task.TaskService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			QuartzServiceImpl quartzService;
			
			@Override
			public void removedService() {
				quartzService.shutdown();
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				quartzService = new QuartzServiceImpl(logService);
				registerService(TaskService.class, quartzService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
