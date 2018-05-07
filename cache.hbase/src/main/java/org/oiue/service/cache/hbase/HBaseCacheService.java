package org.oiue.service.cache.hbase;

import java.io.Serializable;
import java.util.Dictionary;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

@SuppressWarnings({ "serial" })
public class HBaseCacheService implements CacheService, Serializable {
	
	private Logger logger;
	
	public HBaseCacheService(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public void put(String name, Object object, Type type) {
		if (type == Type.ONE) {
		}
	}
	
	@Override
	public void put(String name, String key, Object object, Type type) {
		if (type == Type.ONE) {
		} else if (type == Type.MANY) {
		}
	}
	
	@Override
	public void put(String name, Object object, Type type, int expire) {
		
	}
	
	@Override
	public void put(String name, String key, Object object, Type type, int expire) {
		
	}
	
	@Override
	public Object get(String name) {
		return null;
	}
	
	@Override
	public Object get(String name, String key) {
		return null;
	}
	
	@Override
	public long delete(String name) {
		return 0l;
	}
	
	@Override
	public long delete(String name, String... keys) {
		return 0l;
	}
	
	@Override
	public boolean exists(String name) {
		return false;
	}
	
	public void updated(Dictionary<String, ?> dict) {
		try {
			
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	
	@Override
	public boolean contains(String name, String... keys) {
		return false;
	}
	
	@Override
	public void put(String name, String key, Type type, Object... objects) {
		
	}
	
	@Override
	public void swap(String nameA, String nameB) {
		
	}
	
}
