package org.oiue.service.permission;

public interface PermissionServiceManager extends PermissionService {
	
	public boolean registerPermissionService(String name, PermissionService verify);
	
	public boolean unRegisterPermissionService(String name);
}