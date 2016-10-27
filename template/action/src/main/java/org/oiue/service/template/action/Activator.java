package org.oiue.service.template.action;

import java.util.Dictionary;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.template.TemplateService;
import org.osgi.service.http.HttpService;

@SuppressWarnings("deprecation")
public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {

            private HttpService httpService;
            private TemplateFilter filter;

            @Override
            public void removedService() {
                TemplateFilter.templateService = null;
                TemplateFilter.logger = null;
                if (httpService != null)
                    httpService.unregister("/");
                if (httpService instanceof ExtHttpService) {
                    ((ExtHttpService) httpService).unregisterFilter(filter);
                }
                httpService=null;
                filter=null;
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                Logger logger = logService.getLogger(getClass());
                TemplateService templateService = getService(TemplateService.class);

                httpService = getService(HttpService.class);
                TemplateFilter.templateService = templateService;
                TemplateFilter.logger = logService.getLogger(TemplateFilter.class);
                filter = new TemplateFilter();
                if (httpService instanceof ExtHttpService) {
                    try {
                        ((ExtHttpService) httpService).registerFilter(filter, "/.*", null, 1, httpService.createDefaultHttpContext());
                    } catch (ServletException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            @Override
            public void updated(Dictionary<String, ?> props) {

            }
        }, LogService.class, HttpService.class,TemplateService.class);
    }

    @Override
    public void stop() throws Exception {}
}
