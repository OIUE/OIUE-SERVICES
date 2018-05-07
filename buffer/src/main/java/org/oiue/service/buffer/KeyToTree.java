package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("serial")
public class KeyToTree implements Serializable {
	private Map<Object, KeyToTreeNode> hashMap = new ConcurrentHashMap<Object, KeyToTreeNode>();
	
	public Map<Object, KeyToTreeNode> getHashMap() {
		return hashMap;
	}
	
	public void put(Object key, Object parent, Object value) {
		List<Object> list = getKey(key);
		if (list != null) {
			if (list.contains(parent)) {
				System.out.println("error " + value);
				return;
			}
		}
		KeyToTreeNode nodeParent = hashMap.get(parent);
		if (nodeParent == null) {
			nodeParent = new KeyToTreeNode();
			nodeParent.setKey(key);
			nodeParent.setParent("");
			nodeParent.setValue(value);
			hashMap.put(parent, nodeParent);
		}
		
		KeyToTreeNode node = hashMap.get(key);
		if (node != null) {
			remove(key);
		} else {
			node = new KeyToTreeNode();
			hashMap.put(key, node);
		}
		node.setKey(key);
		node.setParent(parent);
		node.setValue(value);
		
		for (KeyToTreeNode e : hashMap.values()) {
			if (e.getKey().equals(parent)) {
				e.children.add(node);
				return;
			} else if (e.getParent().equals(key)) {
				node.children.add(e);
				return;
			}
		}
	}
	
	private void appendChildren(KeyToTreeNode node, List<Object> list) {
		for (KeyToTreeNode e : node.children) {
			list.add(e.getValue());
		}
		for (KeyToTreeNode e : node.children) {
			appendChildren(e, list);
		}
	}
	
	public List<Object> get(Object key) {
		List<Object> list = new ArrayList<Object>();
		KeyToTreeNode node = hashMap.get(key);
		if (node != null) {
			list.add(node);
			appendChildren(node, list);
		}
		return list;
	}
	
	private void appendChildrenNode(KeyToTreeNode node, List<KeyToTreeNode> list) {
		for (KeyToTreeNode e : node.children) {
			list.add(e);
		}
		for (KeyToTreeNode e : node.children) {
			appendChildrenNode(e, list);
		}
	}
	
	private List<KeyToTreeNode> getNode(Object key) {
		List<KeyToTreeNode> list = new ArrayList<KeyToTreeNode>();
		KeyToTreeNode node = hashMap.get(key);
		if (node != null) {
			list.add(node);
			appendChildrenNode(node, list);
		}
		return list;
	}
	
	private void appendChildrenKey(KeyToTreeNode node, List<Object> list) {
		for (KeyToTreeNode e : node.children) {
			list.add(e.getKey());
		}
		for (KeyToTreeNode e : node.children) {
			appendChildrenKey(e, list);
		}
	}
	
	private List<Object> getKey(Object key) {
		List<Object> list = new ArrayList<Object>();
		KeyToTreeNode node = hashMap.get(key);
		if (node != null) {
			list.add(node.getKey());
			appendChildrenKey(node, list);
		}
		return list;
	}
	
	public List<Object> get(Set<Object> matchSet) {
		List<Object> list = new ArrayList<Object>();
		for (Object key : matchSet) {
			KeyToTreeNode node = hashMap.get(key);
			if (node != null) {
				list.add(node);
				appendChildren(node, list);
			}
		}
		return list;
	}
	
	public void remove(Object key) {
		List<KeyToTreeNode> list = getNode(key);
		for (KeyToTreeNode e : list) {
			hashMap.remove(e.getKey());
		}
		hashMap.remove(key);
	}
	
	public void remove(Set<Object> matchSet) {
		for (Object key : matchSet) {
			remove(key);
		}
	}
	
	public boolean contains(Object key) {
		return hashMap.containsKey(key);
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		boolean first = true;
		stringBuffer.append("{\"t\":\"t\",");
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
			
			KeyToTreeNode node = hashMap.get(obj);
			stringBuffer.append(node.getValue());
		}
		stringBuffer.append("}}");
		return stringBuffer.toString();
	}
	
	public static void main(String[] args) {
		KeyToTree keyToTree = new KeyToTree();
		
		keyToTree.put(2, 3, "{k:2,p:3,new}");
		keyToTree.put(3, 2, "{k:3,p:2}");
		
		keyToTree.put(0, -1, "{k:0,p:-1}");
		keyToTree.put(1, 0, "{k:1,p:0}");
		keyToTree.put(2, 1, "{k:2,p:1}");
		System.out.println("all = " + keyToTree);
		System.out.println("1 = " + JSONUtil.getJSONString(keyToTree.get(1)));
		
		// keyToTree.put(2, 2, "{k:2,p:2}");
		keyToTree.put(2, 3, "{k:2,p:3,new}");
		// keyToTree.put(1, 2, "{k:1,p:2}");
		
		System.out.println("all = " + keyToTree);
		System.out.println("2 = " + JSONUtil.getJSONString(keyToTree.get(2)));
		keyToTree.put(3, 2, "{k:3,p:2}");
		System.out.println("all = " + keyToTree);
		System.out.println("1 = " + JSONUtil.getJSONString(keyToTree.get(1)));
		
		keyToTree.put(4, 3, "{k:4,p:3}");
		keyToTree.put(3, 1, "{k:3,p:1}");
		System.out.println("all = " + keyToTree);
		System.out.println("1 = " + JSONUtil.getJSONString(keyToTree.get(1)));
	}
}
