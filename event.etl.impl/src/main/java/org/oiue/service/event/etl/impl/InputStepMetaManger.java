package org.oiue.service.event.etl.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.oiue.tools.map.MapUtil;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class InputStepMetaManger implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Map<String,InputStepMeta> stepMetaM = new HashMap<>();

	public static StepMeta ConvertToStepMeta(Map data,TransMeta transMeta) {
		String type = MapUtil.getString(data, "input_type","table");
		return stepMetaM.get(type).ConvertToStepMeta(data,transMeta);
	}

	public static void registerInputStepMeta(String type,InputStepMeta ism){
		stepMetaM.put(type, ism);
	}

}
