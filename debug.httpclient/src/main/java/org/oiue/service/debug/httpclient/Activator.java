package org.oiue.service.debug.httpclient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Dictionary;

import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.tcp.TcpService;

public class Activator extends FrameActivator {

    @Override
    public void start()  {
        this.start(new MulitServiceTrackerCustomizer() {
            private TcpService tcpService;
            private SocketAddress address;
            private HttpClientService httpClient;
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
                httpClient = (HttpClientService) getService(HttpClientService.class);
                tcpService = getService(TcpService.class);
                LogService logService = getService(LogService.class);
                logger=logService.getLogger(getClass());
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
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
                    tcpService.register(address, new ServerHandler(httpClient), false, idleTime, charset);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }, LogService.class, HttpClientService.class, TcpService.class);
    }

    @Override
    public void stop()  {}
}
