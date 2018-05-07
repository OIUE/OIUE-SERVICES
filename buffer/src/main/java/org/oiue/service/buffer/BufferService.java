package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BufferService extends Serializable {
	
	public Map<String, Object> getHashMap();
	
	public Object put(String name, Type bufferType);
	
	public Object get(String name);
	
	public Object remove(String name);
	
	public void put(String name, String key, Object value);
	
	public void put(String name, String key, Object value, Type bufferType);
	
	public void put(String name, String key, double x, double y, Object value);
	
	public void put(String name, String key, String parentKey, Object value);
	
	public void swap(String nameA, String nameB);
	
	public Object get(String name, String key);
	
	// public Object get(String name, String key, String parentKey);
	
	public List<Object> get(String name, Set<Object> matchSet);
	
	public List<Object> get(String name, double x1, double x2, double y1, double y2, Set<Object> matchSet);
	
	public List<Object> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, Set<Object> matchSet);
	
	public List<KeyToSpatialMerge> get(String name, double x1, double x2, double y1, double y2, double dx, double dy, Set<Object> matchSet);
	
	public List<KeyToSpatialMerge> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy, Set<Object> matchSet);
	
	public List<Object> get(String name, double x1, double x2, double y1, double y2);
	
	public List<Object> get(String name, double[] x1, double[] x2, double[] y1, double[] y2);
	
	public List<KeyToSpatialMerge> get(String name, double x1, double x2, double y1, double y2, double dx, double dy);
	
	public List<KeyToSpatialMerge> get(String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy);
	
	public boolean contains(String name, String key);
	
	public boolean contains(String nameKeyToMany, String key, String value);
	
	public List<Object> getRelation(String nameKeyToMany, String key, String name);
	
	public List<Object> getRelation(String nameKeyToMany, String key, String name, double x1, double x2, double y1, double y2);
	
	public List<Object> getRelation(String nameKeyToMany, String key, String name, double[] x1, double[] x2, double[] y1, double[] y2);
	
	public List<KeyToSpatialMerge> getRelation(String nameKeyToMany, String key, String name, double x1, double x2, double y1, double y2, double dx, double dy);
	
	public List<KeyToSpatialMerge> getRelation(String nameKeyToMany, String key, String name, double[] x1, double[] x2, double[] y1, double[] y2, double dx, double dy);
	
	public void remove(String name, String key);
	
	public void remove(String name, Set<Object> matchSet);
	
	public void remove(String name, String key, Object value);
	
	public void remove(String name, String key, Set<Object> matchSet);
	
	public void removeRelation(String nameKeyToMany, String key, String name);
	
	public void removeRelation(String nameKeyToMany, String key, String removeNameKeyToMany, String removeKey);
	
}