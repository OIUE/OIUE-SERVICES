package org.oiue.service.tcp.mina;

import java.io.Serializable;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;

@SuppressWarnings("serial")
public class ServiceHandler extends IoHandlerAdapter implements Serializable {
    // public static final String
    // SERVICE_SOCKET_IDLE_IS_READER="service_socket_idle_is_reader";
    // public static final String
    // SERVICE_SOCKET_IDLE_READER="service_socket_idle_reader";
    // public static final String
    // SERVICE_SOCKET_IDLE_WRITER="service_socket_idle_writer";

    private Logger logger = null;

    public static final String SESSION_NAME = "SERVICE_TCP_SESSION";
    public static final String REMOTE_ADDRESS = "SERVICE_REMOTE_ADDRESS";
    public static final String LAST_TIME = "SERVICE_LAST_TIME";
    public static final String SESSION_BINARY = "SERVICE_SESSION_BINARY";
    private Handler handler;
    private boolean binary;
    private LogService logService;
    private IoConnector connector;

    public ServiceHandler(Handler handler, boolean binary, IoConnector connector, LogService logService) {
        this.handler = handler;
        this.binary = binary;
        this.connector = connector;
        this.logService = logService;

        logger = logService.getLogger(this.getClass());
        logger.info("new service handle binary = " + binary);
    }

    public String toByteString(byte[] bytes, int size) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                sb.append("0x");
            } else {
                sb.append(", 0x");
            }
            sb.append(toByteHex(bytes[i]));
        }
        return sb.toString();
    }

    private String toByteHex(byte b) {
        String temp = Integer.toHexString(0x000000FF & b);
        if (temp.length() < 2) {
            return "0" + temp;
        }
        return temp;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        session.setAttribute(LAST_TIME, System.currentTimeMillis());
        if (binary) {
            IoBuffer buffer = (IoBuffer) message;
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            if (logger.isDebugEnabled()) {
                logger.debug("messageReceived session = " + session + ", length = " + bytes.length + ", bytes = " + toByteString(bytes, bytes.length));
            }
            handler.received((Session) session.getAttribute(SESSION_NAME), null, bytes);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("messageReceived session = " + session + ", text = " + message);
            }
            String msg = message.toString();
            if (msg != null && !msg.startsWith("{'_t':'hb'"))
                handler.received((Session) session.getAttribute(SESSION_NAME), msg, null);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("exceptionCaught session = " + session + ", cause message = " + cause.getMessage());
        }
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("sessionCreated session = " + session);
        }
        session.setAttribute(SESSION_NAME, new SessionImpl(session, logService));
        session.setAttribute(SESSION_BINARY, binary);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if (connector != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connector.dispose();
                }

            },this.getClass().getName()).start();
        }
        if (session == null) {
            if (logger.isInfoEnabled()) {
                logger.info("connect error");
            }
            handler.closed(null);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("sessionClosed session = " + session);
            }
            handler.closed((Session) session.getAttribute(SESSION_NAME));
            session.close(true);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.info("messageSent session = " + session);
        }
        session.setAttribute(LAST_TIME, System.currentTimeMillis());
        handler.sent((Session) session.getAttribute(SESSION_NAME));
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("sessionOpened session = " + session);
        }
        Session newSession = (Session) session.getAttribute(SESSION_NAME);
        session.setAttribute(REMOTE_ADDRESS, session.getRemoteAddress());
        session.setAttribute(LAST_TIME, System.currentTimeMillis());
        handler.opened(newSession);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("idled is_reader = " + (status == IdleStatus.READER_IDLE) + "|sessionIdle session = " + session);
        }
        if(session.isClosing())
            return;
        Session newSession = (Session) session.getAttribute(SESSION_NAME);
        int readerIdleCount = session.getIdleCount(IdleStatus.READER_IDLE);
        if (logger.isDebugEnabled()) {
            logger.debug("idled is_reader = " + (status == IdleStatus.READER_IDLE) + "/" + readerIdleCount);
        }
        if (status == IdleStatus.READER_IDLE) {
            if (readerIdleCount != 0 && handler.getReaderIdleCount() != 0 && readerIdleCount > handler.getReaderIdleCount()) {
                newSession.close();
            }
        } else {
            session.setAttribute(LAST_TIME, System.currentTimeMillis());
            if (binary) {
            } else {
                if (logger.isDebugEnabled()) {
                    newSession.write("{'_t':'hb','_data':'heartbeat','_reader_idle':" + readerIdleCount + ",'_writer_idle':" + session.getIdleCount(IdleStatus.WRITER_IDLE) + ",'_system_time':" + System.currentTimeMillis() + "}");
                } else {
                    newSession.write("{'_t':'hb'}");
                }
            }
        }
        if (status == IdleStatus.WRITER_IDLE)
            handler.idled(newSession);
    }
}
