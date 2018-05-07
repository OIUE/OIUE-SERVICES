package org.oiue.service.permission;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.online.Online;
import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface PermissionService extends Serializable {
	StatusResult verify(Map per, Online online);
	
	StatusResult convert(Map per);
	
	void unregister();
}
