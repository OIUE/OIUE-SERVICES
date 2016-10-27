package org.oiue.service.debug.cache;

import java.util.List;
import java.util.Map;

import org.oiue.service.cache.script.CacheScriptResult;
import org.oiue.service.cache.script.CacheScriptService;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;
import org.oiue.tools.list.ListUtil;
import org.oiue.tools.map.MapUtil;

public class ServerHandler implements Handler {
	private CacheScriptService cacheScript = null;

	public ServerHandler(CacheScriptService cacheScript) {
		this.cacheScript = cacheScript;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public void received(Session session, String line, byte[] bytes) throws Exception {
		if (line.startsWith("o")) {
			session.write("o");
			session.close();
		} else if (line.startsWith("k")) {
			session.write("k");
		} else {
			CacheScriptResult result = cacheScript.eval(line);
			if (result.getResult() != CacheScriptResult.OK) {
				session.write(result.getResult());
			} else {
				Object  data=result.getData();
				if(data instanceof List){
					session.write(ListUtil.toString((List)data));
				}else if(data instanceof Map){
					session.write(MapUtil.toString((Map) data));
				}else{
					session.write(data+"");
				}
			}
		}
	}

	@Override
	public void closed(Session session) throws Exception {

	}

	@Override
	public void opened(Session session) throws Exception {

	}

	@Override
	public void idled(Session session) throws Exception {
		session.close();
	}

	@Override
	public void sent(Session session) throws Exception {
		
	}

    @Override
    public int getReaderIdleCount() {
        return 0;
    }
}
