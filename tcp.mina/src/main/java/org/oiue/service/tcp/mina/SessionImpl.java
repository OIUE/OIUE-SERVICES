package org.oiue.service.tcp.mina;

import java.io.Serializable;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.tcp.Session;
import org.oiue.tools.exception.ExceptionUtil;

@SuppressWarnings("serial")
public class SessionImpl implements Session, Serializable {
	private IoSession session;
	private Logger logger;
	
	public SessionImpl(IoSession session, LogService logService) {
		this.session = session;
		this.logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public void close() {
		try {
			session.close(true);
		} catch (Throwable e) {
			logger.error("close session error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}
	
	@Override
	public Object getAttribute(String key) {
		try {
			return session.getAttribute(key);
		} catch (Throwable e) {
			logger.error("getAttribute error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
		return null;
	}
	
	@Override
	public Object setAttribute(String key, Object attribute) {
		try {
			return session.setAttribute(key, attribute);
		} catch (Throwable e) {
			logger.error("setAttribute error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return session.toString();
		} catch (Throwable e) {
			logger.error("session toString error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
		return null;
	}
	
	public static String toByteString(byte[] bytes, int size) {
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
	
	private static String toByteHex(byte b) {
		String temp = Integer.toHexString(0x000000FF & b);
		if (temp.length() < 2) {
			return "0" + temp;
		}
		return temp;
	}
	
	@Override
	public void write(byte[] data) {
		try {
			if (session.isClosing())
				throw new NullPointerException();
			if (logger.isDebugEnabled()) {
				logger.debug("write session = " + session + ", length = " + data.length + ", bytes = " + toByteString(data, data.length));
			}
			session.write(IoBuffer.wrap(data));
		} catch (Throwable e) {
			logger.error("session write byte error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}
	
	@Override
	public void write(String line) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("write session = " + session + ", text = " + line);
			}
			session.write(line);
		} catch (Throwable e) {
			logger.error("session write string error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}
}
