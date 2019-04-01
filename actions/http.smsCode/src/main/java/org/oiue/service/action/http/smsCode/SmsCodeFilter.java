package org.oiue.service.action.http.smsCode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("serial")
public class SmsCodeFilter implements ActionFilter {
	protected static FactoryService _factoryService;
	protected static CacheServiceManager _cacheServiceManager;
	private List<String> modules;
	
	public SmsCodeFilter(FactoryService factoryService, CacheServiceManager cacheServiceManager) {
		_factoryService = factoryService;
		_cacheServiceManager = cacheServiceManager;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult sr = new StatusResult();
		
		String operation = MapUtil.getString(per, "operation");
		if (modules!=null&&modules.contains(operation)) {
			String tel = MapUtil.getString(per, "data.phoneNo");
			String code = MapUtil.getString(per, "data.code");
			if(code!=null)
				code=code.toLowerCase();
			
			Object smscode = _cacheServiceManager.get("_system_phone_code_", tel);
			
			if (smscode != null && smscode.equals(code)) {
				sr.setResult(StatusResult._SUCCESS_CONTINUE);
			} else {
				sr.setResult(StatusResult._data_error);
				sr.setDescription(error_desc);
			}
			
		} else
			sr.setResult(StatusResult._SUCCESS_CONTINUE);
		
		return sr;
	}
	
	private String error_desc;
	
	public void updated(Map props) {
		try {
			error_desc = MapUtil.getString(props, "error_desc", "验证码错误！");
		} catch (Exception e) {}
		try {
			modules = Arrays.asList(MapUtil.getString(props, "filter_modules", "17dcea54-44c3-49ba-8229-0fbd22ba1194,7d697034-a93e-489b-95f9-6b09791a27a0").split(","));
		} catch (Exception e) {}
	}
	
}
