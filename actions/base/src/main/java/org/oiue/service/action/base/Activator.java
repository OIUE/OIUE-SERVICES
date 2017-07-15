package org.oiue.service.action.base;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

public class Activator extends FrameActivator {

	@Override
	public void start() throws Exception {
		FrameActivator tracker = this;
		this.start(new MulitServiceTrackerCustomizer() {
			private ActionServiceImpl actionService;

			@Override
			public void removedService() {
				actionService.unregisterAllActionFilter();
			}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				AnalyzerService analyzerService = getService(AnalyzerService.class);
				actionService = new ActionServiceImpl(logService, analyzerService, tracker);
				registerService(ActionService.class, actionService);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {

			}
		}, LogService.class, AnalyzerService.class);
	}

	@Override
	public void stop() throws Exception {

	}
}