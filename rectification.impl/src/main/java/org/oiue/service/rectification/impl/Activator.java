package org.oiue.service.rectification.impl;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.rectification.RectificationService;
import org.oiue.service.rectification.RectificationServiceManager;

public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {
			private RectificationServiceManagerImpl rectificationServiceManager;

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);

				rectificationServiceManager = new RectificationServiceManagerImpl(logService);
				registerService(RectificationServiceManager.class, rectificationServiceManager);
				registerService(RectificationService.class, rectificationServiceManager);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				rectificationServiceManager.updated(props);
			}
		}, LogService.class);
	}

	@Override
	public void stop()  {}
}
