package org.oiue.service.action.filter.module2domain;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

	@Override
	public void start() throws Exception {
		this.start(new MulitServiceTrackerCustomizer() {
			private AuthFilterServiceImpl actionFilter;

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);

				actionFilter = new AuthFilterServiceImpl(logService, actionService);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				actionFilter.updated(props);
			}
		}, LogService.class, ActionService.class);
	}

	@Override
	public void stop() throws Exception {}
}
