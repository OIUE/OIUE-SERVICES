package org.oiue.service.permission.impl;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.permission.PermissionService;
import org.oiue.service.permission.PermissionServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "serial" })
public class PermissionServiceManagerImpl implements PermissionServiceManager, Serializable {
	private Logger logger;

	private String permission_type = "permission_type";
	private String permission_convert_type = "permission_convert_type";
	private String def_permission = "def_permission";
	private String def_permission_convert = "def_permission_convert";

	private Map<String, PermissionService> permission = new HashMap<>();

	public PermissionServiceManagerImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	public void updated(Dictionary<String, ?> props) {
		try {
			String def_permission = props.get("defaultPermission") + "";
			if (!StringUtil.isEmptys(def_permission)) {
				this.def_permission = def_permission;
			}
			String def_permission_convert = props.get("defaultPermissionConvert") + "";
			if (!StringUtil.isEmptys(def_permission_convert)) {
				this.def_permission_convert = def_permission_convert;
			}
			String permission_type = props.get("permissionKey") + "";
			if (!StringUtil.isEmptys(permission_type)) {
				this.permission_type = permission_type;
			}
			String permission_convert_type = props.get("permissionConvertKey") + "";
			if (!StringUtil.isEmptys(permission_convert_type)) {
				this.permission_convert_type = permission_convert_type;
			}

		} catch (Throwable e) {
			logger.error("config is error:" + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public StatusResult verify(Map per, Online online) {
		if (online == null) {
			StatusResult afr = new StatusResult();
			afr.setResult(StatusResult._ncriticalAbnormal);
			afr.setDescription("verify error!");
			return afr;
		}

		Object data = per.get("data");
		if(data instanceof Map){
			((Map)data).put("user_id", online.getUser_id());
			((Map)data).put("domain",per.get("domain"));
		}else if(data instanceof List){
			for (Map da : (List<Map>)data) {
				da.put("user_id", online.getUser_id());
				da.put("domain",per.get("domain"));
			}
			per.put("user_id", online.getUser_id());
			per.put("domain",per.get("domain"));
		}

		per.put("token", online.getToken());

		String type = (String) per.remove(permission_type);

		if (StringUtil.isEmptys(type)) {
			String msg = "the key[" + permission_type + "] con't null or empty!";
			if (logger.isWarnEnabled())
				logger.warn(msg + ":" + per);
			type = def_permission;
		}
		PermissionService auth = permission.get(type);
		per.put(PermissionConstant.permission_user_key, online);
		return auth.verify(per, online);
	}

	@Override
	public StatusResult convert(Map per) {
		Map data = (Map) per.get("data");

		String type = (String) data.remove(permission_convert_type);

		if (StringUtil.isEmptys(type)) {
			String msg = "the key[" + permission_convert_type + "] con't null or empty!";
			logger.warn(msg + ":" + per);
			type = def_permission_convert;
		}
		PermissionService auth = permission.get(type);
		return auth.convert(per);
	}

	@Override
	public boolean registerPermissionService(String name, PermissionService verify) {
		if (permission.containsKey(name)) {
			return false;
		} else {
			permission.put(name, verify);
			return true;
		}
	}

	@Override
	public boolean unRegisterPermissionService(String name) {
		if (permission.containsKey(name)) {
			permission.remove(name);
			return true;
		} else
			return false;
	}

	@Override
	public void unregister() {

	}
}
