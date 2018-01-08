package org.oiue.service.driver.listener.storage;

import java.util.Dictionary;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverListenerService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverListenerService driverListenerService;
			private DriverListenerStorageServiceImpl driverListener;

			@Override
			public void removedService() {
				driverListenerService.unregisterListener(driverListener);
			}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				IResource iResource = getService(IResource.class);
				AnalyzerService analyzerService = getService(AnalyzerService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);
				driverListenerService = getService(DriverListenerService.class);

				driverListener = new DriverListenerStorageServiceImpl(logService, iResource, analyzerService,cacheService);
				driverListenerService.registerListener(driverListener);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				driverListener.updated(props);
			}
		}, LogService.class, IResource.class, AnalyzerService.class, DriverListenerService.class,CacheServiceManager.class);
	}

	@Override
	public void stop()  {}
}
