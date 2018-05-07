package org.oiue.service.permission.verify;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.Online;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.permission.PermissionService;
import org.oiue.service.permission.PermissionServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "serial" })
public class PermissionServiceImpl implements PermissionService, Serializable {
	private Logger logger;
	private FactoryService factoryService;
	
	private PermissionServiceManager permissionServiceManager;
	private String permission_type = "def_permission";
	
	private String data_source_name = null;
	private String event_id = "fm_system_query_component_service";
	
	public PermissionServiceImpl(LogService logService, PermissionServiceManager permissionServiceManager, FactoryService factoryService) {
		logger = logService.getLogger(this.getClass());
		this.permissionServiceManager = permissionServiceManager;
		this.factoryService = factoryService;
	}
	
	@Override
	public void unregister() {
		permissionServiceManager.unRegisterPermissionService(this.permission_type);
	}
	
	public void updated(Dictionary<String, ?> props) {
		try {
			String permission_type = props.get("permissionKey") + "";
			if (!StringUtil.isEmptys(permission_type)) {
				this.permission_type = permission_type;
			}
			permissionServiceManager.registerPermissionService(this.permission_type, this);
		} catch (Throwable e) {
			logger.error("config[permissionKey] is error:" + e.getMessage(), e);
		}
		try {
			String event_id = props.get("service_event_id") + "";
			if (!StringUtil.isEmptys(event_id)) {
				this.event_id = event_id;
			}
		} catch (Throwable e) {
			logger.error("config[service_event_id] is error:" + e.getMessage(), e);
		}
		try {
			String data_source_name = props.get("data_source_name") + "";
			if (!StringUtil.isEmptys(data_source_name)) {
				this.data_source_name = data_source_name;
			}
		} catch (Throwable e) {
			logger.error("config[data_source_name] is error:" + e.getMessage(), e);
		}
	}
	
	@Override
	public StatusResult verify(Map per, Online online) {
		StatusResult afr = new StatusResult();
		String modulename = MapUtil.getVauleMatchCase(per, "modulename") + "";
		// String operation = MapUtil.getString(per, "operation");
		
		// Map data = (Map) per.get("data");
		
		if ("execute".equals(modulename)) {
			String event_id = MapUtil.getVauleMatchCase(per, "operation") + "";
			Map tmp = new HashMap<>();
			tmp.put("serviceName", "org.oiue.service.event.execute.EventExecuteService");
			tmp.put("methodName", "execute");
			tmp.put(EventField.service_event_id, event_id);
			
			per.put(PermissionConstant.permission_key, tmp);
			afr.setResult(StatusResult._SUCCESS_CONTINUE);
		} else
			afr.setResult(StatusResult._SUCCESS);
		return afr;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public StatusResult convert(Map per) {
		StatusResult afr = new StatusResult();
		String modulename = MapUtil.getVauleMatchCase(per, "modulename") + "";
		String operation = MapUtil.getVauleMatchCase(per, "operation") + "";
		if (!StringUtil.isEmptys(modulename) || !StringUtil.isEmptys(operation)) {
			Map<String, Object> temp = new HashMap<>();
			temp.put("modulename", modulename);
			temp.put("operation", operation);
			try {
				IResource iresource = factoryService.getBmo(IResource.class.getName());
				Object ro = iresource.callEvent(event_id, data_source_name, temp);
				if (ro == null)
					throw new RuntimeException("Data does not exist！");
				if (ro instanceof Map) {
					Map tmp = (Map) ro;
					if (tmp.size() == 0)
						throw new RuntimeException("Data does not exist！");
					tmp.put("serviceName", MapUtil.getVauleMatchCase(tmp, "bundle_service_id"));
					tmp.put("methodName", MapUtil.getVauleMatchCase(tmp, "name"));
					ro = tmp;
				}
				per.put(PermissionConstant.permission_key, ro);
			} catch (Throwable e) {
				String msg = "convert module to service event is error," + ExceptionUtil.getCausedBySrcMsg(e);
				logger.error("query[" + data_source_name + ":" + event_id + "] is error:" + msg, e);
				afr.setResult(StatusResult._ncriticalAbnormal);
				afr.setDescription(msg);
				return afr;
			}
		}
		afr.setResult(StatusResult._SUCCESS);
		return afr;
	}
}
