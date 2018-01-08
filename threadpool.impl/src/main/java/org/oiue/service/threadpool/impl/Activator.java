package org.oiue.service.threadpool.impl;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.threadpool.ThreadPoolService;

public class Activator extends FrameActivator {

    @Override
    public void start()  {
        this.start(new MulitServiceTrackerCustomizer() {
            private ThreadPoolService threadPoolService;
            Logger logger;

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                try {
                    LogService logService = getService(LogService.class);
                    logger = logService.getLogger(this.getClass());
                    threadPoolService = new ThreadPoolServiceImpl(logService);

                    registerService(ThreadPoolService.class, threadPoolService);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                try {
                    threadPoolService.updated(props);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, LogService.class);
    }

    @Override
    public void stop()  {}
}
