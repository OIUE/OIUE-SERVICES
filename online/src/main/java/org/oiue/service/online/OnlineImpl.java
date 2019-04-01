package org.oiue.service.online;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("rawtypes")
public class OnlineImpl implements Online {
	
	private static final long serialVersionUID = -6943289104123830175L;
	private Object o;
	private String tokenId;
	private Map user;
	private String user_id;
	private String user_name;
	private Type type;
	private int status = 0;
	private long lastTime;
	private long loginTime;
	private String accessIp;
	
	public OnlineImpl() {
		loginTime = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		Map<Object, Object> temp = new HashMap<>();
		temp.put("tokenId", this.getTokenId());
		temp.put("user_id", this.getUser_id());
		temp.put("user_name", this.getUser_name());
		temp.put("type", this.getType());
		temp.put("lastTime", this.getLastTime());
		temp.put("loginTime", this.getLoginTime());
		temp.put("user", this.getUser());
		temp.put("accessIp", this.getAccessIp());
		
		return JSONUtil.parserToStr(temp);
	}
	
	@Override
	public void setStatus(Type type) {
		switch (type) {
			case apikey:
				status = status | 31; // 1 1111
				break;
			case tcp:
				status = status | 16; // 1 0000
				break;
			case webSocket:
				status = status | 8; // 0 1000
				break;
			case socketIo:
				status = status | 4; // 0 0100
				break;
			case udp:
				status = status | 2; // 0 0010
				break;
			case http:
				status = status | 1; // 0 0001
				break;
		}
	}
	
	@Override
	public void resetStatus(Type type) {
		switch (type) {
			case apikey:
				status = status ^ 31; // 1 1111
				break;
			case tcp:
				status = status ^ 16; // 1 0000
				break;
			case webSocket:
				status = status ^ 8; // 0 1000
				break;
			case socketIo:
				status = status ^ 4; // 0 0100
				break;
			case udp:
				status = status ^ 2; // 0 0010
				break;
			case http:
				status = status ^ 1; // 0 0001
				break;
		}
	}
	
	@Override
	public int getStatus() {
		return status;
	}
	
	@Override
	public Type getBestType() {
		return (status & 16) > 0 ? Type.tcp : (status & 8) > 0 ? Type.webSocket : (status & 8) > 0 ? Type.socketIo : (status & 8) > 0 ? Type.udp : Type.http;
	}
	
	@Override
	public Object getO() {
		return o;
	}
	
	@Override
	public String getToken() {
		long now = System.currentTimeMillis();
		return JWTUtil.encode(getTokenId(), new Date(now), new Date(now + OnlineDataField.online_timeout), getUser());
	}
	
	@Override
	public String getTokenId() {
		return tokenId;
	}
	
	@Override
	public Map getUser() {
		return user;
	}
	
	@Override
	public String getUser_id() {
		return user_id;
	}
	
	@Override
	public String getUser_name() {
		return user_name;
	}
	
	@Override
	public void setO(Object o) {
		this.o = o;
	}
	
	@Override
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
	
	@Override
	public void setUser(Map user) {
		this.user = user;
	}
	
	@Override
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	
	@Override
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public long getLastTime() {
		return lastTime;
	}
	
	@Override
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	@Override
	public long getLoginTime() {
		return loginTime;
	}
	
	@Override
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public String getAccessIp() {
		return accessIp;
	}
	
	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}
}