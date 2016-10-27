package org.oiue.service.driver.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface DriverService extends Serializable {

    boolean registerDriver(String driverName, Driver driver);

    void unregisterDriver(String driverName);

    Driver getDriver(String driverName);

    void unregisterAllDriver();

    StatusResult send(Map data);

    StatusResult receive(Map data);

    int registerFilterService(DriverFilterService filter, String driverName);

    void unregisterFilterService(String filterName);

    void unregisterFilterService(DriverFilterService filter);

    boolean registerListenerService(DriverListenerService listener, String driverName);

    void unregisterListenerService(String listenerName);

    void unregisterListenerService(DriverListenerService listener);
}
