package org.oiue.service.permission.verify;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.permission.PermissionServiceManager;

public class Activator extends FrameActivator {

    @Override
    public void start()  {
        this.start(new MulitServiceTrackerCustomizer() {
            PermissionServiceImpl permissionService;

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                PermissionServiceManager permissionServiceManager = (PermissionServiceManager) getService(PermissionServiceManager.class);
                FactoryService factoryService = getService(FactoryService.class);

                permissionService = new PermissionServiceImpl(logService, permissionServiceManager, factoryService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                permissionService.updated(props);
            }
        }, LogService.class, PermissionServiceManager.class, FactoryService.class);
    }

    @Override
    public void stop()  {}
}
