package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.oiue.tools.json.JSONUtil;


@SuppressWarnings("serial")
public class KeyToSpatialObject implements Serializable {
	private Object key;
	private double x;
	private double y;
	private Object value;

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String toString() {
		if (value == null) {
			return "";
		} else {
			if (value instanceof String) {
				return JSONUtil.getJSONString((String) value);
			} else if (value instanceof Map){
				return JSONUtil.parserToStr((Map)value);
			} else if (value instanceof List){
				return JSONUtil.parserToStr((List)value);
			} else {
				return value.toString();
			}
		}
	}
	

	public Object toObj() throws JSONException{
		return value;
	}
}
