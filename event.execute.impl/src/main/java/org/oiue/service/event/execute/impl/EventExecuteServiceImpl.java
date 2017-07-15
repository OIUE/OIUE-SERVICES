package org.oiue.service.event.execute.impl;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.execute.EventExecuteService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("serial")
public class EventExecuteServiceImpl implements EventExecuteService {

	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static CacheServiceManager cache;
	protected static OnlineService onlineService;
	protected static FactoryService factoryService ;
	private static String data_source_name = null;


	@SuppressWarnings({ "rawtypes",  "unchecked" })
	@Override
	public Object execute(Map data, Map event, String tokenId) throws Throwable {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}

}
