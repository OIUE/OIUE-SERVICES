package org.oiue.service.io.impl;

import java.net.SocketAddress;

import org.oiue.service.io.Handler;
import org.oiue.service.io.IOServicesManager;
import org.oiue.service.io.TcpService;
import org.oiue.service.io.UdpService;

public class IOServicesManagerImpl implements IOServicesManager {

	@Override
	public boolean register(SocketAddress address, Handler handler, boolean binary, int idleTime, String charsetName) {
		return false;
	}

	@Override
	public void unregister(SocketAddress address) {
		
	}

	@Override
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName) {
		return false;
	}

	@Override
	public boolean registerTcpService(String name, TcpService tcpService) {
		return false;
	}

	@Override
	public TcpService getTcpService(String name) {
		return null;
	}

	@Override
	public TcpService getTcpService() {
		return null;
	}

	@Override
	public boolean registerUdpService(String name, UdpService udpService) {
		return false;
	}

	@Override
	public UdpService getUdpService(String name) {
		return null;
	}

	@Override
	public UdpService getUdpService() {
		return null;
	}
}