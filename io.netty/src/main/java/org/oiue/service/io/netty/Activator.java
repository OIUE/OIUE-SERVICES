package org.oiue.service.io.netty;

import java.util.Map;

import org.oiue.service.io.TcpService;
import org.oiue.service.io.UdpService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private TcpServiceImpl tcpService;
			private UdpServiceImpl udpService;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				tcpService = new TcpServiceImpl(logService);
				registerService(TcpService.class, tcpService);
				
				udpService = new UdpServiceImpl(logService);
				registerService(UdpService.class, udpService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
