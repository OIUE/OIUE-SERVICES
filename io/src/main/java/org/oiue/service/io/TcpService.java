package org.oiue.service.io;

import java.net.SocketAddress;

public interface TcpService {
	public boolean register(SocketAddress address, Handler handler, boolean binary, int idleTime, String charsetName);
	
	public void unregister(SocketAddress address);
	
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName);
}
