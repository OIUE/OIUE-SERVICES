package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("serial")
public class KeyToOne implements Serializable {
	private Map<Object, Object> hashMap = new ConcurrentHashMap<Object, Object>();

	public Map<Object, Object> getHashMap() {
		return hashMap;
	}

	public void put(Object key, Object value) {
		hashMap.put(key, value);
	}

	public void remove(Object key) {
		hashMap.remove(key);
	}

	public void remove(Set<Object> matchSet) {
		for (Object key : matchSet) {
			hashMap.remove(key);
		}
	}

	public Object get(Object key) {
		return hashMap.get(key);
	}

	public List<Object> get(Set<Object> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : matchSet) {
			Object obj = hashMap.get(object);
			if (obj != null) {
				list.add(obj);
			}
		}
		return list;
	}

	public List<Object> find(Object like) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : hashMap.keySet()) {
			if (object.toString().indexOf(like.toString()) >= 0) {
				list.add(hashMap.get(object));
			}
		}
		return list;
	}

	public boolean contains(Object key) {
		return hashMap.containsKey(key);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		boolean first = true;
		stringBuffer.append("{\"t\":\"o\",");
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
			stringBuffer.append(":");

			Object objv = hashMap.get(obj);
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
		stringBuffer.append("}}");
		return stringBuffer.toString();
	}
}
