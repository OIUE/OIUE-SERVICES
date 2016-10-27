/**
 * 
 */
package org.oiue.service.action.api;

import java.io.Serializable;
import java.util.Map;


/**
 * @author Every
 *
 */
public interface ActionService extends Serializable {
	public boolean registerActionFilter(String requestAction, ActionFilter actionFilter,int index);
	public void unregisterActionFilter(String requestAction);
	
	public boolean registerActionResultFilter(String requestAction, ActionResultFilter actionResultFilter,int index);
	public void unregisterActionResultFilter(String requestAction);

	public void unregisterAllActionFilter();
	
	public Map<String,ActionFilter> getBeforeActionFilterPool();
	
	public String request(String paramString);
    public Map<Object,Object> request(Map<Object,Object> per);
}
