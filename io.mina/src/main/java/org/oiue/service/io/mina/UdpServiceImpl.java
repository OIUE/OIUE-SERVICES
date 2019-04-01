package org.oiue.service.io.mina;

import java.net.SocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
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
			NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
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
			acceptor.getSessionConfig().setReadBufferSize(2048);
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
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("connect address = " + address + ", binary = " + binary + ", ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			NioDatagramConnector connector = new NioDatagramConnector();
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
			connector.getSessionConfig().setReadBufferSize(4096);
			connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, idleTime / 1000);
			connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE, idleTime / 1000);
			
			ConnectFuture future = connector.connect(address);
			future.addListener(new ConnectFutureListener(serviceHandler));
			return true;
		} catch (Throwable e) {
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
