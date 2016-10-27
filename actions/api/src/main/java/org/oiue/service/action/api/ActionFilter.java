package org.oiue.service.action.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface ActionFilter extends Serializable {
	StatusResult doFilter(Map per);
}
