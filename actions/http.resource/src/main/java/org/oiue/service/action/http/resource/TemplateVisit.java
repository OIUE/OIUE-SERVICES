package org.oiue.service.action.http.resource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.service.action.http.services.ParseHtml;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.template.TemplateService;

public class TemplateVisit implements Visit{
	private static final long serialVersionUID = 1L;
	private FactoryService factoryService;
	private CacheServiceManager cacheService;
	private TemplateService templateService;

	public TemplateVisit(FactoryService factoryService, CacheServiceManager cacheService,TemplateService templateService,LogService logService) {
		this.factoryService = factoryService;
		this.cacheService = cacheService;
		this.templateService = templateService;
		this.logger = logService.getLogger(getClass());
	}
	private Logger logger;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void visit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map map = ParseHtml.parseRequest(request);
		Map data = null;
		try {
			data = (Map) map.get("data");
		} catch (Exception e) {}
		if(data == null)
			data=new HashMap<>();
		
		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String an = (String) attributeNames.nextElement();
			data.put(an, request.getAttribute(an));
		}
		
		String domain = (String) request.getAttribute("domain");
		String resName = (String) request.getAttribute("resName");
		String resNameK = domain+":"+resName;

		response.setCharacterEncoding("UTF-8");
		if (cacheService.contains("system_template",resNameK)) {
			Map parameter = new HashMap<>();
			parameter.put("token", map.get("token"));
			parameter.put("user_name", request.getAttribute("user_name"));
			parameter.put("login_name", request.getAttribute("login_name"));

			Map menu = (Map) cacheService.get("system_menu", resNameK);
			Map events = new HashMap<>();
			try {
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				data.put("model", "");
				data.put("domain", domain);
				data.put("user_id", request.getAttribute("user_id"));
				List<Map> attributes = (List<Map>) iresource.callEvent("fm_system_query_attribute", null, data);
				if (attributes != null)
					for (Map attribute : attributes) {
						if ("string".equals(attribute.get("type"))) {
							parameter.put(attribute.get("name"), attribute.get("value"));
						} else if ("event_id".equals(attribute.get("type"))) {
							events.put(attribute.get("name"), attribute.get("value"));
						}
					}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			try {
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				data.put("model", resName);
				data.put("domain", domain);
				data.put("user_id", request.getAttribute("user_id"));
				List<Map> attributes = (List<Map>) iresource.callEvent("fm_system_query_attribute", null, data);
				if (attributes != null) {
					for (Map attribute : attributes) {
						if ("string".equals(attribute.get("type"))) {
							parameter.put(attribute.get("name"), attribute.get("value"));
						} else if ("event_id".equals(attribute.get("type"))) {
							events.put(attribute.get("name"), attribute.get("value"));
						}
					}
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			try {
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				data.put("model", resName);
				data.put("domain", domain);
				data.put("user_id", request.getAttribute("user_id"));
				List<Map> attributes = (List<Map>) iresource.callEvent("fm_system_query_r_attribute", null, data);
				if (attributes != null) {
					for (Map attribute : attributes) {
						if ("string".equals(attribute.get("type"))) {
							parameter.put(attribute.get("name"), attribute.get("value"));
						} else if ("event_id".equals(attribute.get("type"))) {
							events.put(attribute.get("name"), attribute.get("value"));
						}
					}
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}

			try {
				LinkedList<Map.Entry<String, String>> tempList = new LinkedList<Map.Entry<String, String>>();
				tempList.addAll(events.entrySet());
				ListIterator<Map.Entry<String, String>> itor = tempList.listIterator();
				Map.Entry entry = null;

				while (itor.hasNext()) {
					entry = itor.next();
					IResource iresource = factoryService.getBmo(IResource.class.getName());
					data.put("model", resName);
					data.put("domain", domain);
					data.put("user_id", request.getAttribute("user_id"));
					if (menu != null)
						data.put("menu_id", menu.get("menu_id"));
					parameter.put(entry.getKey(), iresource.callEvent(entry.getValue() + "", null, data));
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}

			try {
				parameter.put("resName", resName);
				this.templateService.render(request, response, parameter);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			} finally {
			}
			return;
		}else{
			logger.warn("找不到模板配置！");
			this.templateService.render(request, response, new HashMap<>());
		}
	}
}
