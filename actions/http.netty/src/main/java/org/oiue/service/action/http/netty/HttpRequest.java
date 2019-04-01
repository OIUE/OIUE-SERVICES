package org.oiue.service.action.http.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

public final class HttpRequest implements HttpServletRequest {
	private FullHttpRequest request;
	private Map<String,Object> attribute = new HashMap<>();
	
	public HttpRequest(FullHttpRequest request) {
		this.request=request;
	}

	@Override
	public Object getAttribute(String name) {
		return attribute.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attribute.keySet());
	}
	private String characterEncoding = null;
	@Override
	public String getCharacterEncoding() {
		if (characterEncoding == null) {
			String contentType = getContentType();
			if (contentType == null) {
				return null;
			} else {
				int charsetPos = contentType.indexOf("charset=");
				if (charsetPos == -1) {
					characterEncoding = "UTF-8";
				} else {
					characterEncoding = contentType.substring(charsetPos + 8);
				}
			}
		}
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		this.characterEncoding=env;
	}

	@Override
	public int getContentLength() {
		return getIntHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
	}

	@Override
	public long getContentLengthLong() {
		return 0;
	}

	@Override
	public String getContentType() {
		String contentType = null;
		try {
			contentType=request.headers().get(HttpHeaderNames.CONTENT_TYPE).toString();
		}catch(Throwable e) {
			
		}
        if (contentType == null) {
            contentType = "application/x-www-form-urlencoded";
        }
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getParameter(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRemoteAddr() {
//		return this.request.headers().;
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object o) {
		this.attribute.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		this.attribute.remove(name);
	}

	@Override
	public Locale getLocale() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public Enumeration<Locale> getLocales() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isSecure() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getRealPath(String path) {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public int getRemotePort() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getLocalName() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getLocalAddr() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public int getLocalPort() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public ServletContext getServletContext() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isAsyncStarted() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isAsyncSupported() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public AsyncContext getAsyncContext() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public DispatcherType getDispatcherType() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getAuthType() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public Cookie[] getCookies() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public long getDateHeader(String name) {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public String getHeader(String name) {
		return this.request.headers().get(name).toString();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration<String> getHeaders(String name) {
		List l = this.request.headers().getAll(name);
		return Collections.enumeration(l);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration<String> getHeaderNames() {
		List l = this.request.headers().namesList();
		return Collections.enumeration(l);
	}

	@Override
	public int getIntHeader(String name) {
		String value = getHeader(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return -1;
            }
        } else {
            return -1;
        }
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public String changeSessionId() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public void login(String username, String password) throws ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public void logout() throws ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		throw new OIUEException(StatusResult._service_can_not_found, "Unimplemented method！");
	}

}
