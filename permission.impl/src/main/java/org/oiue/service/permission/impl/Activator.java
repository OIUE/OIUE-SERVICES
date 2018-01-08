package org.oiue.service.permission.impl;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.permission.PermissionServiceManager;

public class Activator extends FrameActivator {

    @Override
    public void start()  {
        this.start(new MulitServiceTrackerCustomizer() {
            private PermissionServiceManagerImpl permissionServiceManager;

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);

                permissionServiceManager = new PermissionServiceManagerImpl(logService);
                registerService(PermissionServiceManager.class, permissionServiceManager);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                permissionServiceManager.updated(props);
            }
        }, LogService.class);
    }

    @Override
    public void stop()  {}
}
