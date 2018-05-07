package org.oiue.service.event.etl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Map;

public interface ETLService extends Serializable {
	Object getRepository(Map data, Map event, String tokenid);
	
	Object getDatabaseType(Map data, Map event, String tokenid);
	
	Object getAccessMethod(Map data, Map event, String tokenid);
	
	Object getAccessSetting(Map data, Map event, String tokenid);
	
	Object setRepository(Map data, Map event, String tokenid);
	
	Object delRepository(Map data, Map event, String tokenid);
	
	Object getEntity(Map data, Map event, String tokenid);
	
	Object getEntityColumns(Map data, Map event, String tokenid);
	
	Object setEntityColumns(Map data, Map event, String tokenid) throws Exception;
	
	Object setEntityRelation(Map data, Map event, String tokenid) throws Exception;
	
	Object savaTrans(Map data, Map event, String tokenid);
	
	Object runTrans(Map data, Map event, String tokenid);
	
	Object initRun(Map data, Map event, String tokenid);
	
	Object result(Map data, Map event, String tokenid);
	
	Object convertToGeometry(Map data, Map event, String tokenid) throws SQLException;
	
	void readAndInsertEntiry(Map data, Map event, String tokenid) throws Exception;
	
	void updated(Dictionary<String, ?> props);
	Object createApi(Map data, Map event, String tokenid) throws Throwable;
	Object testServiceEvent(Map data, Map event, String tokenid) throws Throwable;
	Object convertColumnType(Map data, Map event, String tokenid) throws Throwable;
	Object getMaxMinValue(Map data, Map event, String tokenid) throws Throwable;
	Object getCountValue(Map data, Map event, String tokenid) throws Throwable;
}
