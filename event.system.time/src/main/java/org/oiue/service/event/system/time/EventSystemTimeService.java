package org.oiue.service.event.system.time;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EventSystemTimeService extends Serializable {
    Object getTime(Map data,Map event,String tokenid);
}
