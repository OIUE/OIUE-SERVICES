package org.oiue.service.debug.treecache;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.oiue.service.cache.tree.script.CacheTreeScriptService;
import org.oiue.service.io.TcpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private CacheTreeScriptService cacheTreeScript;
			private TcpService tcpService;
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
				cacheTreeScript = getService(CacheTreeScriptService.class);
				tcpService = getService(TcpService.class);
				LogService logService = getService(LogService.class);
				logger = logService.getLogger(getClass());
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				try {
					if (props == null)
						return;
					if (address != null) {
						tcpService.unregister(address);
					}
					int listenPort = Integer.parseInt(props.get("listenPort").toString());
					String listenAddress = props.get("listenAddress").toString();
					int idleTime = Integer.parseInt(props.get("receiveTimeOut").toString());
					String charset = props.get("charset").toString();
					address = new InetSocketAddress(listenAddress, listenPort);
					tcpService.register(address, new ServerHandler(cacheTreeScript), false, idleTime, charset);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, LogService.class, CacheTreeScriptService.class, TcpService.class);
	}
	
	@Override
	public void stop() {}
}
