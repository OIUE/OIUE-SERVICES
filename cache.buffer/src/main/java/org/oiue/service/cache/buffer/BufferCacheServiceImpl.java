package org.oiue.service.cache.buffer;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.buffer.KeyToMany;
import org.oiue.service.buffer.KeyToOne;
import org.oiue.service.buffer.KeyToSpatial;
import org.oiue.service.buffer.KeyToTree;
import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

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
		Object obj = bufferService.get(name);
		CacheObject co = new CacheObject(object);
		if (obj == null) {
			obj = bufferService.put(name, type == Type.MANY ? org.oiue.service.buffer.Type.KeyToMany : org.oiue.service.buffer.Type.KeyToOne);
			if (obj instanceof KeyToOne) {
				((KeyToOne) obj).put(key, co);
			} else if (obj instanceof KeyToMany) {
				((KeyToMany) obj).put(key, co);
			}
			new CacheExpire(name, expire).start();
		} else {
			bufferService.put(name, key, co);
		}
//		throw new RuntimeException("BufferCacheServiceImpl error put(String name, String key,Object object, Type type, int expire) ");
	}
	
	class CacheObject {
		public CacheObject(Object object2) {
			this.object = object2;
		}
		
		Object object;
		long cache_time = System.currentTimeMillis() / 1000;
		
		@Override
		public String toString() {
			return cache_time+":"+object;
		}
	}
	
	class CacheExpire extends Thread {
		private int expire;
		private String name;
		
		public CacheExpire(String name, int expire) {
			this.name = name;
			this.expire = expire;
		}
		
		@Override
		public void run() {
			Object objt = bufferService.get(name);
			while (true) {
				long utc = System.currentTimeMillis() / 1000;
				try {
					if (objt instanceof KeyToOne) {
						KeyToOne ko = (KeyToOne) objt;
						Map<Object, Object> map = ko.getHashMap();
						for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
							Entry<Object, Object> et = (Entry<Object, Object>) iterator.next();
							CacheObject co = (CacheObject) et.getValue();
							if (utc - co.cache_time > expire) {
								logger.debug("remove cache[{}] by expire[{}]", co,expire);
								iterator.remove();
							}
						}
					} else if (objt instanceof KeyToMany) {
						KeyToMany km = (KeyToMany) objt;
						Map<Object, Set<Object>> map = km.getHashMap();
						
						for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
							Entry<Object, Set<Object>> et = (Entry<Object, Set<Object>>) iterator.next();
							Set<Object> cos = (Set<Object>) et.getValue();
							for (Iterator iterator2 = cos.iterator(); iterator2.hasNext();) {
								CacheObject co = (CacheObject) iterator2.next();
								if (co.cache_time + expire < utc) {
									iterator2.remove();
								}
							}
							if (cos.size() == 0) {
								iterator.remove();
							}
						}
					}
					
					try {
//						Thread.sleep(expire*1000-(System.currentTimeMillis() -utc*1000));
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					
				} catch (Throwable e) {}
			}
		}
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
		Object c = bufferService.get(name, key);
		
		return c instanceof CacheObject? ((CacheObject)c).object:c;
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
