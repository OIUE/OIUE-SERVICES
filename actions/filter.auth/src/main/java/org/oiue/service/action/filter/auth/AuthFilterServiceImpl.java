/**
 * 
 */
package org.oiue.service.action.filter.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
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
        unFilter_modulename = new ArrayList<String>();
        String unFilter_modulenames = (String) dict.get("unFilter_modulename");
        if (!StringUtil.isEmptys(unFilter_modulenames)) {
            unFilter_modulename = StringUtil.Str2List(unFilter_modulenames, ",");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updateConfigure: unFilter_modulename  = " + unFilter_modulenames);
        }
        actionService.registerActionFilter("authFilter", this, 1);
    }

    @Override
    public StatusResult doFilter(Map per) {
        String modulename = MapUtil.getString(per, "modulename");
        String tokenid = MapUtil.getString(per, "tokenid");

        modulename = modulename == null ? "" : modulename.trim();
        tokenid = StringUtil.isEmptys(tokenid) ? null : tokenid.trim();

        StatusResult afr = new StatusResult();
        Map data = (Map) per.get("data");
        if (modulename == null || (data == null && !"logout".equals(modulename))) {
            afr.setResult(StatusResult._ncriticalAbnormal);
            afr.setDescription("error request data!");
            return afr;
        }
        Online online = null;
        if (unFilter_modulename != null && unFilter_modulename.size() > 0 && unFilter_modulename.contains(modulename)) {
            logger.info("un_Filter_modulename:" + modulename);
            return permissionService.convert(per);
        } else if (tokenid == null || "login".equals(modulename)) {
            if (!StringUtil.isEmptys(tokenid) && onlineService.isOnlineByToken(tokenid)) {
                online = onlineService.getOnlineByToken(tokenid);
                data.clear();
                data.put("tokenid", online.getToken());
                afr.setResult(StatusResult._SUCCESS_OVER);
                afr.setDescription("login success");
                return afr;
            } else {
                try {
                    online = authService.login(data);
                } catch (Exception e) {
                    logger.error("login status error,please login again:" + e.getMessage(), e);
                    afr.setResult(StatusResult._pleaseLogin);
                    afr.setDescription("login status error,please login again!");
                    return afr;
                }

                if (online == null || StringUtil.isEmptys(online.getToken())) {
                    afr.setResult(StatusResult._ncriticalAbnormal);
                    afr.setDescription(online == null ? "error login, please login again " : online.getO() + "");
                    return afr;
                }

                per.put("tokenid", online.getToken());
                onlineService.putOnline(online.getToken(), online);
            }
            if ("login".equals(modulename)) {
                afr.setResult(StatusResult._SUCCESS_OVER);
                afr.setDescription("login success");
                return afr;
            }
            afr = permissionService.verify(per, online);
            if (afr.getResult() < StatusResult._permissionDenied)
                return afr;
            return permissionService.convert(per);
        } else if ("logout".equals(modulename)) {
            authService.logout(data);
            onlineService.removeOnlineByToken(tokenid);
            afr.setResult(StatusResult._SUCCESS_OVER);
            afr.setDescription("logout success");
            per.put("success", true);
            return afr;
        } else {
            if (!onlineService.isOnlineByToken(tokenid)) {
                afr.setResult(StatusResult._pleaseLogin);
                afr.setDescription("Tokenid is expired");
                return afr;
            } else {
                online = onlineService.getOnlineByToken(tokenid);
                afr = permissionService.verify(per, online);
                if (afr.getResult() < StatusResult._permissionDenied)
                    return afr;
                return permissionService.convert(per);
            }
        }
    }
}
