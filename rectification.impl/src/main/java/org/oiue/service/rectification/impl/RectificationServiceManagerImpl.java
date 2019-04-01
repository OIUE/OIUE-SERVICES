package org.oiue.service.rectification.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.rectification.RectificationService;
import org.oiue.service.rectification.RectificationServiceManager;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

/**
 * 纠偏服务
 * @author every
 *
 */
@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class RectificationServiceManagerImpl implements RectificationServiceManager, Serializable {
	private Logger logger;
	private String rectification_type = "rectificationType";
	private String rectification_default = "tolingtu";
	
	private Map<String, RectificationService> rectifications = new HashMap<>();
	
	public RectificationServiceManagerImpl(LogService logService) {
		logger = logService.getLogger(getClass());
	}
	
	public void updated(Map<String, ?> props) {
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
	public RectificationService getRectificationService(String name) {
		return rectifications.get(name);
	}
	
	@Override
	public boolean registerRectificationService(String name, RectificationService rectification) {
		logger.info("register RectificationService :{} ", name);
		if (rectifications.containsKey(name)) {
			return false;
		} else {
			rectifications.put(name, rectification);
		}
		return true;
	}
	
	@Override
	public boolean unRegisterRectificationService(String name) {
		logger.info("register RectificationService :{} ", name);
		if (rectifications.containsKey(name)) {
			rectifications.remove(name);
			return true;
		}
		return false;
	}

	@Override
	public StatusResult convert(Map data) {
		String rectificationType=MapUtil.getString(data, rectification_type);
		if(StringUtil.isEmptys(rectificationType)) {
			rectificationType = rectification_default;
		}
			
		return rectifications.get(rectificationType).convert(data);
	}
	
}