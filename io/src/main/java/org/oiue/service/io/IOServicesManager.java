package org.oiue.service.io;

public interface IOServicesManager extends UdpService {
	boolean registerTcpService(String name,TcpService tcpService);
	
	TcpService getTcpService(String name);
	
	TcpService getTcpService();

	boolean registerUdpService(String name,UdpService udpService);
	
	UdpService getUdpService(String name);
	
	UdpService getUdpService();
}