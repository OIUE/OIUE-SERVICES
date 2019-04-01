package org.oiue.service.event.dataconvert;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Map;

import org.oiue.service.osgi.FrameActivator;

public interface ETLService extends Serializable {
	
	Object convertToGeometry(Map data, Map event, String tokenid) throws SQLException;
	
	void updated(Dictionary<String, ?> props, FrameActivator tracker);
	
	Object convertColumnType(Map data, Map event, String tokenid) throws Throwable;
}
