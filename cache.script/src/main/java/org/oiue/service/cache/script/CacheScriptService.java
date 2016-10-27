package org.oiue.service.cache.script;

import java.util.List;
import java.util.Map;

public interface CacheScriptService {
	public CacheScriptResult eval(String script);

	public CacheScriptResult eval(String script, List<Object> list);

	public CacheScriptResult eval(String script, Map<String, Object> map);
}
