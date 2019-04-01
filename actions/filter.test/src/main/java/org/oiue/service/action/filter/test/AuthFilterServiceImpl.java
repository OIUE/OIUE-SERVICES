/**
 *
 */
package org.oiue.service.action.filter.test;

import java.io.Serializable;
import java.util.List;
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
	
	public AuthFilterServiceImpl(LogService logService,ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		actionService.registerActionFilter("testFilter", this, -900);
	}
	
	public void updated(Map dict) {}
	
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult afr = new StatusResult();
		String modulename = MapUtil.getString(per, "modulename");
		modulename = StringUtil.isEmptys(modulename) ? null : modulename.trim();
		if (modulename!=null&&modulename.startsWith("test")) {
			per.put("modulename", modulename.substring(4));
			Object data = per.get("data");
			if(data instanceof Map){
				((Map) data).put("system_test_execute", "Y");
			}else if(data instanceof List){
				for (Object o : (List)data) {
					if(o instanceof Map){
						((Map) o).put("system_test_execute", "Y");
					}
				}
			}
			
			logger.debug("test module:{}", per);
		}
		afr.setResult(StatusResult._SUCCESS);
		return afr;
	}
}
