package org.oiue.service.driver.server;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.oiue.service.driver.api.Driver;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.io.Handler;
import org.oiue.service.io.Session;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("unused")
public class DriverPS implements Handler, Driver {
	public final static String DriverName = "hwwifi_head";
	private LogService logService;
	private Logger logger;
	private Map<String, Session> clients = new ConcurrentHashMap<>();
	public static LinkedBlockingQueue<RO> reveiceQueue = new LinkedBlockingQueue<>();
	
	
	public DriverPS(LogService logService) {
		this.logService = logService;
		this.logger = this.logService.getLogger(getClass());
	}
	
	@Override
	public void opened(Session session) {
		this.logger.info("tcp opened " + session);
		if (session == null)
			return;
		if (null == session.getAttribute("binary"))
			session.setAttribute("binary", false);
	}
	
	@Override
	public void received(Session session, String line, byte[] bytes) {
		this.logger.debug("received[{}] {}" , session,line);
		reveiceQueue.offer(new RO(session, line));
	}
	
	@Override
	public void sent(Session session) {
		
	}
	
	@Override
	public void idled(Session session) {
		
	}
	
	@Override
	public void closed(Session session) {
		
	}
	
	@Override
	public int getReaderIdleCount() {
		return 0;
	}
	
	@SuppressWarnings("serial")
	public class RO implements Serializable {
		public Session session;
		public String line;
		
		public RO(Session session,String line) {
			this.session = session;
			this.line = line;
		}
	}
	
	boolean run = true;
	
	class Receive extends Thread {
		@Override
		public void run() {
			Session s=null;
			while (run) {
				logger.debug("running ...");
				try {
					RO t = reveiceQueue.take();
					logger.debug("receive data {}", t.line);
					Map rdata = JSONUtil.parserStrToMap(t.line);
					listener.receive(rdata);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			if(s!=null)
			try {
				s.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private DriverListener listener = null;
	@Override
	public void registered(DriverListener listener) {
		this.listener = listener;
		run = true;
		new Receive().start();
	}
	
	@Override
	public void unregistered() {
		run = false;
	}
	
	@Override
	public StatusResult send(Map sendData) {
		return null;
	}

	public void stop() {
		run=false;
	}
}
