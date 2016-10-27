package org.oiue.service.auth.impl;

import java.util.Dictionary;

import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);

                AuthServiceManager authServiceManager = new AuthServiceManagerImpl(logService);
                registerService(AuthServiceManager.class, authServiceManager);

            }

            @Override
            public void updated(Dictionary<String, ?> props) {

            }
        }, LogService.class);
    }

    @Override
    public void stop() throws Exception {}
}
