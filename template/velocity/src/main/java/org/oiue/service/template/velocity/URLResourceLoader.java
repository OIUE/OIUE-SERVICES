package org.oiue.service.template.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.oiue.service.log.Logger;
import org.osgi.service.http.HttpContext;

@SuppressWarnings({"unchecked","rawtypes"})
public class URLResourceLoader extends ResourceLoader {
    static Logger logger;
    static HttpContext httpContext;
    private final String uuid = UUID.randomUUID().toString();
    private Map urlMap = new HashMap();

    public void init(ExtendedProperties configuration) {
        if(logger.isDebugEnabled()){
            logger.debug("["+this.uuid+"]init:"+configuration);
        }
    }

    public synchronized InputStream getResourceStream(String resourceName) throws ResourceNotFoundException {
        try {
            if(logger.isDebugEnabled()){
                logger.debug("["+this.uuid+"]getResourceStream:"+resourceName+""+this.rsvc);
            }
            if("VM_global_library.vm".equals(resourceName))
                return null;
            URL url = getURL(resourceName);

            if (url == null) {
                throw new ResourceNotFoundException("Can not find resource: " + resourceName);
            }
            return url.openStream();
        } catch (IOException e) {
            throw new ResourceNotFoundException("Can not find resource: " + resourceName + " - Reason: " + e.getMessage(),e);
        }
    }

    public long getLastModified(Resource res) {
        try {
            URL url = getURL(res.getName());
            long lm = url.openConnection().getLastModified();
            return lm;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    public boolean isSourceModified(Resource res) {
        long lastModified = getLastModified(res);
        return (lastModified != res.getLastModified());
    }

    private URL getURL(String resourceName) {
        if (urlMap.containsKey(resourceName)) {
            return (URL) urlMap.get(resourceName);
        }

        URL url = httpContext.getResource(resourceName);

        if (url != null) {
            urlMap.put(resourceName, url);
        }

        return url;
    }
}
