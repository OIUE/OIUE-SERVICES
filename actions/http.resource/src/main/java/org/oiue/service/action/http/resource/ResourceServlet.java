package org.oiue.service.action.http.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

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
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.string.StringUtil;
import org.osgi.service.http.HttpService;

public class ResourceServlet implements Servlet,ResourceFilterManger {
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
	private Map<String, ResourceFilter> beforeResourceFilter = new HashMap();
	private Map<Integer, String> beforeFilterSort = new TreeMap();
	
	private Map<String, ResourceResultFilter> afterResourceFilter = new HashMap();
	private Map<Integer, String> afterFilterSort = new TreeMap();
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		{
			StatusResult afr = beforeResourceFilter(request, response);
			if (afr.getResult() == StatusResult._SUCCESS_OVER || afr.getResult() < StatusResult._NoncriticalAbnormal) {
				return;
			}
		}
		
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
				logger.error("FileNotFoundException:" + request, e);
				fv = new FileVisit(logService);
				request.setAttribute("domain_path", "/comm");
				request.setAttribute("resName", "notfound.html");
				fv.visit(request, response);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private StatusResult beforeResourceFilter(HttpServletRequest request, HttpServletResponse response) {
		StatusResult afr = new StatusResult();
		afr.setResult(StatusResult._SUCCESS);
		
		for (ResourceFilter afilter : beforeResourceFilter.values()) {
			afr = afilter.doFilter(request,response);
			if (afr.getResult() == StatusResult._SUCCESS_OVER) {
				break;
			} else if (afr.getResult() == StatusResult._SUCCESS) {
				continue;
			} else if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
				break;
			}
		}
		return afr;
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
	
	public void updated(Map<String, ?> props) {}
	
	
	public synchronized boolean registerResourceFilter(String requestAction, ResourceFilter ResourceFilter, int index) {
		if (beforeFilterSort.get(index) != null) {
			throw new OIUEException(StatusResult._blocking_errors, "index conflict! name=" + requestAction + ", old index is " + beforeFilterSort.get(index)+",list:"+beforeFilterSort);
		}
		if (beforeResourceFilter.get(requestAction) == null) {
			ResourceFilter.setServletContext(getServletContext());
			
			beforeResourceFilter.put(requestAction, ResourceFilter);
			beforeFilterSort.put(index, requestAction);
			
			Map<String, ResourceFilter> actionPoolFilterTemp = new LinkedHashMap<String, ResourceFilter>();
			for (Iterator iterator = beforeFilterSort.values().iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				actionPoolFilterTemp.put(value, beforeResourceFilter.get(value));
			}
			beforeResourceFilter = actionPoolFilterTemp;
			return true;
		}
		return false;
	}
	
	public void unregisterResourceFilter(String requestAction) {
		ResourceFilter ResourceFilter = beforeResourceFilter.remove(requestAction);
		for (Iterator iterator = beforeFilterSort.values().iterator(); iterator.hasNext();) {
			String requestActions = (String) iterator.next();
			if (requestAction.equals(requestActions))
				iterator.remove();
		}
	}
	
	public synchronized boolean registerResourceResultFilter(String requestAction, ResourceResultFilter ResourceResultFilter, int index) {
		
		if (afterFilterSort.get(index) != null) {
			throw new RuntimeException("index conflict! name=" + requestAction + ", old index is " + afterFilterSort.get(index));
		}
		if (afterResourceFilter.get(requestAction) == null) {
			afterResourceFilter.put(requestAction, ResourceResultFilter);
			afterFilterSort.put(index, requestAction);
			
			Map<String, ResourceResultFilter> actionPoolFilterTemp = new LinkedHashMap<String, ResourceResultFilter>();
			for (Iterator iterator = afterFilterSort.values().iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				actionPoolFilterTemp.put(value, afterResourceFilter.get(value));
			}
			afterResourceFilter = actionPoolFilterTemp;
			return true;
		}
		return false;
	}
	
	public void unregisterResourceResultFilter(String requestAction) {
		ResourceResultFilter ResourceFilter = afterResourceFilter.remove(requestAction);
		for (Iterator iterator = afterFilterSort.values().iterator(); iterator.hasNext();) {
			String requestActions = (String) iterator.next();
			if (requestAction.equals(requestActions))
				iterator.remove();
		}
	}
	
	public Map<String, ResourceFilter> getBeforeResourceFilterPool() {
		return beforeResourceFilter;
	}
	
	private static Map sortByComparator(Map unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());
		
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		
		// put sorted list into map again
		// LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
