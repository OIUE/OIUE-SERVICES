package org.oiue.service.cache.jedis;

import java.util.Dictionary;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private CacheServiceManager cacheServiceManager;
            private JedisCacheService cacheService;

            @Override
            public void removedService() {
                cacheServiceManager.unRegisterCacheService("redis");
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                cacheServiceManager = getService(CacheServiceManager.class);

                cacheService = new JedisCacheService(logService);
                cacheServiceManager.registerCacheService("redis", cacheService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                cacheService.updated(props);
            }
        }, LogService.class, CacheServiceManager.class);
    }

    @Override
    public void stop() throws Exception {}
}
