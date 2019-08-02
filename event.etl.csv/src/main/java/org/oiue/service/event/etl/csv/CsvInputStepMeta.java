package org.oiue.service.event.etl.csv;

import java.util.List;
import java.util.Map;

import org.oiue.service.event.etl.impl.EventETLServiceImpl;
import org.oiue.service.event.etl.impl.InputStepMeta;
import org.oiue.service.log.Logger;
import org.oiue.tools.map.MapUtil;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

@SuppressWarnings("deprecation")
public class CsvInputStepMeta implements InputStepMeta {
	private static final long serialVersionUID = 1L;
	protected static Logger logger;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StepMeta ConvertToStepMeta(Map data, TransMeta transMeta) {
		String instepid = "CsvInput";
		String instepname = MapUtil.getString(data, "table", "CvsInput" + System.currentTimeMillis());
		boolean hasSystemId = MapUtil.getBoolean(data, "hasSystemId",false);
		PluginRegistry registry = PluginRegistry.getInstance();
		PluginInterface sp = registry.findPluginWithId(StepPluginType.class, instepid);
		StepMetaInterface stepMetaInterface;
		try {
			stepMetaInterface = (StepMetaInterface) registry.loadClass(sp);
			CsvInputMeta inputMeta = (CsvInputMeta) stepMetaInterface;
			
			inputMeta.setFilename(EventETLServiceImpl.path + "/uploadfile/" + MapUtil.getString(data, "upload_file"));
			inputMeta.setDelimiter(MapUtil.getString(data, "delimiter", ","));
			inputMeta.setEncoding(MapUtil.getString(data, "charset", "UTF-8"));
			inputMeta.setEnclosure(MapUtil.getString(data, "enclosure", "\""));
			inputMeta.setBufferSize("1024");
			inputMeta.setHeaderPresent(true);
			inputMeta.setRunningInParallel(true);
			
			List<Map> fields;
			fields = (List) data.get("fields");
			logger.debug("fields:{}", fields);
			int nrNonEmptyFields = fields.size();
			inputMeta.allocate(nrNonEmptyFields-(hasSystemId?1:0));
			int i = 0;
			for (Map field : fields) {
				if (EventETLServiceImpl._system_colnum.equals(MapUtil.getString(field, "column_name"))) {} else {
					inputMeta.getInputFields()[i] = new TextFileInputField();
					inputMeta.getInputFields()[i].setName(MapUtil.getString(field, "name"));
					inputMeta.getInputFields()[i].setType(MapUtil.getInt(field, "type"));
					i++;
				}
			}
			
			return new StepMeta(instepid, instepname, stepMetaInterface);
		} catch (KettlePluginException e) {
			throw new RuntimeException(e);
		}
		
	}
}
