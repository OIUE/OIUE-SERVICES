/**
 *
 */
package org.oiue.service.action.filter.permission;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.permission.PermissionServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class PermissionFilterServiceImpl implements ActionFilter, Serializable {
	private Logger logger;
	private OnlineService onlineService = null;
	private PermissionServiceManager permissionService = null;
	
	public PermissionFilterServiceImpl(LogService logService, OnlineService onlineService, PermissionServiceManager permissionService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.permissionService = permissionService;
		this.onlineService = onlineService;
		actionService.registerActionFilter("permissionFilter", this, 1);
	}
	
	public void updated(Map dict) {}
	
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult afr = permissionService.convert(per);
		if (StatusResult._SUCCESS == afr.getResult()) {
			Map cie = (Map) per.get(PermissionConstant.permission_key);
			if (cie == null) {
				afr.setResult(StatusResult._url_can_not_found);
			} else if (MapUtil.getInt(cie, "op_auth") == 0) { // 不需要权限
			} else {
				String token = MapUtil.getString(per, "token");
				if (StringUtil.isEmptys(token)) {
					throw new OIUEException(StatusResult._pleaseLogin, "请登录！");
				}
				if (!onlineService.isOnlineByToken(token)) {
					throw new OIUEException(StatusResult._pleaseReLogin, "Tokenid is expired");
				} else {
					afr = permissionService.verify(per, onlineService.getOnlineByToken(token));
				}
			}
		}
		if (logger.isDebugEnabled()) {
			
		}
		return afr;
	}
}
