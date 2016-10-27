package org.oiue.service.template.base;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.template.TemplateService;
import org.oiue.service.template.TemplateServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({"serial","unchecked"})
public class TemplateServiceManagerImpl implements TemplateServiceManager {
    private Logger logger;
    private String template_type = "templateType";
    private String template_default = "velocity";

    private Map<String, TemplateService> templates = new HashMap<>();
    
    public TemplateServiceManagerImpl(LogService logService) {
        logger = logService.getLogger(getClass());
    }
    public void updated(Dictionary<String, ?> props) {
        String template_type = props.get("templateType") + "";
        if (!StringUtil.isEmptys(template_type)) {
            this.template_type = template_type;
        }
        String template_default = props.get("localTemplate") + "";
        if (!StringUtil.isEmptys(template_default)) {
            this.template_default = template_default;
        }
    }
    @Override
    public StatusResult render(String path, Map<String, ?> parameter) {
        String template_name = MapUtil.getString((Map<String, Object>) parameter, template_type);
        if(StringUtil.isEmptys(template_name)){
            template_name= template_default;
        }
        TemplateService template = this.getTemplateService(template_name);
        if(template==null){
            String msg = "the key[" + template_name + "] template service not find!";
            logger.error(msg + ":" + parameter);
            throw new RuntimeException(msg);
        }
        
        return template.render(path, parameter);
    }

    @Override
    public boolean registerTemplateService(String name, TemplateService template) {
        if (templates.containsKey(name)) {
            return false;
        } else {
            templates.put(name, template);
        }
        return true;
    }

    @Override
    public boolean unRegisterTemplateService(String name) {
        if (templates.containsKey(name)) {
            templates.remove(name);
            return true;
        } else
            return false;
    }
    @Override
    public TemplateService getTemplateService(String name) {
        return templates.get(name);
    }

}
