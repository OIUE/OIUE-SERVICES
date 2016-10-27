package org.oiue.service.event.execute;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EventExecuteService extends Serializable {
    Object execute(Map data,Map event,String tokenid) throws Throwable;
}
