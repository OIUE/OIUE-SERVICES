package org.oiue.service.event.etl.impl;

import java.io.Serializable;
import java.util.Map;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public interface InputStepMeta extends Serializable {
	StepMeta ConvertToStepMeta(Map data,TransMeta transMeta);
}
