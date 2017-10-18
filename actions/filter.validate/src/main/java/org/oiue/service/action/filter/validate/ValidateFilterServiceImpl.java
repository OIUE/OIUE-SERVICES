/**
 *
 */
package org.oiue.service.action.filter.validate;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class ValidateFilterServiceImpl implements ActionFilter, Serializable {
	private Logger logger;
	private Map<String, Map<String,List<Map<String,Object>>>> validates = new HashMap<>();
	private ActionService actionService = null;

	public ValidateFilterServiceImpl(LogService logService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
	}

	public void updated(Dictionary dict) {
		String validatesStr = (String) dict.get("validates");

		if(!StringUtil.isEmptys(validatesStr))
			try {
				Map validatest = JSONUtil.parserStrToMap(validatesStr);
				validates=validatest;
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		if (logger.isDebugEnabled()) {
			logger.debug("updateConfigure: validatesStr  = " + validatesStr);
		}
		actionService.registerActionFilter("validateFilter", this, 10);
	}

	@Override
	public StatusResult doFilter(Map per) {
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
		Object data =  per.get("data");
		if(data == null){
			data = new HashMap<>();
			per.put("data", data);
		}
		Map<String,List<Map<String,Object>>> validate = validates.get(modulename+"/"+operation);
		if(validate!=null&&validate.size()>0){
			for (Map.Entry<String, List<Map<String,Object>>> entry : validate.entrySet()) {
				Object value = ((Map)data).get(entry.getKey());
				List<Map<String,Object>> validates = entry.getValue();
				for (Map<String, Object> map : validates) {
					switch ((map.get("type")+"").toLowerCase()) {
					case "notnull":
						if(value==null||StringUtil.isEmptys(value+"")){
							afr.setResult(StatusResult._ncriticalAbnormal);
							afr.setDescription(map.get("msg")+"");
							return afr;
						}
						break;
					case "length":
						if(value==null||StringUtil.isEmptys(value+"")){
						}else{
							if(value.toString().length()>MapUtil.getInt(map, "max")||value.toString().length()<MapUtil.getInt(map, "min")){
								if(value==null||StringUtil.isEmptys(value+"")){
									afr.setResult(StatusResult._ncriticalAbnormal);
									afr.setDescription(map.get("msg")+"");
									return afr;
								}
							}
						}
						break;
					case "pattern":
						Pattern p = Pattern.compile(map.get("pattern")+"");
						Matcher m = p.matcher("aaaaab");
						if(!m.matches()){
							afr.setResult(StatusResult._ncriticalAbnormal);
							afr.setDescription(map.get("msg")+"");
							return afr;
						}
						break;

					default:
						break;
					}
				}
			}
		}
		afr.setResult(StatusResult._SUCCESS);
		afr.setDescription("validate success");
		per.put("success", true);
		return afr;
	}
}
