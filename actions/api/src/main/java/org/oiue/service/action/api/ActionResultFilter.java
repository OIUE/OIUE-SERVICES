package org.oiue.service.action.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface ActionResultFilter extends Serializable {
	StatusResult doFilter(Map per, Object source_data);
}
