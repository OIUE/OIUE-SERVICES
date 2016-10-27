package org.oiue.service.template;

import java.io.Serializable;


public interface TemplateServiceManager extends Serializable,TemplateService {
    boolean registerTemplateService(String name,TemplateService template);
    boolean unRegisterTemplateService(String name);
    TemplateService getTemplateService(String name);
}
