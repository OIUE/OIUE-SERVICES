package org.oiue.service.template.beetl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.oiue.service.log.LogService;
import org.oiue.service.template.TemplateService;
import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public class BeetlService implements TemplateService {
    private static final long serialVersionUID = 1L;
    GroupTemplate gt;

    public BeetlService(LogService logService) {}

    public void updated(Dictionary dict) {
        try {
            ResourceLoader resourceLoader = new FileResourceLoader("/work/project");

            Properties p = new Properties();
            Enumeration keys =dict.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                p.setProperty(key, (String) dict.get(key));
            }
            Configuration cfg = new Configuration(p);
            gt = new GroupTemplate(resourceLoader, cfg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public StatusResult render(String path, Map<String, ?> parameter) {
        Template template = gt.getTemplate(path);
        List<Map> testList = new ArrayList<>();
        Map m1 = new HashMap();
        m1.put("image_url", "image_url.jpg");
        m1.put("title", "title");
        m1.put("content", "content---------------------------------------content");
        testList.add(m1);
        Map p = parameter;
        p.put("testList", testList);

        p.put("BASE_PATH", "BASE_PATH");
        p.put("HEAD_TITLE", "HEAD_TITLE");
        
        List aboutList = new ArrayList<>();
        Map ma = new HashMap<>();
        ma.put("name", "name");
        aboutList.add(ma);
        p.put("aboutList", aboutList);
        
        template.fastBinding(p);
        System.out.println(template.render());
        if(parameter.containsKey("system_out_type")){
            Object system_out_type = parameter.get("system_out_type");
            if(system_out_type instanceof OutputStream){
                OutputStream os = (OutputStream) system_out_type;
                template.renderTo(os);
            }
        }

        StatusResult sr = new StatusResult();
        sr.setResult(StatusResult._SUCCESS);
        return sr;
    }

}
