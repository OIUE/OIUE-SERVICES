/**
 *
 */
package org.oiue.service.action.filter.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.permission.PermissionServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class AuthFilterServiceImpl implements ActionFilter, Serializable {
	private Logger logger;
	private List<String> unFilter_modulename = null;
	private Map<String, List<String>> unFilter_module_operation = new HashMap<>();
	private OnlineService onlineService = null;
	private AuthServiceManager authService = null;
	private PermissionServiceManager permissionService = null;
	private ActionService actionService = null;

	public AuthFilterServiceImpl(LogService logService, OnlineService onlineService, PermissionServiceManager permissionService, AuthServiceManager authService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.authService = authService;
		this.permissionService = permissionService;
		this.actionService = actionService;
		this.onlineService = onlineService;
	}

	public void updated(Dictionary dict) {
		try {
			unFilter_modulename = new ArrayList<String>();
			String unFilter_modulenames = (String) dict.get("unFilter_modulename");
			if (!StringUtil.isEmptys(unFilter_modulenames)) {
				unFilter_modulename = StringUtil.Str2List(unFilter_modulenames, ",");
			}
			for (Iterator iterator = unFilter_modulename.iterator(); iterator.hasNext();) {
				String module = (String) iterator.next();
				try {
					String unFilter_modulename_operation = (String) dict.get(module);
					if (!StringUtil.isEmptys(unFilter_modulename_operation)) {
						unFilter_module_operation.put(module, StringUtil.Str2List(unFilter_modulename_operation, ","));
					}
				} catch (Throwable e) {}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("updateConfigure: unFilter_modulename  = " + unFilter_modulenames);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		actionService.registerActionFilter("authFilter", this, 1);
	}

	@Override
	public StatusResult doFilter(Map per) {
		String modulename = MapUtil.getString(per, "modulename");
		String token = MapUtil.getString(per, "token");

		modulename = modulename == null ? "" : modulename.trim();
		token = StringUtil.isEmptys(token) ? null : token.trim();

		StatusResult afr = new StatusResult();
		if (modulename == null) {
			afr.setResult(StatusResult._ncriticalAbnormal);
			afr.setDescription("error request data!");
			return afr;
		}
		Object data = per.get("data");
		if(data == null){
			data = new HashMap<>();
			per.put("data", data);
		}
		Online online = null;
		if (unFilter_modulename != null && unFilter_modulename.size() > 0 && unFilter_modulename.contains(modulename)) {
			logger.info("un_Filter_modulename:" + modulename);
			if("chat_execute".equals(modulename)){
				String event_id = MapUtil.getVauleMatchCase(per, "operation") + "";
				if(unFilter_module_operation.get(modulename)!=null&&!unFilter_module_operation.get(modulename).contains(event_id)){
					afr.setResult(StatusResult._permissionDenied);
					return afr;
				}
				Map tmp = new HashMap<>();
				tmp.put("serviceName","org.oiue.service.event.execute.EventExecuteService");
				tmp.put("methodName", "execute");
				tmp.put("component_instance_event_id",null);
				tmp.put("service_event_id", event_id);

				per.put(PermissionConstant.permission_key, tmp);
				afr.setResult(StatusResult._SUCCESS_CONTINUE);
				return afr;
			}else
				return permissionService.convert(per);
		} else if (token == null || "login".equals(modulename)) {

			if (!StringUtil.isEmptys(token) && onlineService.isOnlineByToken(token)) {
				online = onlineService.getOnlineByToken(token);
				((Map)data).clear();
				((Map)data).put("tokenid", online.getTokenId());
				afr.setResult(StatusResult._SUCCESS_OVER);
				afr.setDescription("login success");
				return afr;
			} else if("login".equals(modulename)||StringUtil.isTrue(MapUtil.getString(per, "auto_login","n"))){
				online = authService.login(((Map)data));

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
			}else{
				afr.setResult(StatusResult._pleaseLogin);
				afr.setDescription("please login！");
				return afr;
			}
			afr = permissionService.verify(per, online);
			if (afr.getResult() < StatusResult._permissionDenied||afr.getResult()>=StatusResult._SUCCESS_CONTINUE)
				return afr;
			return permissionService.convert(per);
		} else if ("logout".equals(modulename)) {
			onlineService.removeOnlineByToken(token);
			afr.setResult(StatusResult._SUCCESS_OVER);
			afr.setDescription("logout success");
			per.put("success", true);
			return afr;
		} else {
			if (!onlineService.isOnlineByToken(token)) {
				afr.setResult(StatusResult._pleaseLogin);
				afr.setDescription("Tokenid is expired");
				return afr;
			} else {
				online = onlineService.getOnlineByToken(token);
				afr = permissionService.verify(per, online);
				if (afr.getResult() < StatusResult._permissionDenied||afr.getResult()>=StatusResult._SUCCESS_CONTINUE)
					return afr;
				return permissionService.convert(per);
			}
		}
	}
}
