package org.oiue.service.auth;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.online.Online;

@SuppressWarnings("rawtypes")
public interface AuthService extends Serializable {
	Online login(Map per);
	
	boolean logout(Map per);
	
	void unregister();
}
