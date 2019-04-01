package org.oiue.service.action.http.resource;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.tools.StatusResult;

public interface ResourceFilter extends Serializable {
	StatusResult doFilter(HttpServletRequest request, HttpServletResponse response);
	void setServletContext(ServletContext httpContext);
}
