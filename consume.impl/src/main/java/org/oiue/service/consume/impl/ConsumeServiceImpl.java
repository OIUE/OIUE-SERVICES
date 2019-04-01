package org.oiue.service.consume.impl;

import java.util.Map;
import java.util.Set;

import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.consume.ConsumeService;
import org.oiue.service.log.LogService;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.StatusResult;

public class ConsumeServiceImpl implements ConsumeService {
	private static final long serialVersionUID = 1L;
	
	public ConsumeServiceImpl(LogService logService, CacheServiceManager cache, OnlineService onlineService, BytesService bytesService) {
		
	}
	
	@Override
	public Object consume(Map data, Map permission, String tokenid) {
		return null;
	}
	
	@Override
	public Object unConsume(Map data, Map permission, String tokenid) {
		return null;
	}
	
	@Override
	public StatusResult consume(Online online, Map<String, Object> consume) {
		return null;
	}
	
	@Override
	public StatusResult unConsume(Online online, Map<String, Object> consume) {
		return null;
	}
	
	@Override
	public StatusResult unConsume(String tokenid) {
		return null;
	}
	
	@Override
	public StatusResult setDataBytoken(String tokenid, Map<String, Object> data) {
		return null;
	}
	
	@Override
	public StatusResult setDataByUserID(String userId, Map<String, Object> data) {
		return null;
	}
	
	@Override
	public StatusResult setDataByUserIDS(Set userIDS, Map<String, Object> data) {
		return null;
	}
	
	@Override
	public StatusResult setDataByOnline(Online online, Map<String, Object> data) {
		return null;
	}
	
	@Override
	public StatusResult setDataByOnline(Online online, byte[] data) {
		return null;
	}
	
	@Override
	public StatusResult setData(Map<String, Object> data) {
		return null;
	}
	
	@Override
	public void updated(Map<?, ?> props) {
		
	}
}
