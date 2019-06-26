package org.oiue.service.event.etl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

import org.oiue.service.osgi.FrameActivator;

@SuppressWarnings("rawtypes")
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
	
	void readAndInsertEntiry(Map data, Map event, String tokenid) throws Exception;
	
	void updated(Map<String, ?> props, FrameActivator tracker);
	
	Object createApi(Map data, Map event, String tokenid) throws Throwable;
	
	Object testServiceEvent(Map data, Map event, String tokenid) throws Throwable;
	
	Object getMaxMinValue(Map data, Map event, String tokenid) throws Throwable;
	
	Object getCountValue(Map data, Map event, String tokenid) throws Throwable;
	
	Object getCount(Map data, Map event, String tokenid) throws Throwable;
	
	Object convertToGeometry(Map paramMap1, Map paramMap2, String paramString) throws SQLException;

	Object convertColumnType(Map paramMap1, Map paramMap2, String paramString) throws Throwable;
	
	void userDefinedEntity(Map paramMap1, Map paramMap2, String paramString) throws Throwable;
	  
	void createEntity(Map paramMap1, Map paramMap2, String paramString) throws Throwable;
	  
	void insertEntity(Map paramMap1, Map paramMap2, String paramString) throws Throwable;
}
