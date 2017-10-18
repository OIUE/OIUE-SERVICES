package org.oiue.service.consume.impl;

import java.util.HashMap;
import java.util.Map;

import org.oiue.service.online.Online;
import org.oiue.tools.string.StringUtil;


@SuppressWarnings({ "serial","unused"})
public class Consume<K, V> extends HashMap<K, V> implements Map<K, V> {
	private String token;
	private String getway;

	public Consume(Map<K, V> map,Online online) {
		this.token=online.getToken();
		this.getway = map.remove("tag")+"";
		this.putAll(map);
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	public boolean contains(Object key) {
		return this.containsKey(key);
	}

	@Override
	public V put(K key, V value) {
		if(value==null)
			return null;
		if(StringUtil.isEmptys(value+""))
			return null;
		return super.put(key, value);
	}
}
