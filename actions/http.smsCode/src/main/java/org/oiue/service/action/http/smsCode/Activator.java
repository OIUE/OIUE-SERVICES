package org.oiue.service.action.http.smsCode;

import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverService driverService;
			private SmsCodeServiceImpl smsCodeCode;
			private SmsCodeFilter smsCodeFilter;
			
			@Override
			public void removedService() {
			}
			
			@Override
			public void addingService() {
				driverService = getService(DriverService.class);
				LogService logService = getService(LogService.class);
				FactoryService factoryService  = getService(FactoryService.class);
				ActionService actionService = getService(ActionService.class);
				CacheServiceManager cacheServiceManager = getService(CacheServiceManager.class);
				smsCodeFilter = new SmsCodeFilter(factoryService,cacheServiceManager);
				actionService.registerActionFilter("smsCode", smsCodeFilter, -501);
				smsCodeCode = new SmsCodeServiceImpl(logService,driverService,factoryService,cacheServiceManager);
				registerService(SmsCodeService.class, smsCodeCode);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				smsCodeCode.updated(props);
				smsCodeFilter.updated(props);
			}
		}, DriverService.class, ActionService.class, LogService.class,FactoryService.class,CacheServiceManager.class);
	}
	
	@Override
	public void stop() {}
}
