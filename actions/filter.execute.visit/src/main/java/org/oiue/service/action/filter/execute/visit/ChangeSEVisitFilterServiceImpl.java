/**
 *
 */
package org.oiue.service.action.filter.execute.visit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.oiue.service.action.api.ActionResultFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class ChangeSEVisitFilterServiceImpl implements ActionResultFilter, Serializable {
	@SuppressWarnings("unused")
	private Logger logger;
	private String cacheType;
	private String cacheName;
	private ActionService actionService = null;
	private CacheServiceManager cacheServiceManager= null;

	public ChangeSEVisitFilterServiceImpl(LogService logService, ActionService actionService, CacheServiceManager cacheServiceManager) {
		logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
		this.cacheServiceManager = cacheServiceManager;
	}

	public void updated(Dictionary dict) {
		actionService.registerActionResultFilter("changeSEVisitFilter", this, 17);
		cacheType = MapUtil.getString(dict, "cacheType","storage");
		cacheName = MapUtil.getString(dict, "cacheName","1297676d-7610-4c31-a1e0-841f23de6ea7");
	}

	@Override
	public StatusResult doFilter(Map per, Object source_data) {
		String modulename = MapUtil.getString(per, "modulename");
		String operation = MapUtil.getString(per, "operation");

		modulename = modulename == null ? "" : modulename.trim();
		operation = operation == null ? "" : operation.trim();

		StatusResult afr = new StatusResult();
		if("execute".equals(modulename)){
			List pers = new ArrayList<>();
			pers.add(operation);
			cacheServiceManager.getCacheService(cacheType).put(cacheName, pers, Type.MANY);
		}

		afr.setResult(StatusResult._SUCCESS);
		afr.setDescription("validate success");
		per.put("success", true);
		return afr;
	}
}
