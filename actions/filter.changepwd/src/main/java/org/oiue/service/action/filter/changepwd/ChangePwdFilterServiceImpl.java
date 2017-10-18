/**
 *
 */
package org.oiue.service.action.filter.changepwd;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Map;

import org.oiue.service.action.api.ActionResultFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class ChangePwdFilterServiceImpl implements ActionResultFilter, Serializable {
	@SuppressWarnings("unused")
	private Logger logger;
	private ActionService actionService = null;

	public ChangePwdFilterServiceImpl(LogService logService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
	}

	public void updated(Dictionary dict) {
		actionService.registerActionResultFilter("changePwdFilter", this, 10);
	}

	@Override
	public StatusResult doFilter(Map per, Object source_data) {
		String modulename = MapUtil.getString(per, "modulename");
		String operation = MapUtil.getString(per, "operation");

		modulename = modulename == null ? "" : modulename.trim();
		operation = operation == null ? "" : operation.trim();

		StatusResult afr = new StatusResult();
		if (modulename == null||operation == null) {
			afr.setResult(StatusResult._ncriticalAbnormal);
			afr.setDescription("error request!");
			return afr;
		}
		if("d3544810-973d-4b1a-8e65-6283a7964a69".equals(operation)){
			Map data = (Map) per.get("data");
			if(data==null||data.size()==0||MapUtil.getInt(data,"count")!=1){
				afr.setResult(StatusResult._ncriticalAbnormal);
				afr.setDescription("password is error！");
				return afr;
			}
		}

		afr.setResult(StatusResult._SUCCESS);
		afr.setDescription("validate success");
		per.put("success", true);
		return afr;
	}
}
