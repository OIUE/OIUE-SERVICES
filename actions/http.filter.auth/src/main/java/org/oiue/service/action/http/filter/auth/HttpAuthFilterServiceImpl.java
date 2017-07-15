/**
 *
 */
package org.oiue.service.action.http.filter.auth;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.file.MimeTypes;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class HttpAuthFilterServiceImpl implements Filter, Serializable {
	private Logger logger;
	private OnlineService onlineService = null;
	private CacheServiceManager cacheService = null;;

	private List<String> unFilter = new ArrayList<>();
	private String loginPage = "login.html";

	public HttpAuthFilterServiceImpl(LogService logService, OnlineService onlineService,CacheServiceManager cacheService) {
		logger = logService.getLogger(this.getClass());
		this.onlineService = onlineService;
		this.cacheService = cacheService;
	}

	public void updated(Dictionary dict) {
		String unFilter_modulenames = (String) dict.get("unfilter_file");
		if (!StringUtil.isEmptys(unFilter_modulenames)) {
			unFilter = StringUtil.Str2List(unFilter_modulenames, ",");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("updateConfigure: unFilter_modulename  = " + unFilter_modulenames);
		}
	}

	private FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			if (request instanceof ServletRequestWrapper) {
				HttpServletRequest req = (HttpServletRequest) ((ServletRequestWrapper) request).getRequest();
				String target = req.getPathInfo();
				String resName = target == null ? "" : target.startsWith("/") ? target.substring(1) : target;
				if(resName.startsWith("services/")){
					chain.doFilter(request, response);
					return;
				}
				if(resName.startsWith("system/console")){
					chain.doFilter(request, response);
					return;
				}
				String domain = req.getServerName();
				req.setAttribute("resName", resName);
				req.setAttribute("domain", domain);
				req.setAttribute("httpContext", request.getServletContext());
				String domain_path = (String) cacheService.get("system_domain", domain);
				if (domain_path != null)
					req.setAttribute("domain_path", domain_path);

				String mimeType = filterConfig.getServletContext().getMimeType(resName);
				if (mimeType == null)
					mimeType = MimeTypes.get().getByFile(resName);
				if (mimeType != null) {
					response.setContentType(mimeType);
				}

				if (!unFilter.contains(mimeType)) {

					Map menu = (Map) cacheService.get("system_menu", domain + ":" + resName);
					if (menu == null) {
						//					((HttpServletResponse) response).sendRedirect(notFoundPage);
						//					return;
						chain.doFilter(request, response);
						return;
					}
					domain_path = (String) menu.get("root_path");
					req.setAttribute("domain_path", menu.get("root_path"));
					req.setAttribute("type", menu.get("type"));

					int auth = MapUtil.getInt(menu, "auth");
					if (auth > 0 && !loginPage.equals(resName)) {
						String token = request.getParameter("token");
						HttpSession session = req.getSession();
						Online online = null;
						if (StringUtil.isEmptys(token)) {
							token = (String) session.getAttribute("token");
						}
						if (!StringUtil.isEmptys(token)) {
							try {
								online = onlineService.getOnlineByToken(token);
								if (online != null) {
									if (session.getAttribute("tokenId") == null) {
										session.setAttribute("tokenId", online.getTokenId());
										session.setAttribute("managed_online", online);
									}
									req.setAttribute("user_name", online.getUser_name());
									req.setAttribute("user_id", online.getUser_id());
									req.setAttribute("login_name", online.getUser().get("login_name"));
								}

							} catch (Exception e) {
								logger.error("token ："+e.getMessage(), e);
							}
						}
						if (online == null) {
							((HttpServletResponse) response).sendRedirect(loginPage);
							return;
						}
					}
				}
			}
			chain.doFilter(request, response);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {

	}
}
