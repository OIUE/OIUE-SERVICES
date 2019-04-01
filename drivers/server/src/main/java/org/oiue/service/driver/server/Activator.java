package org.oiue.service.driver.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.oiue.service.driver.api.DriverService;
import org.oiue.service.io.TcpService;
import org.oiue.service.io.UdpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private DriverPS hw;
			private SocketAddress address;
			private Logger logger;
			private UdpService udpService;
			private TcpService tcpService;
			private DriverService driverService;
			
			@Override
			public void removedService() {
				hw.stop();
				if (address != null) {
					udpService.unregister(address);
				}
				driverService.unregisterDriver(DriverPS.DriverName);
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				driverService = getService(DriverService.class);
//				udpService = getService(UdpService.class);
				tcpService = getService(TcpService.class);
				hw = new DriverPS(logService);
				
				driverService.registerDriver(DriverPS.DriverName, hw);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				try {
					if (address != null) {
						tcpService.unregister(address);
//						udpService.unregister(address);
					}
					int listenPort = Integer.parseInt(props.get("listenPort").toString());
					String listenAddress = props.get("listenAddress").toString();
					int idleTime = Integer.parseInt(props.get("receiveTimeOut").toString());
					String charset = props.get("charset").toString();
					
					address = new InetSocketAddress(listenAddress, listenPort);
//					udpService.register(address, hw, false, idleTime, charset);
					tcpService.register(address, hw, false, idleTime, charset);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, LogService.class, UdpService.class,TcpService.class, DriverService.class);
	}
	
	@Override
	public void stop() {}
}
