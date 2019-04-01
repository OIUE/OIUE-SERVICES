package org.oiue.service.driver.sms.alisms;

import java.util.Map;

import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() {
        this.start(new MulitServiceTrackerCustomizer() {
            private AliSMS leSMS;

            @Override
            public void removedService() {
                leSMS.unregistered();
            }

            @Override
            public void addingService() {
                DriverService smsService = getService(DriverService.class);
                LogService logService = getService(LogService.class);

                leSMS = new AliSMS(smsService, logService);
            }

            @Override
            public void updatedConf(Map<String, ?> props) {
                leSMS.updateConfigure(props);
            }
        },  LogService.class, DriverService.class);
    }

    @Override
    public void stop()  {}
}
