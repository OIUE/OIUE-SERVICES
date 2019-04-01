package org.oiue.service.action.http.netty;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import io.netty.buffer.ReadOnlyByteBuf;
import io.netty.handler.codec.http.DefaultHttpContent;
import lombok.Data;

public class OiueHttpServiceImpl implements HttpService {
	private static Map<String, ServletParent> servlets = new HashMap<>();

	@SuppressWarnings("rawtypes")
	@Override
	public void registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context)
			throws ServletException, NamespaceException {
		synchronized (servlets) {
			if (servlets.containsKey(alias)) {
				throw new RuntimeException();
			}
		}
		ServletParent sp = new ServletParent();
		sp.setAlias(alias);
		sp.setServlet(servlet);
		sp.setInitparams(initparams);
		sp.setContext(context);
		synchronized (servlets) {
			servlets.put(alias, sp);
		}
	}
	public static ServletParent getServlet(String alias) {
		return servlets.get(alias);
	}

	@Override
	public void registerResources(String alias, String name, HttpContext context) throws NamespaceException {

	}

	@Override
	public void unregister(String alias) {
		synchronized (servlets) {
			if (servlets.containsKey(alias))
				servlets.remove(alias);
		}
	}

	@Override
	public HttpContext createDefaultHttpContext() {
		return null;
	}

	@Data
	@SuppressWarnings("rawtypes")
	class ServletParent {
		String alias;
		Servlet servlet;
		Dictionary initparams;
		HttpContext context;
	}

	class ServletContextImpl implements ServletContext {
		HttpContext context;

		public ServletContextImpl(HttpContext context) {
			this.context = context;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public ServletContext getContext(String uripath) {
			return null;
		}

		@Override
		public int getMajorVersion() {
			return 0;
		}

		@Override
		public int getMinorVersion() {
			return 0;
		}

		@Override
		public int getEffectiveMajorVersion() {
			return 0;
		}

		@Override
		public int getEffectiveMinorVersion() {
			return 0;
		}

		@Override
		public String getMimeType(String file) {
			return null;
		}

		@Override
		public Set<String> getResourcePaths(String path) {
			return null;
		}

		@Override
		public URL getResource(String path) throws MalformedURLException {
			return context.getResource(path);
		}

		@Override
		public InputStream getResourceAsStream(String path) {
			return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			return null;
		}

		@Override
		public RequestDispatcher getNamedDispatcher(String name) {
			return null;
		}

		@Override
		public Servlet getServlet(String name) throws ServletException {
			return null;
		}

		@Override
		public Enumeration<Servlet> getServlets() {
			return null;
		}

		@Override
		public Enumeration<String> getServletNames() {
			return null;
		}

		@Override
		public void log(String msg) {

		}

		@Override
		public void log(Exception exception, String msg) {

		}

		@Override
		public void log(String message, Throwable throwable) {

		}

		@Override
		public String getRealPath(String path) {
			return null;
		}

		@Override
		public String getServerInfo() {
			return null;
		}

		@Override
		public String getInitParameter(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return null;
		}

		@Override
		public boolean setInitParameter(String name, String value) {
			return false;
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return null;
		}

		@Override
		public void setAttribute(String name, Object object) {

		}

		@Override
		public void removeAttribute(String name) {

		}

		@Override
		public String getServletContextName() {
			return null;
		}

		@Override
		public Dynamic addServlet(String servletName, String className) {
			return null;
		}

		@Override
		public Dynamic addServlet(String servletName, Servlet servlet) {
			return null;
		}

		@Override
		public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
			return null;
		}

		@Override
		public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
			return null;
		}

		@Override
		public ServletRegistration getServletRegistration(String servletName) {
			return null;
		}

		@Override
		public Map<String, ? extends ServletRegistration> getServletRegistrations() {
			return null;
		}

		@Override
		public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
			return null;
		}

		@Override
		public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
			return null;
		}

		@Override
		public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName,
				Class<? extends Filter> filterClass) {
			return null;
		}

		@Override
		public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
			return null;
		}

		@Override
		public FilterRegistration getFilterRegistration(String filterName) {
			return null;
		}

		@Override
		public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
			return null;
		}

		@Override
		public SessionCookieConfig getSessionCookieConfig() {
			return null;
		}

		@Override
		public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

		}

		@Override
		public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
			return null;
		}

		@Override
		public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
			return null;
		}

		@Override
		public void addListener(String className) {

		}

		@Override
		public <T extends EventListener> void addListener(T t) {

		}

		@Override
		public void addListener(Class<? extends EventListener> listenerClass) {

		}

		@Override
		public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
			return null;
		}

		@Override
		public JspConfigDescriptor getJspConfigDescriptor() {
			return null;
		}

		@Override
		public ClassLoader getClassLoader() {
			return null;
		}

		@Override
		public void declareRoles(String... roleNames) {

		}

		@Override
		public String getVirtualServerName() {
			return null;
		}
	}
}