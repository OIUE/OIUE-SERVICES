package org.oiue.service.action.http.resource;

import java.io.IOException;
import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.template.TemplateService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {

	@Override
	public void start() throws Exception {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/";
			private HttpService httpService;

			private ResourceServlet servlet;
			private TemplateService templateService;
			private LogService logService;
			private HttpContext httpContext;
			private Logger log;

			@Override
			public void removedService() {
				httpService.unregister(url);
			}

			@Override
			public void addingService() {
				httpService = getService(HttpService.class);
				logService = getService(LogService.class);
				templateService = getService(TemplateService.class);
				FactoryService factoryService = getService(FactoryService.class);
				CacheServiceManager cacheService = getService(CacheServiceManager.class);
				OnlineService onlineService = getService(OnlineService.class);

				log = logService.getLogger(this.getClass());
				servlet = new ResourceServlet(cacheService, onlineService, factoryService, logService,templateService, httpService);
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
				try {
					servlet.updated(props);
					log.info("绑定url：" + url);
					httpContext = new ResourceContext(templateService, logService, httpService.createDefaultHttpContext(), (String) props.get("root_path"));
					httpService.registerServlet(url, servlet, props, httpContext);
					httpService.registerServlet("/storefront/track", new Servlet() {

						@Override
						public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
							log.debug(req.toString());
						}

						@Override
						public void init(ServletConfig config) throws ServletException {

						}

						@Override
						public String getServletInfo() {
							return null;
						}

						@Override
						public ServletConfig getServletConfig() {
							return null;
						}

						@Override
						public void destroy() {

						}
					}, null, null);
					httpService.registerServlet("/storefront/page", new Servlet() {

						@Override
						public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
							log.debug(req.toString());
						}

						@Override
						public void init(ServletConfig config) throws ServletException {

						}

						@Override
						public String getServletInfo() {
							return null;
						}

						@Override
						public ServletConfig getServletConfig() {
							return null;
						}

						@Override
						public void destroy() {

						}
					}, null, null);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}, HttpService.class, TemplateService.class, LogService.class, FactoryService.class, OnlineService.class, CacheServiceManager.class);
	}

	@Override
	public void stop() throws Exception {}
}
