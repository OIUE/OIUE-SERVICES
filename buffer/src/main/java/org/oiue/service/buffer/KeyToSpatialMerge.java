package org.oiue.service.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.oiue.tools.json.JSONUtil;

@SuppressWarnings("serial")
public class KeyToSpatialMerge implements Serializable {
	private static int MERGE_MAX_SIZE = 9;
	private KeyToSpatialObject base;
	private List<KeyToSpatialObject> merge = new ArrayList<KeyToSpatialObject>();
	
	public KeyToSpatialObject getBase() {
		return base;
	}
	
	public void setBase(KeyToSpatialObject base) {
		this.base = base;
	}
	
	public List<KeyToSpatialObject> getMerge() {
		return merge;
	}
	
	public void setMerge(List<KeyToSpatialObject> merge) {
		this.merge = merge;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		Object baseValue = base.getValue();
		stringBuffer.append("{\"b\":");
		if (baseValue instanceof String) {
			stringBuffer.append(JSONUtil.getJSONString((String) baseValue));
		} else if (baseValue instanceof Map) {
			stringBuffer.append(JSONUtil.parserToStr((Map) baseValue));
		} else if (baseValue instanceof List) {
			stringBuffer.append(JSONUtil.parserToStr((List) baseValue));
		} else {
			stringBuffer.append(baseValue);
		}
		if (merge.size() > 0) {
			stringBuffer.append(",\"c\":" + (merge.size() + 1));
			stringBuffer.append(",\"m\":[");
			int mergeSize = 0;
			for (KeyToSpatialObject obj : merge) {
				if (mergeSize > 0) {
					stringBuffer.append(",");
				}
				Object objKey = obj.getKey();
				if (objKey instanceof String) {
					stringBuffer.append(JSONUtil.getJSONString((String) objKey));
				} else {
					stringBuffer.append(objKey);
				}
				mergeSize++;
				if (mergeSize >= MERGE_MAX_SIZE) {
					break;
				}
			}
			stringBuffer.append("]");
		}
		stringBuffer.append("}");
		return stringBuffer.toString();
	}
	
	public Object toObj() throws JSONException {
		return JSONUtil.parserStrToMap(this.toString());
	}
}
