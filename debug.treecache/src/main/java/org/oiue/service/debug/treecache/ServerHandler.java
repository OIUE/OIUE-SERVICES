package org.oiue.service.debug.treecache;

import org.oiue.service.cache.tree.script.CacheTreeScriptService;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;

public class ServerHandler implements Handler {
    private CacheTreeScriptService bufferScript = null;

	public ServerHandler(CacheTreeScriptService bufferScript) {
		this.bufferScript = bufferScript;
	}

	@Override
	public void received(Session session, String line, byte[] bytes) throws Exception {
		if (line.startsWith("o")) {
			session.write("o");
			session.close();
		} else if (line.startsWith("k")) {
			session.write("k");
		} else {
		    session.write(bufferScript.eval(line)+"");
		}
	}

	@Override
	public void closed(Session session) throws Exception {

	}

	@Override
	public void opened(Session session) throws Exception {
	    session.write("debug Cache Tree Service");
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
