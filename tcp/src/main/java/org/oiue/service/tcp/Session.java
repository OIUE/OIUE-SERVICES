package org.oiue.service.tcp;

public interface Session {
	public void close();
	
	public Object setAttribute(String key, Object attribute);
	
	public Object getAttribute(String key);
	
	public void write(byte[] data);
	
	public void write(String line);
}
