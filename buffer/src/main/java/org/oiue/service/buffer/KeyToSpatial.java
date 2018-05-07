package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("serial")
public class KeyToSpatial implements Serializable {
	private Map<Object, KeyToSpatialObject> hashMap = new ConcurrentHashMap<Object, KeyToSpatialObject>();
	
	public Map<Object, KeyToSpatialObject> getHashMap() {
		return hashMap;
	}
	
	public void put(Object key, double x, double y, Object value) {
		KeyToSpatialObject obj = new KeyToSpatialObject();
		obj.setKey(key);
		obj.setX(x);
		obj.setY(y);
		obj.setValue(value);
		hashMap.put(key, obj);
	}
	
	public void remove(Object key) {
		hashMap.remove(key);
	}
	
	public void remove(Set<Object> matchSet) {
		for (Object key : matchSet) {
			hashMap.remove(key);
		}
	}
	
	public List<Object> get(double x1, double x2, double y1, double y2) {
		List<Object> list = new ArrayList<Object>();
		for (KeyToSpatialObject obj : hashMap.values()) {
			if ((obj.getX() >= x1) && (obj.getX() <= x2) && (obj.getY() >= y1) && (obj.getY() <= y2)) {
				list.add(obj.getValue());
			}
		}
		return list;
	}
	
	public List<Object> get(double[] x1, double[] x2, double[] y1, double[] y2) {
		List<Object> list = new ArrayList<Object>();
		for (KeyToSpatialObject obj : hashMap.values()) {
			for (int i = 0; i < x1.length; i++) {
				if ((obj.getX() >= x1[i]) && (obj.getX() <= x2[i]) && (obj.getY() >= y1[i]) && (obj.getY() <= y2[i])) {
					list.add(obj.getValue());
					break;
				}
			}
		}
		return list;
	}
	
	public List<KeyToSpatialMerge> get(double x1, double x2, double y1, double y2, double dx, double dy) {
		List<KeyToSpatialMerge> list = new ArrayList<KeyToSpatialMerge>();
		for (KeyToSpatialObject obj : hashMap.values()) {
			if ((obj.getX() >= x1) && (obj.getX() <= x2) && (obj.getY() >= y1) && (obj.getY() <= y2)) {
				boolean add = true;
				for (KeyToSpatialMerge merge : list) {
					if ((Math.abs(merge.getBase().getX() - obj.getX()) <= dx) && (Math.abs(merge.getBase().getY() - obj.getY()) <= dy)) {
						add = false;
						merge.getMerge().add(obj);
						break;
					}
				}
				
				if (add == true) {
					KeyToSpatialMerge merge = new KeyToSpatialMerge();
					merge.setBase(obj);
					list.add(merge);
				}
			}
		}
		return list;
	}
	
	public List<KeyToSpatialMerge> get(double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy) {
		List<KeyToSpatialMerge> list = new ArrayList<KeyToSpatialMerge>();
		for (KeyToSpatialObject obj : hashMap.values()) {
			for (int i = 0; i < x1.length; i++) {
				if ((obj.getX() >= x1[i]) && (obj.getX() <= x2[i]) && (obj.getY() >= y1[i]) && (obj.getY() <= y2[i])) {
					boolean add = true;
					for (KeyToSpatialMerge merge : list) {
						if (((Math.abs(merge.getBase().getX() - obj.getX()) <= dx) || (360 - Math.abs(merge.getBase().getX() - obj.getX()) <= dx)) && (Math.abs(merge.getBase().getY() - obj.getY()) <= dy)) {
							add = false;
							merge.getMerge().add(obj);
							break;
						}
					}
					
					if (add == true) {
						KeyToSpatialMerge merge = new KeyToSpatialMerge();
						merge.setBase(obj);
						list.add(merge);
					}
					break;
				}
			}
		}
		return list;
	}
	
