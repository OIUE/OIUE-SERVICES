/**
 *
 */
package org.oiue.service.action.filter.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionResultFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.security.SecurityUtil;
import org.oiue.tools.string.StringUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class SecurityFilterServiceImpl implements ActionFilter,ActionResultFilter, Serializable {
	private Logger logger;
	private List<String> unFilter_modulename = null;
	private Map<String, List<String>> unFilter_module_operation = new HashMap<>();
	private CacheServiceManager cacheService = null;
	private ActionService actionService = null;
	private OnlineService onlineService = null;
	private static Map keys = SecurityUtil.initKey();// global keys

	public SecurityFilterServiceImpl(LogService logService, CacheServiceManager cacheService, OnlineService onlineService, ActionService actionService) {
		logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
		this.cacheService = cacheService;
		this.onlineService = onlineService;
	}

	public void updated(Dictionary dict) {
		cacheService.put("security", "global", keys, Type.ONE);
		try {
			unFilter_modulename = new ArrayList<String>();
			String unFilter_modulenames = MapUtil.getString(dict,"unFilter_modulename","");
			if (!StringUtil.isEmptys(unFilter_modulenames)) {
				unFilter_modulename = StringUtil.Str2List(unFilter_modulenames, ",");
			}
			for (Iterator iterator = unFilter_modulename.iterator(); iterator.hasNext();) {
				String module = (String) iterator.next();
				try {
					String unFilter_modulename_operation = (String) dict.get(module);
					if (!StringUtil.isEmptys(unFilter_modulename_operation)) {
						unFilter_module_operation.put(module, StringUtil.Str2List(unFilter_modulename_operation, ","));
					}
				} catch (Throwable e) {}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("updateConfigure: unFilter_modulename  = " + unFilter_modulenames);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		actionService.registerActionFilter("securityFilter", this, -100);
		actionService.registerActionResultFilter("securityFilter", this, 100);
	}

	@Override
	public StatusResult doFilter(Map per) {
		String modulename = MapUtil.getString(per, "modulename");
		String token = MapUtil.getString(per, "token");
		String encrypt = MapUtil.getString(per, "encrypt");

		modulename = modulename == null ? "" : modulename.trim();
		token = StringUtil.isEmptys(token) ? null : token.trim();

		StatusResult afr = new StatusResult();
		if (modulename == null) {
			afr.setResult(StatusResult._ncriticalAbnormal);
			afr.setDescription("error request data!");
			return afr;
		}
		
		if("security".equals(modulename)){//拦截安全模块请求，分发公钥
			if(token==null){//未登录用户，分发全局公钥
				per.put("encryptKey", (new BASE64Encoder()).encode(SecurityUtil.getPublicKey(keys)));
			}else{//登录用户，分发私有公钥  每个用户仅持有一对公私秘钥，已有秘钥会被覆盖
				Map ukeys = SecurityUtil.initKey();
				cacheService.put("security", token, ukeys, Type.ONE);
				per.put("encryptKey", (new BASE64Encoder()).encode(SecurityUtil.getPublicKey(ukeys)));
			}
			afr.setResult(StatusResult._SUCCESS_OVER);
		}else if(!StringUtil.isEmptys(encrypt)){//拦截加密请求 进行解密
			try {
				byte[] cli_public_keys = new BASE64Decoder().decodeBuffer(encrypt);
				Object data = per.get("data");
				if(data instanceof String){
					byte[] s_data = new BASE64Decoder().decodeBuffer((String)data);
					
					if(token==null){
						byte[] privateKey = SecurityUtil.getPrivateKey(keys);
						byte[] es_data = SecurityUtil.decryptDH(s_data, cli_public_keys, privateKey);
						String j_data =  new String(es_data,"UTF-8");
						per.put("data", j_data.startsWith("{")?JSONUtil.parserStrToMap(j_data):JSONUtil.parserStrToList(j_data));
					}else{
						Map ukeys = (Map) cacheService.get("security", token);
						byte[] privateKey = SecurityUtil.getPrivateKey(keys);
						byte[] es_data = SecurityUtil.decryptDH(s_data, cli_public_keys, privateKey);
						String j_data =  new String(es_data,"UTF-8");
						per.put("data", j_data.startsWith("{")?JSONUtil.parserStrToMap(j_data):JSONUtil.parserStrToList(j_data));
					}
					afr.setResult(StatusResult._SUCCESS);
				}else{
					afr.setResult(StatusResult._data_error);
				}
				
			} catch (Exception e) {
				afr.setResult(StatusResult._data_error);
			}
		}else{//非安全模块及加密请求直接放过
			afr.setResult(StatusResult._SUCCESS);
		}
		return afr;
	}
	
	@Override
	public StatusResult doFilter(Map per, Object source_data) {
		String token = MapUtil.getString(per, "token");
		String modulename = MapUtil.getString(per, "modulename");
		String encrypt = MapUtil.getString(per, "encrypt");

		modulename = modulename == null ? "" : modulename.trim();
		token = StringUtil.isEmptys(token) ? null : token.trim();

		StatusResult afr = new StatusResult();
		if (modulename == null) {
			afr.setResult(StatusResult._ncriticalAbnormal);
			afr.setDescription("error request data!");
			return afr;
		}
		if(token != null&& !unFilter_module_operation.containsKey(modulename)&&!StringUtil.isEmptys(encrypt)){//登录用户，并且不是非加密模块
			Object data = per.get("data");
			try {
				Map ukeys = (Map) cacheService.get("security", token);
				byte[] privateKey = SecurityUtil.getPrivateKey(keys);
				byte[] cli_public_keys = new BASE64Decoder().decodeBuffer(encrypt);
				String j_data = data instanceof Map? JSONUtil.parserToStr((Map)data):JSONUtil.parserToStr((List)data);
				per.put("data",SecurityUtil.encryptDH(j_data.getBytes(), cli_public_keys, privateKey));
				afr.setResult(StatusResult._SUCCESS);
			} catch (Exception e) {
				afr.setResult(StatusResult._data_error);
			}
		}
		return afr;
	}
}
