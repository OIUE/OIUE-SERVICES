package org.oiue.service.event.etl;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Map;

public interface ETLService extends Serializable {
	Object getRepository(Map data,Map event,String tokenid) throws Throwable;
	Object getDatabaseType(Map data,Map event,String tokenid) throws Throwable;
	Object getAccessMethod(Map data,Map event,String tokenid) throws Throwable;
	Object getAccessSetting(Map data,Map event,String tokenid) throws Throwable;

	Object setRepository(Map data,Map event,String tokenid) throws Throwable;

	Object getEntity(Map data,Map event,String tokenid) throws Throwable;
	Object getEntityColumns(Map data,Map event,String tokenid) throws Throwable;

	Object setEntityColumns(Map data,Map event,String tokenid) throws Throwable;
	Object setEntityRelation(Map data,Map event,String tokenid) throws Throwable;

	Object savaTrans(Map data,Map event,String tokenid) throws Throwable;
	Object runTrans(Map data,Map event,String tokenid) throws Throwable;

	Object initRun(Map data,Map event,String tokenid) throws Throwable;
	Object result(Map data,Map event,String tokenid) throws Throwable;

	void updated(Dictionary<String, ?> props);
}
