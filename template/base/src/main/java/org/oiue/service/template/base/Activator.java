package org.oiue.service.template.base;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.template.TemplateService;
import org.oiue.service.template.TemplateServiceManager;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {

            @Override
            public void removedService() {
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                TemplateServiceManager templateServiceManager = new TemplateServiceManagerImpl(logService);
                registerService(TemplateServiceManager.class, templateServiceManager);
                registerService(TemplateService.class, templateServiceManager);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {

            }
        }, LogService.class);
    }

    @Override
    public void stop() throws Exception {}
}
