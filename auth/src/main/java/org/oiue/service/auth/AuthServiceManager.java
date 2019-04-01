package org.oiue.service.auth;

public interface AuthServiceManager extends AuthService {
	
	boolean registerAuthService(String name, AuthService auth);
	
	boolean unRegisterAuthService(String name);
	
}
