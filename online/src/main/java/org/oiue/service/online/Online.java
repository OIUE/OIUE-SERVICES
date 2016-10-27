/**
 * 
 */
package org.oiue.service.online;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Every
 *
 */
@SuppressWarnings("rawtypes")
public interface Online extends Serializable {
	Object getO();
	
	void setO(Object o);

	Type getType();

	void setType(Type type);
	
	String getToken();

	void setToken(String token);

	Map getUser();

	void setUser(Map user);

	String getUser_name();

	void setUser_name(String user_name);
	
	String getUser_id();

	void setUser_id(String user_id);
	
	long getLastTime();
	
	void setLastTime(long lastTime);
	
	long getLoginTime();
	
	void setLoginTime(long loginTime);
	
	void setStatus(Type type);
	
	void resetStatus(Type type);
	
	int getStatus();
	
	Type getBestType();
}
