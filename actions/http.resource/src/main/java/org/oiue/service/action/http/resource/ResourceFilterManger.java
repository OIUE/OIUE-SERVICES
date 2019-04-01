package org.oiue.service.action.http.resource;

import java.io.Serializable;

public interface ResourceFilterManger extends Serializable {
	void unregisterResourceFilter(String requestAction);
	void unregisterResourceResultFilter(String requestAction);
	boolean registerResourceFilter(String requestAction, ResourceFilter ResourceFilter, int index);
	boolean registerResourceResultFilter(String requestAction, ResourceResultFilter ResourceResultFilter, int index);
}
