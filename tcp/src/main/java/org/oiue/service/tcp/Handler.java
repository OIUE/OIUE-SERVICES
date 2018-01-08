package org.oiue.service.tcp;

public interface Handler {
	public void opened(Session session) ;

	public void received(Session session, String line, byte[] bytes) ;

	public void sent(Session session) ;

	public void idled(Session session) ;

	public void closed(Session session) ;
	
	public int getReaderIdleCount();
}
