package org.oiue.service.action.http.imageCode;

import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings("serial")
public class ImageCodeFilter implements ActionFilter {
	
	@SuppressWarnings("rawtypes")
	@Override
	public StatusResult doFilter(Map per) {
		StatusResult sr = new StatusResult();
		String modulename=MapUtil.getString(per, "modulename");
		String operation=MapUtil.getString(per, "operation");
		
		modulename=modulename==null?"":modulename.trim();
		operation=operation==null?"":operation.trim();

		Map data;
		data = (Map) per.get("data");
		
		if(data==null&&!"logout".equals(modulename)){
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("请求格式不正确！");
			return sr;
		}
		if("login".equals(modulename)){

			try {
				if (data.size() > 1) {
					String CheckCodeImage = data.get("checkCodeImage") + "";
					String Login_Image_Code = per.get("Login_Image_Code") + "";
					if (StringUtil.isEmptys(CheckCodeImage)||!CheckCodeImage.equalsIgnoreCase(Login_Image_Code)) {
						sr.setResult(StatusResult._pleaseLogin);
						sr.setDescription("登录错误！");
						per.put("exception", "验证码错误！");
						return sr;
					}

					sr.setResult(StatusResult._SUCCESS);
					per.put("msg", "验证码正确！");
						
					return sr;
				}
			} catch (Throwable e) {
				sr.setResult(StatusResult._pleaseLogin);
				sr.setDescription("验证码验证错误！");
				per.put("exception", ExceptionUtil.getCausedBySrcMsg(e));
				return sr;
			}
		}
		return sr;
	}
	
}
