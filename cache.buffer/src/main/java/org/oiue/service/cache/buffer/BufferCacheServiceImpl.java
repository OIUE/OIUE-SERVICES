package org.oiue.service.cache.buffer;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.buffer.KeyToMany;
import org.oiue.service.buffer.KeyToOne;
import org.oiue.service.buffer.KeyToSpatial;
import org.oiue.service.buffer.KeyToTree;
import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

@SuppressWarnings("unused")
public class BufferCacheServiceImpl implements CacheService {
	
	private Logger logger;
	private BufferService bufferService;
	
	public BufferCacheServiceImpl(LogService logService, BufferService bufferService) {
		logger = logService.getLogger(this.getClass());
		this.bufferService = bufferService;
	}
	
	@Override
	public void put(String name, Object object, Type type) {
		// throw new RuntimeException("BufferCacheServiceImpl error put(String name, Object object, Type type) ");
		this.put(name, object + "", object, type);
	}
	
	@Override
	public void put(String name, String key, Object object, Type type) {
		bufferService.put(name, key, object, type == Type.MANY ? org.oiue.service.buffer.Type.KeyToMany : org.oiue.service.buffer.Type.KeyToOne);
	}
	
	@Override
	public void put(String name, Object object, Type type, int expire) {
		throw new RuntimeException("BufferCacheServiceImpl error put(String name, Object object, Type type, int expire) ");
	}
	
	@Override
	public void put(String name, String key, Object object, Type type, int expire) {
		throw new RuntimeException("BufferCacheServiceImpl error put(String name, String key,Object object, Type type, int expire) ");
	}
	
	@Override
	public Object get(String name) {
		// throw new RuntimeException("BufferCacheServiceImpl error get(String name)");
		Object obj = bufferService.get(name);
		Object ro = null;
		if (obj instanceof KeyToOne) {
			ro = ((KeyToOne) obj).getHashMap();
		} else if (obj instanceof KeyToMany) {
			ro = ((KeyToMany) obj).getHashMap();
		} else if (obj instanceof KeyToSpatial) {
			ro = ((KeyToSpatial) obj).getHashMap();
		} else if (obj instanceof KeyToTree) {
			ro = ((KeyToTree) obj).getHashMap();
		}
		return ro;
	}
	
	@Override
	public Object get(String name, String key) {
		return bufferService.get(name, key);
	}
	
	@Override
	public long delete(String name) {
		bufferService.remove(name);
		return 0;
	}
	
	@Override
	public long delete(String name, String... keys) {
		if (keys.length == 1) {
			bufferService.remove(name, keys[0]);
		} else {
			for (String key : keys) {
				bufferService.remove(name, key);
			}
		}
		return 0;
	}
	
	@Override
	public boolean exists(String name) {
		return false;
	}
	
	@Override
	public boolean contains(String name, String... keys) {
		return keys.length == 1 ? bufferService.contains(name, keys[0]) : keys.length == 2 ? bufferService.contains(name, keys[0], keys[1]) : false;
	}
	
	@Override
	public void put(String name, String key, Type type, Object... objects) {
		bufferService.put(name, key, objects, org.oiue.service.buffer.Type.KeyToOne);
	}
	
	@Override
	public void swap(String nameA, String nameB) {
		bufferService.swap(nameA, nameB);
	}
	
}
