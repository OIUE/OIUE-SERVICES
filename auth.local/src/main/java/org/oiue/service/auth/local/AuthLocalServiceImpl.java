package org.oiue.service.auth.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.oiue.service.auth.AuthService;
import org.oiue.service.auth.AuthServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineImpl;
import org.oiue.service.online.Type;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AuthLocalServiceImpl implements AuthService, Serializable {
	private static final long serialVersionUID = -3485450639722467031L;
	private Logger logger;
	private String event_id;
	private FactoryService factoryService;
	private AuthServiceManager authServiceManager;
	protected String type = "local";
	private String name = "userName";
	private String pass = "userPass";
	private String corp_id = "corp_id";
	
	public AuthLocalServiceImpl(LogService logService, FactoryService iresource, AuthServiceManager authServiceManager) {
		logger = logService.getLogger(this.getClass().getName());
		this.factoryService = iresource;
		this.authServiceManager = authServiceManager;
	}
	
	public void updated(Map dict) {
		try {
			event_id = (String) dict.get("login.local.auth.eventId");
			name = (String) dict.get("login.local.key.name");
			pass = (String) dict.get("login.local.key.pass");
			corp_id = (String) dict.get("login.local.key.corp_id");
			String type = (String) dict.get("login.sso.type");
			if (!StringUtil.isEmptys(type) && !type.equals(this.type)) {
				authServiceManager.unRegisterAuthService(this.type);
				this.type = type;
			}
			authServiceManager.registerAuthService(type, this);
		} catch (Throwable e) {
			logger.error("config is error :" + dict, e);
		}
	}
	
	@Override
	public void unregister() {
		authServiceManager.unRegisterAuthService(type);
	}
	
	@Override
	public Online login(Map per) {
		String username = (String) per.remove(name);
		String password = (String) per.remove(pass);
		String corp_no = (String) per.remove(corp_id);
		String tokenId = null;
		Online online = new OnlineImpl();
		if (!StringUtil.isEmptys(username) && !StringUtil.isEmptys(password)) {
			Map<String, Object> map = new HashMap<>();
			map.put("origin_name", type);
			map.put("user_name", username);
			map.put("password", password);
			map.put("corp_id", corp_no);
			IResource iResource = factoryService.getBmo(IResource.class.getName());
			map = (Map<String, Object>) iResource.callEvent(event_id, null, map);
			if (map == null || map.size() == 0) {
				if(MapUtil.getString(map, "corp_id")!=null) {
					throw new OIUEException(StatusResult._login_error, "组织编码、用户名或密码不正确!");
				}else
					throw new OIUEException(StatusResult._login_error, "用户名或密码不正确!");
			}
			tokenId = UUID.randomUUID().toString().replaceAll("-", "");
			online.setO(new ConcurrentHashMap<>());
			online.setTokenId(tokenId);
			online.setType(Type.http);
			map.remove("password");
			online.setUser(map);
			online.setUser_id(map.get("user_id") + "");
			online.setUser_name(map.get("user_name") + "");
		}
		return online;
	}
	
	@Override
	public boolean logout(Map per) {
		return false;
	}
	
}
