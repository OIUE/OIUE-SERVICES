package org.oiue.service.event.system.info;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EventSystemInfoService extends Serializable {
	Object getInfo(Map data, Map event, String tokenid);
	Object getRequest(Map data, Map event, String tokenid);
}
