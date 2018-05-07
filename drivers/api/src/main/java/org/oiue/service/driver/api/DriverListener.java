package org.oiue.service.driver.api;

import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface DriverListener {
	StatusResult receive(Map data);
}
