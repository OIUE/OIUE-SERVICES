package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.oiue.tools.json.JSONUtil;


@SuppressWarnings("serial")
public class KeyToMany implements Serializable {
	private Map<Object, Set<Object>> hashMap =new ConcurrentHashMap<Object, Set<Object>>();

	public Map<Object, Set<Object>> getHashMap() {
		return hashMap;
	}

	public void put(Object key, Object value) {
		Set<Object> hashSet = hashMap.get(key);
		if (hashSet == null) {
			hashSet = new CopyOnWriteArraySet<Object>();
			hashMap.put(key, hashSet);
		}
		hashSet.add(value);
	}

	public void remove(Object key) {
		hashMap.remove(key);
	}

	public void remove(Set<Object> matchSet) {
		for (Object key : matchSet) {
			hashMap.remove(key);
		}
	}

	public void remove(Object key, Object value) {
		Set<Object> hashSet = hashMap.get(key);
		if (hashSet != null) {
			hashSet.remove(value);
			if (hashSet.isEmpty()) {
				hashMap.remove(key);
			}
		}
	}

	public void remove(Object key, Set<Object> matchSet) {
		Set<Object> hashSet = hashMap.get(key);
		if (!hashSet.equals(matchSet)) {
			if (hashSet != null) {
				for (Object e : matchSet) {
					hashSet.remove(e);
				}
				if (hashSet.isEmpty()) {
					hashMap.remove(key);
				}
			}
		}
	}

	public Set<Object> get(Object key) {
		return hashMap.get(key);
	}

	public List<Object> get(Set<Object> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : matchSet) {
			Set<Object> hashSet = hashMap.get(object);
			if (hashSet != null) {
				list.addAll(hashSet);
			}
		}
		return list;
	}

	public List<Object> find(Object key, Object like) {
		List<Object> list = new ArrayList<Object>();
		Set<Object> hashSet = hashMap.get(key);
		if (hashSet != null) {
			for (Object object : hashSet) {
				if (object.toString().indexOf(like.toString()) >= 0) {
					list.add(object);
				}
			}
		}
		return list;
	}

	public boolean contains(Object key) {
		return hashMap.containsKey(key);
	}

	public boolean contains(Object key, Object value) {
		return hashMap.containsKey(key) && hashMap.get(key).contains(value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		boolean first = true;
		stringBuffer.append("{\"t\":\"m\",");
		stringBuffer.append("\"v\":{");
		for (Object obj : hashMap.keySet()) {
			if (first) {
				first = false;
			} else {
				stringBuffer.append(",");
			}
			if (obj instanceof String) {
				stringBuffer.append(JSONUtil.getJSONString((String) obj));
			} else {
				stringBuffer.append(obj);
			}
			stringBuffer.append(":[");

			boolean firstv = true;
			for (Object objv : hashMap.get(obj)) {
				if (firstv) {
					firstv = false;
				} else {
					stringBuffer.append(",");
				}
				if (objv instanceof String) {
					stringBuffer.append(JSONUtil.getJSONString((String) objv));
				} else if (objv instanceof Map){
					stringBuffer.append(JSONUtil.parserToStr((Map)objv));
				} else if (objv instanceof List){
					stringBuffer.append(JSONUtil.parserToStr((List)objv));
				} else {
					stringBuffer.append(objv);
				}
			}
			stringBuffer.append("]");
		}
		stringBuffer.append("}}");
		return stringBuffer.toString();
	}
}
