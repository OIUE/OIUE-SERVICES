package org.oiue.service.driver.api;

import java.util.Map;

import org.oiue.tools.StatusResult;

public interface Driver {
	void registered(DriverListener listener);
	
	void unregistered();
	
	@SuppressWarnings("rawtypes")
	StatusResult send(Map sendData);
}
