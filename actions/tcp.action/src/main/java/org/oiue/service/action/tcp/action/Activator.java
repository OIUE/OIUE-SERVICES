package org.oiue.service.action.tcp.action;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.TcpService;

public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {
			private TcpService tcpService;
			private SocketAddress address;
			private Handler handler;
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
				tcpService = getService(TcpService.class);
				logger = logService.getLogger(getClass());

				handler = new ServerHandler(logService, actionService, onlineService);
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
					tcpService.register(address, handler, false, idleTime, charset);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, TcpService.class, ActionService.class, LogService.class, OnlineService.class);
	}

	@Override
	public void stop()  {}
}
