/**
 *
 */
package org.oiue.service.action.filter.auth;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class AuthFilterServiceImpl implements ActionFilter, Serializable {
	private Logger logger;
	private OnlineService onlineService = null;
	private AuthServiceManager auth = null;
	
	public AuthFilterServiceImpl(LogService logService, OnlineService onlineService, AuthServiceManager auth, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.onlineService = onlineService;
		this.auth = auth;
		actionService.registerActionFilter("authFilter", this, 0);
	}
	
	public void updated(Map dict) {}
	
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult afr = new StatusResult();
		String token = MapUtil.getString(per, "token");
		token = StringUtil.isEmptys(token) ? null : token.trim();
		String modulename = MapUtil.getString(per, "modulename");
		modulename = StringUtil.isEmptys(modulename) ? null : modulename.trim();
		Online online = null;
		if ("login".equals(modulename) || StringUtil.isTrue(MapUtil.getString(per, "auto_login", "n"))) {
			try{
				online = auth.login(((Map) per.get("data")));
			}catch (OIUEException e) {
				return e.getStatus();
			}
			
			if (online == null || StringUtil.isEmptys(online.getTokenId())) {
				afr.setResult(StatusResult._ncriticalAbnormal);
				afr.setDescription(online == null ? "error login, please login again " : online.getO() + "");
				return afr;
			}
			
			onlineService.putOnline(online.getTokenId(), online);
			per.put("data", online.getUser());
			per.put("token", online.getToken());
			if ("login".equals(modulename)) {
				afr.setResult(StatusResult._SUCCESS_OVER);
				afr.setDescription("login success");
				return afr;
			}
		} else if ("logout".equals(modulename)) {
			onlineService.removeOnlineByToken(token);
			afr.setResult(StatusResult._SUCCESS_OVER);
			afr.setDescription("logout success");
			per.put("success", true);
			return afr;
		}
		if (logger.isDebugEnabled()) {
			
		}
		afr.setResult(StatusResult._SUCCESS);
		return afr;
	}
}
