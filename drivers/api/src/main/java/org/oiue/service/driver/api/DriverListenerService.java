package org.oiue.service.driver.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

public interface DriverListenerService extends Serializable {
	String getListenerName();
	
	boolean registerListener(DriverListener listener);
	
	boolean registerListener(String driverName, DriverListener listener);
	
	void unregisterListener(DriverListener listener);
	
	void unregisterListener(String listenerName);
	
	void unregisterAllListener();
	
	void unregisterAllListener(String driverName);
	
	@SuppressWarnings("rawtypes")
	StatusResult receive(Map data);
}
