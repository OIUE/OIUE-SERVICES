package org.oiue.service.event.entity;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface EntityService extends Serializable {
	/**
	 * 自定义数据源
	 * @param data
	 * @param event
	 * @param tokenid
	 * @throws Throwable
	 */
	void userDefinedEntity(Map data, Map event, String tokenid)throws Throwable;
	void createEntityView(Map data, Map event, String tokenid) throws Throwable;
	void loadEntity(Map data, Map event, String tokenid);
	
	void geo(Map data, Map event, String tokenid) throws Throwable;
	void regeo(Map data, Map event, String tokenid) throws Throwable;
	
	Object convertToGeometry(Map data, Map event, String tokenid) throws Throwable;
	Object convertColumnType(Map data, Map event, String tokenid) throws Throwable;
	public static String _system_colnum = "system_id";
	void updatedConf(Map<String, ?> props);
}
