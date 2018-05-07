package org.oiue.service.auth;

public interface AuthServiceManager extends AuthService {
	
	public boolean registerAuthService(String name, AuthService auth);
	
	public boolean unRegisterAuthService(String name);
	
}
