package org.oiue.service.event.etl.table;

import java.util.Map;

import org.oiue.service.event.etl.impl.InputStepMeta;
import org.oiue.service.event.etl.utils.StringEscapeHelper;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.map.MapUtil;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

public class TableInputStepMeta implements InputStepMeta {
	private static final long serialVersionUID = 1L;
	private LogService logService;
	private Logger logger;
	
	public TableInputStepMeta(LogService logService) {
		this.logService = logService;
		logger = this.logService.getLogger(getClass());
	}
	
	@Override
	public StepMeta ConvertToStepMeta(Map data, TransMeta transMeta) {
		String instepid = "TableInput";
		String instepname = MapUtil.getString(data, "table");
		PluginRegistry registry = PluginRegistry.getInstance();
		PluginInterface sp = registry.findPluginWithId(StepPluginType.class, instepid);
		StepMetaInterface stepMetaInterface;
		try {
			stepMetaInterface = (StepMetaInterface) registry.loadClass(sp);
			TableInputMeta tableInputMeta = (TableInputMeta) stepMetaInterface;
			
			tableInputMeta.setDatabaseMeta(DatabaseMeta.findDatabase(transMeta.getDatabases(), MapUtil.getString(data, "etl_database_name")));
			tableInputMeta.setSQL(StringEscapeHelper.decode(MapUtil.getString(data, "sql")));
			tableInputMeta.setRowLimit(MapUtil.getString(data, "limit"));
			
			tableInputMeta.setExecuteEachInputRow("Y".equalsIgnoreCase("N"));
			tableInputMeta.setVariableReplacementActive("Y".equalsIgnoreCase("N"));
			tableInputMeta.setLazyConversionActive("Y".equalsIgnoreCase("N"));
			
			String lookupFromStepname = MapUtil.getString(data, "lookup");
			StreamInterface infoStream = tableInputMeta.getStepIOMeta().getInfoStreams().get(0);
			infoStream.setSubject(lookupFromStepname);
			
			return new StepMeta(instepid, instepname, stepMetaInterface);
		} catch (KettlePluginException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
}
