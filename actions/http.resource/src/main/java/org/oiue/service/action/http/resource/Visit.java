package org.oiue.service.action.http.resource;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Visit extends Serializable {
	void visit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
