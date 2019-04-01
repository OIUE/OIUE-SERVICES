package org.oiue.service.debug.treecache;

import org.oiue.service.cache.tree.script.CacheTreeScriptService;
import org.oiue.service.io.Handler;
import org.oiue.service.io.Session;

public class ServerHandler implements Handler {
	private CacheTreeScriptService bufferScript = null;
	
	public ServerHandler(CacheTreeScriptService bufferScript) {
		this.bufferScript = bufferScript;
	}
	
	@Override
	public void received(Session session, String line, byte[] bytes) {
		if (line.startsWith("o")) {
			session.write("o");
			session.close();
		} else if (line.startsWith("k")) {
			session.write("k");
		} else {
			session.write(bufferScript.eval(line) + "");
		}
	}
	
	@Override
	public void closed(Session session) {
		
	}
	
	@Override
	public void opened(Session session) {
		session.write("debug Cache Tree Service");
	}
	
	@Override
	public void idled(Session session) {
		session.close();
	}
	
	@Override
	public void sent(Session session) {
		
	}
	
	@Override
	public int getReaderIdleCount() {
		return 0;
	}
}