	public List<Object> get(double x1, double x2, double y1, double y2, Set<?> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : matchSet) {
			KeyToSpatialObject obj = hashMap.get(object);
			if (obj != null) {
				if ((obj.getX() >= x1) && (obj.getX() <= x2) && (obj.getY() >= y1) && (obj.getY() <= y2)) {
					list.add(obj.getValue());
				}
			}
		}
		return list;
	}
	
	public List<Object> get(double[] x1, double[] x2, double[] y1, double[] y2, Set<?> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : matchSet) {
			KeyToSpatialObject obj = hashMap.get(object);
			if (obj != null) {
				for (int i = 0; i < x1.length; i++) {
					if ((obj.getX() >= x1[i]) && (obj.getX() <= x2[i]) && (obj.getY() >= y1[i]) && (obj.getY() <= y2[i])) {
						list.add(obj.getValue());
						break;
					}
				}
			}
		}
		return list;
	}
	
	public List<KeyToSpatialMerge> get(double x1, double x2, double y1, double y2, double dx, double dy, Set<?> matchSet) {
		List<KeyToSpatialMerge> list = new ArrayList<KeyToSpatialMerge>();
		for (Object object : matchSet) {
			KeyToSpatialObject obj = hashMap.get(object);
			if (obj != null) {
				if ((obj.getX() >= x1) && (obj.getX() <= x2) && (obj.getY() >= y1) && (obj.getY() <= y2)) {
					boolean add = true;
					for (KeyToSpatialMerge merge : list) {
						if (((Math.abs(merge.getBase().getX() - obj.getX()) <= dx) || (360 - Math.abs(merge.getBase().getX() - obj.getX()) <= dx)) && (Math.abs(merge.getBase().getY() - obj.getY()) <= dy)) {
							add = false;
							merge.getMerge().add(obj);
							break;
						}
					}
					
					if (add == true) {
						KeyToSpatialMerge merge = new KeyToSpatialMerge();
						merge.setBase(obj);
						list.add(merge);
					}
				}
			}
		}
		return list;
	}
	
	public List<KeyToSpatialMerge> get(double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy, Set<?> matchSet) {
		List<KeyToSpatialMerge> list = new ArrayList<KeyToSpatialMerge>();
		for (Object object : matchSet) {
			KeyToSpatialObject obj = hashMap.get(object);
			if (obj != null) {
				for (int i = 0; i < x1.length; i++) {
					if ((obj.getX() >= x1[i]) && (obj.getX() <= x2[i]) && (obj.getY() >= y1[i]) && (obj.getY() <= y2[i])) {
						boolean add = true;
						for (KeyToSpatialMerge merge : list) {
							if (((Math.abs(merge.getBase().getX() - obj.getX()) <= dx) || (360 - Math.abs(merge.getBase().getX() - obj.getX()) <= dx)) && (Math.abs(merge.getBase().getY() - obj.getY()) <= dy)) {
								add = false;
								merge.getMerge().add(obj);
								break;
							}
						}
						
						if (add == true) {
							KeyToSpatialMerge merge = new KeyToSpatialMerge();
							merge.setBase(obj);
							list.add(merge);
						}
						break;
					}
				}
			}
		}
		return list;
	}
	
	public Object get(Object key) {
		KeyToSpatialObject v = hashMap.get(key);
		if (v != null) {
			return v.getValue();
		} else {
			return null;
		}
	}
	
	public List<Object> get(Set<Object> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object object : matchSet) {
			KeyToSpatialObject obj = hashMap.get(object);
			if (obj != null) {
				list.add(obj.getValue());
			}
		}
		return list;
	}
	
	public boolean contains(Object key) {
		return hashMap.containsKey(key);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		boolean first = true;
		stringBuffer.append("{\"t\":\"s\",");
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
			} else if (objv instanceof Map) {
				stringBuffer.append(JSONUtil.parserToStr((Map) objv));
			} else if (objv instanceof List) {
				stringBuffer.append(JSONUtil.parserToStr((List) objv));
			} else {
				stringBuffer.append(objv);
			}
		}
		stringBuffer.append("}}");
		return stringBuffer.toString();
	}
	
	public Object toObj() throws JSONException {
		return JSONUtil.parserStrToMap(this.toString());
	}
}
