package org.oiue.service.action.tcp.bytes;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.io.TcpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private SocketAddress address;
			private TcpService tcpService;
			private ServerHandler handler;
			private Logger logger;
			
			@Override
			public void removedService() {
				if (tcpService != null && address != null) {
					tcpService.unregister(address);
				}
				address = null;
				tcpService = null;
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				OnlineService onlineService = getService(OnlineService.class);
				BytesService bytesService = getService(BytesService.class);
				tcpService = getService(TcpService.class);
				logger = logService.getLogger(getClass());
				
				handler = new ServerHandler(logService, actionService, onlineService, bytesService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				try {
					if (address != null) {
						tcpService.unregister(address);
					}
					int listenPort = Integer.parseInt(props.get("listenPort").toString());
					String listenAddress = props.get("listenAddress").toString();
					int idleTime = Integer.parseInt(props.get("idleTime").toString());
					String charset = props.get("charset").toString();
					address = new InetSocketAddress(listenAddress, listenPort);
					tcpService.register(address, handler, true, idleTime, charset);
					handler.updated(props);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, LogService.class, ActionService.class, OnlineService.class, BytesService.class, TcpService.class);
	}
	
	@Override
	public void stop() {}
}
