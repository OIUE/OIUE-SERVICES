package org.oiue.service.event.etl;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Map;

public interface ETLService extends Serializable {
	Object getRepository(Map data,Map event,String tokenid);
	Object getDatabaseType(Map data,Map event,String tokenid);
	Object getAccessMethod(Map data,Map event,String tokenid);
	Object getAccessSetting(Map data,Map event,String tokenid);

	Object setRepository(Map data,Map event,String tokenid);
	Object delRepository(Map data,Map event,String tokenid);

	Object getEntity(Map data,Map event,String tokenid);
	Object getEntityColumns(Map data,Map event,String tokenid);

	Object setEntityColumns(Map data,Map event,String tokenid);
	Object setEntityRelation(Map data,Map event,String tokenid);

	Object savaTrans(Map data,Map event,String tokenid);
	Object runTrans(Map data,Map event,String tokenid);

	Object initRun(Map data,Map event,String tokenid);
	Object result(Map data,Map event,String tokenid);

	Object convertToGeometry(Map data,Map event,String tokenid);
	void readAndInsertEntiry(Map data, Map event, String tokenid);
	void updated(Dictionary<String, ?> props);
}
