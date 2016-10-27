package org.oiue.service.template;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

public interface TemplateService extends Serializable {

    public StatusResult render(String path,Map<String,?> parameter);
    
}
