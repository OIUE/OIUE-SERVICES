package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("serial")
public class KeyToTreeNode implements Serializable {
	private Object key;
	private Object parent;
	private Object value;

	public Set<KeyToTreeNode> children = new CopyOnWriteArraySet<KeyToTreeNode>();

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KeyToTreeNode) {
			return key.equals(((KeyToTreeNode) obj).key);
		}
		return key.equals(obj);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		if (value instanceof String) {
			return JSONUtil.getJSONString((String) value);
		} else {
			return value.toString();
		}
	}
}
