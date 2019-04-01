package org.oiue.service.dataconvert;

import java.util.List;
import java.util.Map;

public interface ConvertServiceManager extends ConvertService {
	
	boolean registerConvertService(String name, ConvertService convert);
	
	boolean unRegisterConvertService(String name);
	
	ConvertService getConvertService(String name);
	
	Object convert(Map data, Map event, String tokenid);
	
	Object convert(List data, Map event, String tokenid);
}
