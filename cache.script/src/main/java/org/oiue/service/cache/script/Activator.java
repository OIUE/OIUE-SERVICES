package org.oiue.service.cache.script;

import java.util.Dictionary;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start()  {
        this.start(new MulitServiceTrackerCustomizer() {

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                CacheServiceManager buffer = getService(CacheServiceManager.class);
                registerService(CacheScriptService.class, new CacheScriptServiceImpl(buffer));
            }

            @Override
            public void updated(Dictionary<String, ?> props) {

            }
        }, LogService.class, CacheServiceManager.class);
    }

    @Override
    public void stop()  {}
}
