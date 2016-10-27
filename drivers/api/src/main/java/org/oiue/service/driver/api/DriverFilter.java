package org.oiue.service.driver.api;

import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface DriverFilter {
    int getPriority();
    void setPriority(int priority);
    StatusResult doFilter(Map data);
}
