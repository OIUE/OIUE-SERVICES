package org.oiue.service.event.execute;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EventExecuteService extends Serializable {
	Object execute(Map data,Map event,String tokenid);
	Object execute(List data,Map event,String tokenid);
}
