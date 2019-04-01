package org.oiue.service.event.system.info.impl;

import java.util.HashMap;
import java.util.Map;

import org.oiue.service.event.system.info.EventSystemInfoService;
import org.oiue.service.log.Logger;

@SuppressWarnings("serial")
public class EventSystemInfoServiceImpl implements EventSystemInfoService {
	
	protected static Logger logger;
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getInfo(Map data, Map event, String tokenid) {
		Map info = new HashMap();
		info.put("version", "1.0.0");
		info.put("framework", "OIUE");
		return info;
	}

	@Override
	public Object getRequest(Map data, Map event, String tokenid) {
		return data;
	}
}
