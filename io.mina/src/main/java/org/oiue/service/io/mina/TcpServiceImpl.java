package org.oiue.service.io.mina;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.oiue.service.io.Handler;
import org.oiue.service.io.TcpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

public class TcpServiceImpl implements TcpService {
	protected HashMap<SocketAddress, IoAcceptor> acceptorMap = new HashMap<SocketAddress, IoAcceptor>();
	protected Logger logger;
	protected LogService logService;
	
	public TcpServiceImpl(LogService logService) {
		this.logService = logService;
		logger = logService.getLogger(this.getClass());
	}
	
	public synchronized void unregisterAll() {
		if (logger.isInfoEnabled()) {
			logger.info("unregisterAll");
		}
		for (IoAcceptor e : acceptorMap.values()) {
			e.dispose();
		}
		acceptorMap.clear();
	}
	
	@Override
	public synchronized boolean register(SocketAddress address, Handler handler, boolean binary, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("register address = " + address + ", binary = " + binary + ", ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			NioSocketAcceptor acceptor = new NioSocketAcceptor();
			
			acceptor.setReuseAddress(true);
			if (!binary) {
				if (charsetName == null) {
					charsetName = "UTF-8";
				}
				TextLineCodecFactory textLineCodec = new TextLineCodecFactory(Charset.forName(charsetName));
				textLineCodec.setDecoderMaxLineLength(Integer.MAX_VALUE);
				textLineCodec.setEncoderMaxLineLength(Integer.MAX_VALUE);
				acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(textLineCodec));
			}
			acceptor.setHandler(new ServiceHandler(handler, binary, null, logService));
			acceptor.getSessionConfig().setTcpNoDelay(true);
			acceptor.getSessionConfig().setSendBufferSize(2 * 1024 * 1024);
			acceptor.getSessionConfig().setReadBufferSize(2 * 1024 * 1024);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, idleTime / 1000);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE, idleTime / 1000);
			acceptor.bind(address);
			acceptorMap.put(address, acceptor);
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("register error", e);
			}
			return false;
		}
	}
	
	@Override
	public synchronized void unregister(SocketAddress address) {
		if (logger.isInfoEnabled()) {
			logger.info("unregister address = " + address);
		}
		IoAcceptor acceptor = acceptorMap.get(address);
		if (acceptor != null) {
			acceptor.dispose();
			acceptorMap.remove(address);
		}
	}
	
	@Override
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("connect address = " + address + ", binary = " + binary + ", connectTimeout = " + connectTimeout + "ms, ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			NioSocketConnector connector = new NioSocketConnector();
			connector.setConnectTimeoutMillis(connectTimeout);
			if (!binary) {
				if (charsetName == null) {
					charsetName = "UTF-8";
				}
				TextLineCodecFactory textLineCodec = new TextLineCodecFactory(Charset.forName(charsetName));
				textLineCodec.setDecoderMaxLineLength(Integer.MAX_VALUE);
				textLineCodec.setEncoderMaxLineLength(Integer.MAX_VALUE);
				connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(textLineCodec));
			}
			ServiceHandler serviceHandler = new ServiceHandler(handler, binary, connector, logService);
			connector.setHandler(serviceHandler);
			connector.getSessionConfig().setTcpNoDelay(true);
			connector.getSessionConfig().setSendBufferSize(2 * 1024 * 1024);
			connector.getSessionConfig().setReadBufferSize(2 * 1024 * 1024);
			connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, idleTime / 1000);
			connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE, idleTime / 1000);
			
			ConnectFuture future = connector.connect(address);
			future.addListener(new ConnectFutureListener(serviceHandler));
			
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("connect error", e);
			}
		}
		return false;
	}
	
	private class ConnectFutureListener implements IoFutureListener<ConnectFuture> {
		private ServiceHandler handler;
		
		public ConnectFutureListener(ServiceHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public void operationComplete(ConnectFuture future) {
			if (!future.isConnected()) {
				try {
					handler.sessionClosed(null);
				} catch (Exception e) {}
			}
		}
	}
}
