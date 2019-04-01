package org.oiue.service.event.etl.shapfile;

import java.util.Map;

import org.oiue.service.event.etl.impl.InputStepMeta;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.map.MapUtil;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.shapefilereader.ShapeFileReaderMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class ShapInputStepMeta implements InputStepMeta {
	private static final long serialVersionUID = 1L;
	private LogService logService;
	private Logger logger;
	
	public ShapInputStepMeta(LogService logService) {
		this.logService = logService;
		logger = this.logService.getLogger(getClass());
	}
	
	@Override
	public StepMeta ConvertToStepMeta(Map data, TransMeta transMeta) {
		String instepid = "ShapeFileReader";
		String shapeFilename = MapUtil.getString(data, "shapeFilename");
		String dbfFilename = MapUtil.getString(data, "dbfFilename");
		PluginRegistry registry = PluginRegistry.getInstance();
		PluginInterface sp = registry.findPluginWithId(StepPluginType.class, instepid);
		StepMetaInterface stepMetaInterface;
		try {
			stepMetaInterface = (StepMetaInterface) registry.loadClass(sp);
			ShapeFileReaderMeta shapeInputMeta = (ShapeFileReaderMeta) stepMetaInterface;
			
			shapeInputMeta.setShapeFilename(shapeFilename);
			shapeInputMeta.setDbfFilename(dbfFilename);
			
			String lookupFromStepname = MapUtil.getString(data, "lookup");
			StreamInterface infoStream = shapeInputMeta.getStepIOMeta().getInfoStreams().get(0);
			infoStream.setSubject(lookupFromStepname);
			
			return new StepMeta(instepid, shapeFilename, stepMetaInterface);
		} catch (KettlePluginException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
