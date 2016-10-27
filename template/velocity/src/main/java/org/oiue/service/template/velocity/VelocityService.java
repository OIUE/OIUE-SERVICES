package org.oiue.service.template.velocity;

import java.io.Writer;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.template.TemplateService;
import org.oiue.tools.StatusResult;
import org.osgi.service.http.HttpContext;

@SuppressWarnings("unused")
public class VelocityService implements TemplateService {
    private static final long serialVersionUID = 1L;
    private ResourceLoader resourceLoader;
    private LogService logService;
    private Logger logger;
    private VelocityEngine ve;

    public VelocityService(LogService logService, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.logService = logService;
    }

    @Override
    public StatusResult render(String path, Map<String, ?> parameter) {
        if (ve == null) {
            HttpContext httpContext = (HttpContext) parameter.get("httpContext");
            URLResourceLoader.httpContext = httpContext;
            ve = new VelocityEngine();
            Properties p = new Properties();
            p.setProperty("resource.loader", "urlrl");
            p.setProperty("urlrl.resource.loader.class", resourceLoader.getClass().getName());
            p.setProperty("input.encoding", "UTF-8");
            ve.init(p);
        }
        Template template = ve.getTemplate(path);
        VelocityContext context = new VelocityContext(parameter);
        template.merge(context, (Writer) parameter.get("system_writer"));
        StatusResult sr = new StatusResult();
        sr.setResult(StatusResult._SUCCESS);
        return sr;
    }

    public void updated(Dictionary<String, ?> props) {

    }

}
