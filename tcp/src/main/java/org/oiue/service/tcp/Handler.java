package org.oiue.service.tcp;

public interface Handler {
	public void opened(Session session) throws Exception;

	public void received(Session session, String line, byte[] bytes) throws Exception;

	public void sent(Session session) throws Exception;

	public void idled(Session session) throws Exception;

	public void closed(Session session) throws Exception;
	
	public int getReaderIdleCount();
}
