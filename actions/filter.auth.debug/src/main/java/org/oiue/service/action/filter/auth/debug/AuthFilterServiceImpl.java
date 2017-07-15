/**
 *
 */
package org.oiue.service.action.filter.auth.debug;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineImpl;
import org.oiue.service.online.OnlineService;
import org.oiue.service.online.Type;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class AuthFilterServiceImpl implements ActionFilter, Serializable {
	private OnlineService onlineService = null;
	private FactoryService factoryService;

	public AuthFilterServiceImpl(LogService logService, OnlineService onlineService, ActionService actionService, FactoryService factoryService) {
		this.onlineService = onlineService;
		this.factoryService = factoryService;
		actionService.registerActionFilter("debugAuthFilter", this, 0);
	}

	@Override
	public StatusResult doFilter(Map per) {
		String tokenid = MapUtil.getString(per, "tokenid");
		boolean auto_login = false;
		if (per.containsKey("auto_login")) {
			auto_login = StringUtil.isTrue(MapUtil.getString(per, "auto_login"));
		}

		tokenid = StringUtil.isEmptys(tokenid) ? null : tokenid.trim();

		StatusResult afr = new StatusResult();
		Online online = null;

		try {
			IResource iresource = factoryService.getBmo(IResource.class.getName());

			if (!StringUtil.isEmptys(tokenid) && !onlineService.isOnlineByToken(tokenid)) {

				if (auto_login) {
					if ("17f3f93a-4580-11e5-b785-fa163e6f7961".equals(tokenid)) {
						String uid = "88888888";
						Map<String, Object> map = new HashMap<>();
						map.put("source_id", uid);
						map.put("origin_name", "letv");
						try {
							map = (Map<String, Object>) iresource.callEvent("fm_system_service_query_user", null, map);
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						online = new OnlineImpl();
						online.setTokenId(tokenid);
						online.setType(Type.http);
						online.setUser(map);
						online.setUser_id(map.get("user_id") + "");
						online.setUser_name(map.get("user_name") + "");

						onlineService.putOnline(online.getTokenId(), online);
					} else {
						String uid = "88888888";
						Map<String, Object> map = new HashMap<>();
						map.put("source_id", uid);
						map.put("origin_name", "letv");
						try {
							map = (Map<String, Object>) iresource.callEvent("fm_system_service_query_user", null, map);
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						online = new OnlineImpl();
						online.setTokenId(tokenid);
						online.setType(Type.http);
						map.put("user_id", tokenid);
						map.put("user_name", tokenid);
						online.setUser(map);
						online.setUser_id(map.get("user_id") + "");
						online.setUser_name(map.get("user_name") + "");

						onlineService.putOnline(online.getTokenId(), online);
					}
				}
			}
		} catch (Throwable e) {
			afr.setDescription(e.getMessage());
			afr.setResult(StatusResult._ncriticalAbnormal);
			return afr;
		}
		afr.setResult(StatusResult._SUCCESS);
		return afr;
	}
}
