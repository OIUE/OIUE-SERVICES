package org.oiue.service.event.entity;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EntityService extends Serializable {
	void userDefinedEntity(Map data, Map event, String tokenid)throws Throwable;
	void createEntity(Map data, Map event, String tokenid);
	void insertEntity(Map data, Map event, String tokenid);
	void loadEntity(Map data, Map event, String tokenid);
	
	Object convertToGeometry(Map data, Map event, String tokenid) throws SQLException;
	Object convertColumnType(Map data, Map event, String tokenid) throws Throwable;
	public static String _system_colnum = "system_id";
}
