package org.oiue.service.action.base;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

public class Activator extends FrameActivator {
	// private MulitServiceTracker tracker;
	//
	// @Override
	// public void start(BundleContext context) throws Exception {
	// String classNames[] = { LogService.class.getName(),
	// AnalyzerService.class.getName()};
	//
	// tracker = new MulitServiceTracker(context, classNames, new
	// SystemMulitServiceTrackerCustomizer() {
	// private ActionServiceImpl actionService;
	// private ServiceRegistration<?> serviceRegistration;
	// @Override
	// public void removedService(MulitServiceTracker tracker) {
	// actionService.unregisterAllActionFilter();
	// serviceRegistration.unregister();
	// }
	//
	// @Override
	// public void addingService(MulitServiceTracker tracker) {
	// LogService logService = tracker.getService(LogService.class.getName());
	// AnalyzerService analyzerService = tracker.
	// getService(AnalyzerService.class.getName());
	//
	// actionService = new
	// ActionServiceImpl(logService,analyzerService,tracker);
	//
	// serviceRegistration=tracker.getContext().registerService(ActionService.class.getName(),
	// actionService, null);
	// }
	// });
	//
	// tracker.open();
	// }
	//
	// @Override
	// public void stop(BundleContext context) throws Exception {
	// tracker.close();
	// }

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
		// TODO Auto-generated method stub

	}
}