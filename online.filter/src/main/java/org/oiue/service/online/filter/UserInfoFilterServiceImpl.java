package org.oiue.service.online.filter;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionResultFilter;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;

public class UserInfoFilterServiceImpl implements ActionFilter, ActionResultFilter, Serializable {
	private static final long serialVersionUID = 1673038326858659630L;
	private OnlineService onlineService = null;
	private CacheServiceManager cache = null;
	public static final String _SYSTEM_ONLINE = "system.online";
	public static String _GLOBAL_CACHE = "redis";
	
	public UserInfoFilterServiceImpl(OnlineService onlineService,CacheServiceManager cache, LogService logService) {
		this.onlineService=onlineService;
		this.cache=cache;
	}
	
	@Override
	public StatusResult doFilter(Map per, Object source_data) {
//		String modulename = MapUtil.getString(per, "modulename");
		String operation = MapUtil.getString(per, "operation");
		if("81e48b36-3423-438d-bd3b-d250f23f71cf".equals(operation)){
			Map data = JSONUtil.parserStrToMap(source_data+"");
			String user_name= data.get("user_name")+"";
			String token = per.get("token")+"";
			Online online = onlineService.getOnlineByToken(token);
			online.setUser_name(user_name);
			online.getUser().put("user_name", user_name);
			onlineService.putOnline(online.getTokenId(), online);
		}
		StatusResult afr = new StatusResult();
		afr.setResult(StatusResult._SUCCESS);
		afr.setDescription("validate success");
		per.put("success", true);
		return afr;
	}
	@Override
	public StatusResult doFilter(Map per) {
//		String modulename = MapUtil.getString(per, "modulename");
//		String operation = MapUtil.getString(per, "operation");
		StatusResult afr = new StatusResult();
//		
//		if("e106e434-0796-4f4e-a719-51cea01ed804".equals(operation)){
//			Object data = per.get("data");
//			Map datam = null;
//			if(data instanceof Map){
//				datam=(Map) data;
//			}else if(data instanceof String&&((String) data).startsWith("{")){
//				datam=JSONUtil.parserStrToMap((String)data);
//			}else{
//				afr.setResult(StatusResult._ncriticalAbnormal);
//				afr.setDescription("error request!");
//				return afr;
//			}
//			String token=MapUtil.getString(per, "token");
//			Map user = onlineService.getOnlineByToken(token).getUser();
//			long now = System.currentTimeMillis();
//			
//			String tokenId ="a_"+ UUID.randomUUID().toString().replaceAll("-", "");
//			Online online = new OnlineImpl();
//			online.setTokenId(tokenId);
//			online.setType(Type.http);
//			String appToken=JWTUtil.encode(tokenId, new Date(now), new Date(4092599349000l), user);
//			online.setLastTime(4092599349000l);
//			online.setUser(user);
//			online.setUser_id(user.get("user_id") + "");
//			online.setUser_name(user.get("user_name") + "");
//			datam.put("application_id", tokenId);
//			datam.put("token", appToken);
//			per.put("data", datam);
//			
//			this.cache.put(_SYSTEM_ONLINE, tokenId, online, org.oiue.service.cache.Type.ONE);
//			this.cache.getCacheService(_GLOBAL_CACHE).put(_SYSTEM_ONLINE, tokenId, online.toString(), org.oiue.service.cache.Type.ONE);
//		}
		afr.setResult(StatusResult._SUCCESS);
		afr.setDescription("validate success");
		return afr;
	}
}
