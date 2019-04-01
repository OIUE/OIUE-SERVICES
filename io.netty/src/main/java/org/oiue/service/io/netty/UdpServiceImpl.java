package org.oiue.service.io.netty;

import java.net.SocketAddress;

import org.oiue.service.io.Handler;
import org.oiue.service.io.UdpService;
import org.oiue.service.log.LogService;

public class UdpServiceImpl extends TcpServiceImpl implements UdpService {
	
	public UdpServiceImpl(LogService logService) {
		super(logService);
		logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public synchronized boolean register(SocketAddress address, Handler handler, boolean binary, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("register address = " + address + ", binary = " + binary + ", ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("register error", e);
			}
			return false;
		}
	}
	
	@Override
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("connect address = " + address + ", binary = " + binary + ", ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("connect error", e);
			}
		}
		return false;
	}
	
}
