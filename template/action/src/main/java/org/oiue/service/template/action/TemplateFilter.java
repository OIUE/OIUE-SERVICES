package org.oiue.service.template.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.server.Request;
import org.oiue.service.log.Logger;
import org.oiue.service.template.TemplateService;

public class TemplateFilter implements Filter {

    static TemplateService templateService;
    static Logger logger;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            ServletRequestWrapper srw = (ServletRequestWrapper) request;
            Request req = (Request) srw.getRequest();
            // System.out.println(req.getRequestURL());
            // System.out.println(req.getRootURL());
            // System.out.println(req.getRequestURI());
            // System.out.println(req.getPathInfo());
            // System.out.println(request.getCharacterEncoding());
            // System.out.println(request.getContentLength());
            // System.out.println(request.getContentLengthLong());
            // System.out.println(request.getContentType());
            // System.out.println(request.getLocalAddr());
            // System.out.println(request.getLocalName());
            // System.out.println(request.getLocalPort());
            // System.out.println(request.getProtocol());
            // System.out.println(request.getRemoteAddr());
            // System.out.println(request.getRemotePort());
            // System.out.println(request.getScheme());
            // System.out.println(request.getServerName());
            // System.out.println(request.getServerPort());
            // System.out.println(request.isAsyncStarted());
            // System.out.println(request.isAsyncSupported());
            // System.out.println(request.isSecure());
            // System.out.println(request.getAttributeNames());
            // System.out.println(request.getLocale());
            // System.out.println(request.getLocales());
            // System.out.println(request.getParameterMap());
            // System.out.println(request.getParameterNames());
            // System.out.println(request.getServletContext());
            // System.out.println(fc);
            String path = req.getRequestURI();
            if (path.startsWith("/tm")&&path.endsWith(".html")) {
                Map parameter = new HashMap<>();
                OutputStream os = response.getOutputStream();
                try {
                    parameter.put("system_out_type", os);

                    System.out.println(templateService.render(path.substring(3), parameter));
                    System.out.println(parameter);

                } catch (Throwable e2) {
                } finally {
                    if (os != null)
                        os.close();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

}
