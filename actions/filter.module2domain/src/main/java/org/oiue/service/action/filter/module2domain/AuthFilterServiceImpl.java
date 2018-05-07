/**
 *
 */
package org.oiue.service.action.filter.module2domain;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class AuthFilterServiceImpl implements ActionFilter, Serializable {
	private Logger logger;
	private Map<String, String> convert = new HashMap<>();
	private ActionService actionService = null;
	
	public AuthFilterServiceImpl(LogService logService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
	}
	
	public void updated(Dictionary dict) {
		String convert_url = (String) dict.get("convert");
		if (!StringUtil.isEmptys(convert_url)) {
			convert = StringUtil.parStr2Map(convert_url, ";", ":");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("updateConfigure: convert_url  = " + convert_url);
		}
		actionService.registerActionFilter("convertFilter", this, -1);
	}
	
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult afr = new StatusResult();
		String modulename = MapUtil.getString(per, "modulename");
		String operation = MapUtil.getString(per, "operation");
		
		modulename = modulename == null ? "" : modulename.trim();
		operation = operation == null ? "" : operation.trim();
		String key = modulename + "." + operation;
		if (convert.containsKey(key)) {
			String domain = convert.get(key);
			per.put("domain", domain);
			
			Map data = (Map) per.get("data");
			data.put("domain", domain);
		}
		
		return afr;
	}
}
