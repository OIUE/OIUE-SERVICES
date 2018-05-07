package org.oiue.service.debug.insertmodule;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.sql.SqlService;
import org.oiue.service.tcp.TcpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			
			private TcpService tcpService;
			private SqlService sqlService;
			private SocketAddress address;
			private Logger logger;
			
			@Override
			public void removedService() {
				if ((tcpService != null) && (address != null)) {
					tcpService.unregister(address);
					address = null;
				}
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				tcpService = getService(TcpService.class);
				sqlService = getService(SqlService.class);
				logger = logService.getLogger(getClass());
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				
				try {
					if (address != null) {
						tcpService.unregister(address);
					}
					int listenPort = Integer.parseInt(props.get("listenPort").toString());
					String listenAddress = props.get("listenAddress").toString();
					int idleTime = Integer.parseInt(props.get("receiveTimeOut").toString());
					String charset = props.get("charset").toString();
					address = new InetSocketAddress(listenAddress, listenPort);
					tcpService.register(address, new ClientServerHandler(tcpService, sqlService), false, idleTime, charset);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, LogService.class, TcpService.class, SqlService.class);
	}
	
	@Override
	public void stop() {}
}
