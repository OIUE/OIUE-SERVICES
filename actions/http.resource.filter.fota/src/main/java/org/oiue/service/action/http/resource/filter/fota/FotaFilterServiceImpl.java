package org.oiue.service.action.http.resource.filter.fota;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.action.http.resource.ResourceFilter;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.terminal.TerminalService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.string.StringUtil;
import org.osgi.service.http.HttpService;

@SuppressWarnings({ "unused", "rawtypes" })
public class FotaFilterServiceImpl implements ResourceFilter, Serializable {
	private static final long serialVersionUID = -6327347468651806863L;
	private ActionService actionService;
	private OnlineService onlineService;
	private CacheServiceManager cacheServiceManager;
	private Logger logger;
	private Map properties;
	private String userDir = null;
	private boolean isMultipart = false;
	private HttpService httpService;
	private TerminalService terminalService;
	
	public FotaFilterServiceImpl(ActionService actionService, OnlineService onlineService, LogService logService, CacheServiceManager cacheServiceManager, TerminalService terminalService, String userDir) {
		super();
		this.logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
		this.onlineService = onlineService;
		this.userDir = userDir;
		this.cacheServiceManager = cacheServiceManager;
		this.terminalService = terminalService;
	}
	
	public void updated(Map props) {
		logger.info("updateConfigure");
		properties = props;
	}

	private ServletContext httpContext;

	public void setServletContext(ServletContext httpContext) {
		this.httpContext = httpContext;
	}
	@Override
	public StatusResult doFilter(HttpServletRequest request, HttpServletResponse response) {
		StatusResult sr = new StatusResult();
		sr.setResult(StatusResult._SUCCESS);
		try {
			if (request instanceof ServletRequestWrapper) {
				HttpServletRequest req = (HttpServletRequest) ((ServletRequestWrapper) request).getRequest();
				String target = req.getPathInfo();
				String resName = target == null ? "" : target.startsWith("/") ? target.substring(1) : target;
				logger.debug("target:{}", target);
				if (!resName.startsWith("fota/")) {
					return sr;
				}
				
				Map data = new HashMap<>();
				data.put(DriverDataField.type, req.getParameter("type"));
				data.put(DriverDataField.TERMINAL_SN, req.getParameter("identity"));
				Map nv = terminalService.getFOTAInfo(data, null, null);
				if (nv != null) {
					String domain = req.getServerName();
					logger.debug("domain:{}", domain);
					req.setAttribute("domain", domain);
					String domain_path = (String) cacheServiceManager.get("system_domain", domain);
					if (domain_path != null) {
						req.setAttribute("domain_path", domain_path);
					} else {
						req.setAttribute("domain_path", "/fota");
					}
					req.setAttribute("type", null);
					req.setAttribute("resName", nv.get("path"));
				} else {
					Map rtn = new HashMap<>();
					rtn.put("status", StatusResult._data_error);
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					String callBackFn = "";
					try {
						callBackFn = req.getParameter("callback") + "";
					} catch (Throwable e) {
						logger.error("处理Jsonp回调异常：" + e.getMessage(), e);
					}
					
					byte bytes[] = (StringUtil.isEmptys(callBackFn) ? JSONUtil.parserToStr(rtn) : (callBackFn + "(" + JSONUtil.parserToStr(rtn) + ")")).getBytes();
					response.setContentLength(bytes.length);
					
					OutputStream out = response.getOutputStream();
					out.write(bytes);
					
					out.flush();
					out.close();

					return sr;
				}
				
			}
			
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return sr;
	}
	
}
