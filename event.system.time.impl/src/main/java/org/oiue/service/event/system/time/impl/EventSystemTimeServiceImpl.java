package org.oiue.service.event.system.time.impl;

import java.util.Map;

import org.oiue.service.event.system.time.EventSystemTimeService;
import org.oiue.service.log.Logger;

@SuppressWarnings("serial")
public class EventSystemTimeServiceImpl implements EventSystemTimeService {

    protected static Logger logger;
    @SuppressWarnings({ "rawtypes"})
    @Override
    public Object getTime(Map data, Map event, String tokenid) {
        return System.currentTimeMillis()/1000;
    }
}
