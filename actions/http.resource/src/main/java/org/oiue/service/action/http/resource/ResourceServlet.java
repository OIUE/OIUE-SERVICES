package org.oiue.service.action.http.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.ResourceBundle;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.template.TemplateService;
import org.oiue.tools.string.StringUtil;
import org.osgi.service.http.HttpService;

public class ResourceServlet implements Servlet {
	private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
	private ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

	private ServletConfig config;
	private TemplateService templateService;
	private FactoryService factoryService;
	private CacheServiceManager cacheService;

	private Logger logger;
	private LogService logService;

	public ResourceServlet(CacheServiceManager cacheService, OnlineService onlineService, FactoryService factoryService, LogService logService, TemplateService templateService, HttpService httpService) {
		this.factoryService = factoryService;
		this.templateService = templateService;
		this.cacheService = cacheService;
		this.logger = logService.getLogger(getClass());
		this.logService = logService;
	}

	@Override
	public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
		try {
			if (req instanceof ServletRequestWrapper) {
				service(((ServletRequestWrapper) req).getRequest(), res);
			} else {
				service((HttpServletRequest) req, (HttpServletResponse) res);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Visit fv = null;
			try {
				String type = (String) request.getAttribute("type");
				if (!StringUtil.isEmptys(type)) {
					fv = new TemplateVisit(factoryService, cacheService, templateService, logService);
				} else {
					fv = new FileVisit(logService);
				}
				fv.visit(request, response);
			} catch (FileNotFoundException e) {
				logger.error("FileNotFoundException:"+request, e);
				fv = new FileVisit(logService);
				request.setAttribute("domain_path","/comm");
				request.setAttribute("resName","notfound.html");
				fv.visit(request, response);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}

	@Override
	public String getServletInfo() {
		return "";
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public void destroy() {}

	public ServletContext getServletContext() {
		ServletConfig sc = getServletConfig();
		if (sc == null) {
			throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
		}
		return sc.getServletContext();
	}

	public void updated(Dictionary<String, ?> props) {}
}
