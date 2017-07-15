package org.oiue.service.auth.local;

import java.util.Dictionary;

import org.oiue.service.auth.AuthService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            AuthLocalServiceImpl authService;

            @Override
            public void removedService() {
            	authService.unregister();
            }

            @SuppressWarnings("unused")
            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                FactoryService factoryService = getService(FactoryService.class);
                AuthServiceManager authServiceManager = getService(AuthServiceManager.class);
                IResource iResource = getService(IResource.class);

                authService = new AuthLocalServiceImpl(logService, factoryService, authServiceManager);

                registerService(AuthService.class, authService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                authService.updated(props);
            }
        }, LogService.class, AuthServiceManager.class, FactoryService.class,IResource.class);
    }

    @Override
    public void stop() throws Exception {}
}
