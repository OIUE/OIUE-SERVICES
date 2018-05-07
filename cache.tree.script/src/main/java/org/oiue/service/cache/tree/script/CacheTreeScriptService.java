package org.oiue.service.cache.tree.script;

import java.util.List;
import java.util.Map;

public interface CacheTreeScriptService {
	public Object eval(String script);
	
	public Object eval(String script, List<Object> list);
	
	public Object eval(String script, Map<String, Object> map);
}
