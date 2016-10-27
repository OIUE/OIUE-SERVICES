package org.oiue.service.driver.filter.impl;

import java.util.Dictionary;

import org.oiue.service.driver.api.DriverFilterService;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private DriverFilterServiceImpl driverFilterService;
            private DriverService driverService;

            @Override
            public void removedService() {
                driverFilterService.unregisterAllReceiveFilter();
                driverFilterService.unregisterAllSendFilter();
                driverService.unregisterFilterService(driverFilterService);
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                driverService = getService(DriverService.class);

                driverFilterService = new DriverFilterServiceImpl(logService);
                driverService.registerFilterService(driverFilterService, null);
                registerService(DriverFilterService.class, driverFilterService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                driverFilterService.updated(props);
            }
        }, LogService.class, DriverService.class);
    }

    @Override
    public void stop() throws Exception {}
}
