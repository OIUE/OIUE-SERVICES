package org.oiue.service.action.http.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.template.TemplateService;
import org.osgi.service.http.HttpContext;
//extends ServletContextImpl
public class ResourceContext  implements HttpContext {
	HttpContext base;
	Logger logger;
	TemplateService templateService;
	private String root_path;
	
	public ResourceContext(TemplateService templateService, LogService logService, HttpContext base, String root_path) {
		this.logger = logService.getLogger(getClass());
		this.base = base;
		this.templateService = templateService;
		this.root_path = root_path;
		
	}
	
	@Override
	public String getMimeType(String name) {
		try {
			String type = this.base.getMimeType(name);
			if (type == null) {
				type = MimeTypes.get().getByFile(name);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("ManagedContext getMimeType:" + name + ":" + type);
			}
			return type;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public URL getResource(String path) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("ManagedContext getResource:" + path);
			}
			if (path != null)
				path = path.trim();
			
			URL url = new File(root_path + path).toURI().toURL();
			return url;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			if (request instanceof ServletRequestWrapper) {
				ServletRequestWrapper srw = (ServletRequestWrapper) request;
				Request req = (Request) srw.getRequest();
				String path = req.getPathInfo();
				if (logger.isDebugEnabled()) {
					logger.debug("ManagedContext handleSecurity:" + path);
				}
				if ("/".equals(path)) {
					response.sendRedirect(path + (path.endsWith("/") ? "index.html" : "/index.html"));
					return false;
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return true;
	}
	
}
