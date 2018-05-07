package org.oiue.service.buffer.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.oiue.service.buffer.BufferService;
import org.oiue.service.buffer.KeyToMany;
import org.oiue.service.buffer.KeyToOne;
import org.oiue.service.buffer.KeyToSpatial;
import org.oiue.service.buffer.KeyToSpatialMerge;
import org.oiue.service.buffer.KeyToSpatialObject;
import org.oiue.service.buffer.KeyToTree;
import org.oiue.service.buffer.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class BufferServiceImpl implements BufferService, Serializable {
	private Logger logger;
	private static Map<String, Object> hmBuffer = null;
	
	public BufferServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
		hmBuffer = new ConcurrentHashMap<String, Object>();
	}
	
	@Override
	public Map<String, Object> getHashMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("get buffer hashmap");
		}
		
		return hmBuffer;
	}
	
	@Override
	public Object put(String name, Type bufferType) {
		if (logger.isDebugEnabled()) {
			logger.debug("put buffer name = " + name + " ,type = " + bufferType);
		}
		hmBuffer.remove(name);
		Object obj = null;
		switch (bufferType) {
			case KeyToOne:
				obj = new KeyToOne();
				break;
			case KeyToMany:
				obj = new KeyToMany();
				break;
			case KeyToSpatial:
				obj = new KeyToSpatial();
				break;
			case KeyToTree:
				obj = new KeyToTree();
				break;
			default:
				logger.error("put buffer error, type not support, name = " + name + " ,type = " + bufferType);
		}
		
		if (obj != null) {
			hmBuffer.put(name, obj);
		}
		return obj;
	}
	
	@Override
	public Object get(String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("get buffer name = " + name + ", result = " + hmBuffer.containsKey(name));
		}
		return hmBuffer.get(name);
	}
	
	@Override
	public Object remove(String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove buffer name = " + name);
		}
		return hmBuffer.remove(name);
	}
	
	@Override
	public void put(String name, String key, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("put name = " + name + ", key = " + key + ", value = " + value);
		}
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			((KeyToOne) obj).put(key, value);
		} else if (obj instanceof KeyToMany) {
			((KeyToMany) obj).put(key, value);
		} else {
			logger.error("buffer type not support put(key, value), name = " + name + ", key = " + key + ", value = " + value);
		}
	}
	
	@Override
	public void put(String name, String key, Object value, Type bufferType) {
		if (logger.isDebugEnabled()) {
			logger.debug("put name = " + name + ", key = " + key + ", value = " + value + ", type = " + bufferType);
		}
		Object obj = hmBuffer.get(name);
		if (obj == null) {
			obj = put(name, bufferType);
		}
		if (obj instanceof KeyToOne) {
			((KeyToOne) obj).put(key, value);
		} else if (obj instanceof KeyToMany) {
			((KeyToMany) obj).put(key, value);
		}
	}
	
	@Override
	public void put(String name, String key, double x, double y, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("put name = " + name + ", key = " + key + ", x = " + x + ", y = " + y + ", value = " + value);
		}
		Object obj = hmBuffer.get(name);
		if (obj == null) {
			obj = put(name, Type.KeyToSpatial);
		}
		if (obj instanceof KeyToSpatial) {
			((KeyToSpatial) obj).put(key, x, y, value);
		}
	}
	
	@Override
	public void put(String name, String key, String parentKey, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("put name = " + name + ", key = " + key + ", parentKey = " + parentKey + ", value = " + value);
		}
		Object obj = hmBuffer.get(name);
		if (obj == null) {
			obj = put(name, Type.KeyToTree);
		}
		if (obj instanceof KeyToTree) {
			((KeyToTree) obj).put(key, parentKey, value);
		}
	}
	
	// public Object get(String name, String key,String parentKey) {
	// /*
	// * if (System.currentTimeMillis() > 1317398400000L) { return null; }
	// */
	// if (logger.isDebugEnabled()) {
	// logger.debug("get name = " + name + ", key = " + key);
	// }
	// Object obj = hmBuffer.get(name);
	// if (obj instanceof KeyToTree) {
	// return ((KeyToTree) obj).get(key);
	// }
	// return null;
	// }
	@Override
	public Object get(String name, String key) {
		/*
		 * if (System.currentTimeMillis() > 1317398400000L) { return null; }
		 */
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", key = " + key);
		}
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			return ((KeyToOne) obj).get(key);
		} else if (obj instanceof KeyToMany) {
			return ((KeyToMany) obj).get(key);
		} else if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(key);
		} else if (obj instanceof KeyToTree) {
			return ((KeyToTree) obj).get(key);
		}
		return null;
	}
	
	@Override
	public List<Object> get(String name, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", match count = " + matchSet.size());
		}
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			return ((KeyToOne) obj).get(matchSet);
		} else if (obj instanceof KeyToMany) {
			return ((KeyToMany) obj).get(matchSet);
		} else if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(matchSet);
		} else if (obj instanceof KeyToTree) {
			return ((KeyToTree) obj).get(matchSet);
		}
		return null;
	}
	
	@Override
	public List<Object> get(String name, double x1, double x2, double y1, double y2, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2 + ", match count = " + matchSet.size());
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, matchSet);
		}
		return null;
	}
	
	@Override
	public List<Object> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2) + ", match count = " + matchSet.size());
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, matchSet);
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> get(String name, double x1, double x2, double y1, double y2, double dx, double dy, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2 + ", dx = " + dx + ", dy = " + dy + ", match count = " + matchSet.size());
		}
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, dx, dy, matchSet);
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2) + ", dx = " + dx + ", dy = " + dy + ", match count = " + matchSet.size());
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, dx, dy, matchSet);
		}
		return null;
	}
	
	@Override
	public List<Object> get(String name, double x1, double x2, double y1, double y2) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2);
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2);
		}
		return null;
	}
	
	@Override
	public List<Object> get(String name, double[] x1, double[] x2, double[] y1, double[] y2) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2));
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2);
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> get(String name, double x1, double x2, double y1, double y2, double dx, double dy) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2 + ", dx = " + dx + ", dy = " + dy);
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, dx, dy);
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy) {
		if (logger.isDebugEnabled()) {
			logger.debug("get name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2) + ", dx = " + dx + ", dy = " + dy);
		}
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).get(x1, x2, y1, y2, dx, dy);
		}
		return null;
	}
	
	@Override
	public boolean contains(String name, String key) {
		if (key == null)
			return false;
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			return ((KeyToOne) obj).contains(key);
		} else if (obj instanceof KeyToMany) {
			return ((KeyToMany) obj).contains(key);
		} else if (obj instanceof KeyToSpatial) {
			return ((KeyToSpatial) obj).contains(key);
		} else if (obj instanceof KeyToTree) {
			return ((KeyToTree) obj).contains(key);
		}
		return false;
	}
	
	@Override
	public boolean contains(String nameKeyToMany, String key, String value) {
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			return ((KeyToMany) obj).contains(key, value);
		}
		return false;
	}
	
	@Override
	public List<Object> getRelation(String nameKeyToManyOrTree, String key, String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("get relation key to many or tree name = " + nameKeyToManyOrTree + ", key = " + key + ", name = " + name);
		}
		
		Object obj = hmBuffer.get(nameKeyToManyOrTree);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				return get(name, matchSet);
			}
		} else if (obj instanceof KeyToTree) {
			KeyToTree keyToTree = (KeyToTree) obj;
			List<Object> matchList = keyToTree.get(key);
			if (matchList != null) {
				Set<Object> matchSet = new HashSet<Object>();
				matchSet.addAll(matchList);
				return get(name, matchSet);
			}
		}
		return null;
	}
	
	@Override
	public List<Object> getRelation(String nameKeyToMany, String key, String name, double x1, double x2, double y1, double y2) {
		if (logger.isDebugEnabled()) {
			logger.debug("get relation key to many name = " + nameKeyToMany + ", key = " + key + ", name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2);
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				return get(name, x1, x2, y1, y2, matchSet);
			}
		}
		return null;
	}
	
	@Override
	public List<Object> getRelation(String nameKeyToMany, String key, String name, double[] x1, double[] x2, double[] y1, double[] y2) {
		if (logger.isDebugEnabled()) {
			logger.debug("get relation key to many name = " + nameKeyToMany + ", key = " + key + ", name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2));
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				return get(name, x1, x2, y1, y2, matchSet);
			}
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> getRelation(String nameKeyToMany, String key, String name, double x1, double x2, double y1, double y2, double dx, double dy) {
		if (logger.isDebugEnabled()) {
			logger.debug("get relation key to many name = " + nameKeyToMany + ", key = " + key + ", name = " + name + ", x1 = " + x1 + ", x2 = " + x2 + ", y1 = " + y1 + ", y2 = " + y2 + ", dx = " + dx + ", dy = " + dy);
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				return get(name, x1, x2, y1, y2, dx, dy, matchSet);
			}
		}
		return null;
	}
	
	@Override
	public List<KeyToSpatialMerge> getRelation(String nameKeyToMany, String key, String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy) {
		if (logger.isDebugEnabled()) {
			logger.debug("get relation key to many name = " + nameKeyToMany + ", key = " + key + ", name = " + name + ", x1 = " + doubleArrayToString(x1) + ", x2 = " + doubleArrayToString(x2) + ", y1 = " + doubleArrayToString(y1) + ", y2 = " + doubleArrayToString(y2) + ", dx = " + dx + ", dy = " + dy);
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				return get(name, x1, x2, y1, y2, dx, dy, matchSet);
			}
		}
		return null;
	}
	
	@Override
	public void remove(String name, String key) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove name = " + name + ", key = " + key);
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			((KeyToOne) obj).remove(key);
		} else if (obj instanceof KeyToMany) {
			((KeyToMany) obj).remove(key);
		} else if (obj instanceof KeyToSpatial) {
			((KeyToSpatial) obj).remove(key);
		} else if (obj instanceof KeyToTree) {
			((KeyToTree) obj).remove(key);
		}
	}
	
	@Override
	public void remove(String name, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove name = " + name + ", match count = " + matchSet.size());
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToOne) {
			((KeyToOne) obj).remove(matchSet);
		} else if (obj instanceof KeyToMany) {
			((KeyToMany) obj).remove(matchSet);
		} else if (obj instanceof KeyToSpatial) {
			((KeyToSpatial) obj).remove(matchSet);
		} else if (obj instanceof KeyToTree) {
			((KeyToTree) obj).remove(matchSet);
		}
	}
	
	@Override
	public void remove(String name, String key, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove name = " + name + ", key = " + key + ", value = " + value);
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToMany) {
			((KeyToMany) obj).remove(key, value);
		}
	}
	
	@Override
	public void remove(String name, String key, Set<Object> matchSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove name = " + name + ", key = " + key + ", match count = " + matchSet.size());
		}
		
		Object obj = hmBuffer.get(name);
		if (obj instanceof KeyToMany) {
			((KeyToMany) obj).remove(key, matchSet);
		}
	}
	
	@Override
	public void removeRelation(String nameKeyToMany, String key, String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("reomve relation key to many name = " + nameKeyToMany + ", key = " + key + ", name = " + name);
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			if (matchSet != null) {
				Object remove = hmBuffer.get(name);
				if (remove instanceof KeyToOne) {
					((KeyToOne) remove).remove(matchSet);
				} else if (remove instanceof KeyToMany) {
					((KeyToMany) remove).remove(matchSet);
				} else if (remove instanceof KeyToSpatial) {
					((KeyToSpatial) remove).remove(matchSet);
				} else if (remove instanceof KeyToTree) {
					((KeyToTree) remove).remove(matchSet);
				}
			}
		}
	}
	
	@Override
	public void removeRelation(String nameKeyToMany, String key, String removeNameKeyToMany, String removeKey) {
		if (logger.isDebugEnabled()) {
			logger.debug("reomve relation key to many name = " + nameKeyToMany + ", key = " + key + ", remove name = " + removeNameKeyToMany + ", remove key = " + removeKey);
		}
		
		Object obj = hmBuffer.get(nameKeyToMany);
		if (obj instanceof KeyToMany) {
			KeyToMany oneToMany = (KeyToMany) obj;
			Set<Object> matchSet = oneToMany.get(key);
			
			if (matchSet != null) {
				Object remove = hmBuffer.get(removeNameKeyToMany);
				if (remove instanceof KeyToMany) {
					((KeyToMany) remove).remove(removeKey, matchSet);
				}
			}
		}
	}
	
	public static String encodeNameOrKey(String string) {
		if (string == null || string.length() == 0) {
			return "";
		}
		
		char c = 0;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		for (int i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch (c) {
				case ',':
					sb.append('\'');
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String decodeNameOrKey(String string) {
		if (string == null || string.length() == 0) {
			return "";
		}
		char b = 0;
		char c = 0;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		for (int i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\'':
					if (b == '\\') {
						sb.setCharAt(sb.length() - 1, ',');
					} else {
						sb.append(c);
					}
					break;
				case '\\':
					if (b == '\\') {
						c = 0;
					} else {
						sb.append(c);
					}
					break;
				case 'n':
					if (b == '\\') {
						sb.setCharAt(sb.length() - 1, '\n');
					} else {
						sb.append(c);
					}
					break;
				case 'r':
					if (b == '\\') {
						sb.setCharAt(sb.length() - 1, '\r');
					} else {
						sb.append(c);
					}
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String encodeValue(String string) {
		if (string == null || string.length() == 0) {
			return "";
		}
		char c = 0;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		for (int i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch (c) {
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String decodeValue(String string) {
		if (string == null || string.length() == 0) {
			return "";
		}
		char b = 0;
		char c = 0;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		for (int i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
					if (b == '\\') {
						c = 0;
					} else {
						sb.append(c);
					}
					break;
				case 'n':
					if (b == '\\') {
						sb.setCharAt(sb.length() - 1, '\n');
					} else {
						sb.append(c);
					}
					break;
				case 'r':
					if (b == '\\') {
						sb.setCharAt(sb.length() - 1, '\r');
					} else {
						sb.append(c);
					}
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static Object toObj(Object object) throws JSONException {
		if (object instanceof Iterable<?>) {
			List rtnL = new ArrayList();
			for (Iterator iterator = ((Iterable) object).iterator(); iterator.hasNext();) {
				Object type = iterator.next();
				rtnL.add(toObj(type));
			}
			return rtnL;
		} else if (object instanceof Map<?, ?>) {
			Map rtnM = new HashMap();
			for (Iterator iterator = ((Map) object).keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();
				rtnM.put(key, toObj(((Map) object).get(key)));
			}
			return rtnM;
		} else if (object instanceof KeyToSpatial) {
			return ((KeyToSpatial) object).toObj();
		} else if (object instanceof KeyToSpatialMerge) {
			return ((KeyToSpatialMerge) object).toObj();
		} else if (object instanceof KeyToSpatialObject) {
			return ((KeyToSpatialObject) object).toObj();
		} else {
			return object;
		}
		
	}
	
	private String doubleArrayToString(double[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			sb.append(",");
			sb.append(array[i]);
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public void swap(String nameA, String nameB) {
		if (logger.isDebugEnabled()) {
			logger.debug("swap buffer, a name = " + nameA + ", b name = " + nameB);
		}
		Object a = hmBuffer.get(nameA);
		Object b = hmBuffer.get(nameB);
		
		if (b != null) {
			hmBuffer.put(nameA, b);
		} else {
			hmBuffer.remove(nameA);
		}
		
		if (a != null) {
			hmBuffer.put(nameB, a);
		} else {
			hmBuffer.remove(nameB);
		}
	}
}
