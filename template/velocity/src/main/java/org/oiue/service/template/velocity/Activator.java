package org.oiue.service.template.velocity;

import java.util.Dictionary;

import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.template.TemplateServiceManager;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private String name = "velocity";
            private VelocityService templateService;
            private TemplateServiceManager templateServiceManager;

            @Override
            public void removedService() {
                templateServiceManager.unRegisterTemplateService(name);
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                templateServiceManager = getService(TemplateServiceManager.class);
                URLResourceLoader.logger = logService.getLogger(URLResourceLoader.class);
                ResourceLoader resourceLoader = new URLResourceLoader();

                templateService = new VelocityService(logService, resourceLoader);
                templateServiceManager.registerTemplateService(name, templateService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                templateService.updated(props);
            }
        }, LogService.class, TemplateServiceManager.class);
    }

    @Override
    public void stop() throws Exception {}
}
