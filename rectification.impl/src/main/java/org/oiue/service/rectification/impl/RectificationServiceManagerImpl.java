package org.oiue.service.rectification.impl;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.rectification.RectificationService;
import org.oiue.service.rectification.RectificationServiceManager;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class RectificationServiceManagerImpl implements RectificationServiceManager, Serializable {
	private Logger logger;
	private String rectification_type = "rectificationType";
	private String rectification_default = "buffer";
	
	private Map<String, RectificationService> rectifications = new HashMap<>();
	
	public RectificationServiceManagerImpl(LogService logService) {
		logger = logService.getLogger(getClass());
	}
	
	public void updated(Dictionary<String, ?> props) {
		String rectification_type = props.get("rectificationType") + "";
		if (!StringUtil.isEmptys(rectification_type)) {
			this.rectification_type = rectification_type;
		}
		String rectification_default = props.get("rectificationCache") + "";
		if (!StringUtil.isEmptys(rectification_default)) {
			this.rectification_default = rectification_default;
		}
	}
	
	@Override
	public RectificationService getRectificationService(String arg0) {
		return null;
	}
	
	@Override
	public boolean registerRectificationService(String arg0, RectificationService arg1) {
		return false;
	}
	
	@Override
	public boolean unRegisterRectificationService(String arg0) {
		return false;
	}
	
}