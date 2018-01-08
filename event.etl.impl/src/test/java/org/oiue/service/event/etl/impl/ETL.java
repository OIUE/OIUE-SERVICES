package org.oiue.service.event.etl.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oiue.service.event.etl.utils.DatabaseCodec;
import org.oiue.service.event.etl.utils.JSONObject;
import org.oiue.service.event.etl.utils.StringEscapeHelper;
import org.oiue.service.event.etl.utils.TransExecutionConfigurationCodec;
import org.oiue.service.event.etl.utils.TransExecutor;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

@SuppressWarnings("deprecation")
public class ETL {
	private static String transName="t1";
	@Test
	public void runTrans(){
		try {
			KettleEnvironment.init();
			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			//			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","etl","3306","root","mysql");
			KettleDatabaseRepositoryMeta repInfo = new KettleDatabaseRepositoryMeta();
			repInfo.setConnection(dataMeta);
			KettleDatabaseRepository rep = new KettleDatabaseRepository();
			rep.init(repInfo);
			rep.connect("admin", "admin");

			RepositoryDirectoryInterface dir = new RepositoryDirectory();
			dir.setObjectId(rep.getRootDirectoryID());

			TransMeta tranMeta = rep.loadTransformation(rep.getTransformationID(transName, dir), null);
			Trans trans = new Trans(tranMeta);
			trans.execute(null);
			trans.waitUntilFinished();
		} catch (KettleException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testConnRep(){
		try {
			KettleEnvironment.init();
			//			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			//			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","etl","3306","root","mysql");
			//			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","Encrypted 2be98afc86aa7f2e4cb79ce7dc781bed6");
			//			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","POSTGRESQL","Native","127.0.0.1","ltmap","5432","postgres","Encrypted 456e637279707465642034353665363337323739373037343635363432303332363236353339333836313636363333383336363136313337363633323635333664646661393866313063633163306434d8ab9eac0bc3c0d2af41f723d8c5ab83");
			DatabaseMeta dataMeta = new DatabaseMeta("KettleDBRep","POSTGRESQL","Native","127.0.0.1","ltmap","5432","postgres","123456");
			KettleDatabaseRepositoryMeta repInfo = new KettleDatabaseRepositoryMeta();
			repInfo.setConnection(dataMeta);
			KettleDatabaseRepository rep = new KettleDatabaseRepository();
			rep.init(repInfo);
			rep.connect("admin", "admin");

			System.out.println( Encr.decryptPasswordOptionallyEncrypted("Encrypted 2be98afc86aa7f2e4cb79ff228dc6fa8c"));
		} catch (KettleException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testRp(){
		try {
			KettleEnvironment.init();
			RepositoriesMeta input = new RepositoriesMeta();
			if (input.readData()) {
				RepositoryMeta repositoryMeta = input.searchRepository( "r1" );
				if(repositoryMeta != null) {
					Repository repository = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
					repository.init( repositoryMeta );
					repository.connect( "admin", "admin" );

					Props.getInstance().setLastRepository( repositoryMeta.getName() );
					Props.getInstance().setLastRepositoryLogin( "admin" );
					Props.getInstance().setProperty( "ShowRepositoriesAtStartup",true ? "Y" : "N");

					Props.getInstance().saveProps();

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetTableColumn(){

		try {
			KettleEnvironment.init();
		} catch (KettleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databaseName\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\"}";
		jsonStr ="{\"name\":\"1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\"}";
		Map data=JSONUtil.parserStrToMap(jsonStr);
		//		KettleDatabaseRepositoryDatabaseDelegate d=new KettleDatabaseRepositoryDatabaseDelegate(null);
		String databaseName = MapUtil.getString(data, "databasename");
		String schema = null;
		String table = "camera";
		String transId = MapUtil.getString(data, "trans_id");

		LoggingObjectInterface loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );
		//		TransMeta transMeta = (TransMeta) getTrans(transId);
		//		DatabaseMeta inf = transMeta.findDatabase(databaseName);

		try {
			//			DatabaseMeta databaseMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			DatabaseMeta databaseMeta = DatabaseCodec.decode(data);

			Database db = new Database( loggingObject, databaseMeta );
			db.connect();

			List jsonArray = new ArrayList<>();
			String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema,table);
			RowMetaInterface fields = db.getTableFields(schemaTable);
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					ValueMetaInterface field = fields.getValueMeta(i);
					Map jsonObject = new HashMap();
					jsonObject.put("name", databaseMeta.quoteField(field.getName()));
					jsonObject.put("names", field.getName());
					jsonObject.put("type", databaseMeta.quoteField(field.getTypeDesc()));
					jsonObject.put("comments", databaseMeta.quoteField(field.getComments()));
					jsonObject.put("length", field.getLength());
					jsonArray.add(jsonObject);
				}
			}
			System.out.println(jsonArray);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	@Test
	public void testGetCSVColumn(){

		try {
			KettleEnvironment.init();
		} catch (KettleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databaseName\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\"}";
		jsonStr ="{\"name\":\"1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\"}";
		Map data=JSONUtil.parserStrToMap(jsonStr);
		//		KettleDatabaseRepositoryDatabaseDelegate d=new KettleDatabaseRepositoryDatabaseDelegate(null);
		String databaseName = MapUtil.getString(data, "databasename");
		String schema = null;
		String table = "camera";
		String transId = MapUtil.getString(data, "trans_id");

		CsvInputMeta csvInputMeta = new CsvInputMeta();

		LoggingObjectInterface loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );
		//		TransMeta transMeta = (TransMeta) getTrans(transId);
		//		DatabaseMeta inf = transMeta.findDatabase(databaseName);

		try {
			//			DatabaseMeta databaseMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			DatabaseMeta databaseMeta = DatabaseCodec.decode(data);

			Database db = new Database( loggingObject, databaseMeta );
			db.connect();

			List jsonArray = new ArrayList<>();
			String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema,table);
			RowMetaInterface fields = db.getTableFields(schemaTable);
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					ValueMetaInterface field = fields.getValueMeta(i);
					Map jsonObject = new HashMap();
					jsonObject.put("name", databaseMeta.quoteField(field.getName()));
					jsonObject.put("names", field.getName());
					jsonObject.put("type", databaseMeta.quoteField(field.getTypeDesc()));
					jsonObject.put("comments", databaseMeta.quoteField(field.getComments()));
					jsonObject.put("length", field.getLength());
					jsonArray.add(jsonObject);
				}
			}
			System.out.println(jsonArray);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings("unused")
	@Test
	public void testGetTableColumnbysql(){

		try {
			KettleEnvironment.init();
		} catch (KettleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databasename\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\"}";
		Map data=JSONUtil.parserStrToMap(jsonStr);
		String databaseName = MapUtil.getString(data, "databaseName");
		String schema = null;
		String table = "eova_dict";
		String transId = MapUtil.getString(data, "trans_id");

		LoggingObjectInterface loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );
		//		TransMeta transMeta = (TransMeta) getTrans(transId);
		//		DatabaseMeta inf = transMeta.findDatabase(databaseName);

		try {
			//			DatabaseMeta databaseMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			DatabaseMeta databaseMeta = DatabaseCodec.decode(data);

			Database db = new Database( loggingObject, databaseMeta );
			db.connect();

			List jsonArray = new ArrayList<>();
			RowMetaInterface fields = db.getQueryFieldsFromPreparedStatement("select ");
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					ValueMetaInterface field = fields.getValueMeta(i);
					Map jsonObject = new HashMap();
					jsonObject.put("name", databaseMeta.quoteField(field.getName()));
					jsonObject.put("type", databaseMeta.quoteField(field.getTypeDesc()));
					jsonObject.put("comments", databaseMeta.quoteField(field.getComments()));
					jsonObject.put("length", field.getLength());
					jsonArray.add(jsonObject);
				}
			}
			System.out.println(jsonArray);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testCreateTable(){
		try {
			KettleEnvironment.init();
		} catch (KettleException e1) {
			e1.printStackTrace();
		}
		String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databasename\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\"}";
		String jsonStrs ="{\"name\":\"10.211.55.7\",\"type\":\"POSTGRESQL\",\"hostname\":\"10.211.55.7\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\"}";
		Map data=JSONUtil.parserStrToMap(jsonStr);
		Map datas=JSONUtil.parserStrToMap(jsonStrs);

		LoggingObjectInterface loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );
		//		TransMeta transMeta = (TransMeta) getTrans(transId);
		//		DatabaseMeta inf = transMeta.findDatabase(databaseName);

		try {
			//			DatabaseMeta databaseMeta = new DatabaseMeta("KettleDBRep","MYSQL","Native","10.211.55.7","kettle-master","3306","root","mysql");
			DatabaseMeta databaseMeta = DatabaseCodec.decode(data);
			DatabaseMeta databaseMetas = DatabaseCodec.decode(datas);

			Database db = new Database( loggingObject, databaseMeta );
			Database dbs = new Database( loggingObject, databaseMetas );
			db.connect();
			dbs.connect();

			RowMetaInterface fields = db.getQueryFieldsFromPreparedStatement("select * from test ");
			RowMetaInterface fieldss =db.getTableFields("test");
			if (fieldss != null) {
				for (int i = 0; i < fieldss.size(); i++) {
					ValueMetaInterface field = fieldss.getValueMeta(i);
					field.setName("n_"+field.getName());
					System.out.println(field.getTypeDesc());
					//					System.out.println(XML.toJSONObject(field.getMetaXML()));
				}
			}
			RowMetaInterface f=dbs.getQueryFieldsFromPreparedStatement("select");
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					ValueMetaInterface field = fields.getValueMeta(i);
					field.setName("n_"+field.getName());
					field.setComments("中文描述"+field.getName());
					f.addValueMeta(field);
				}
			}
			System.out.println(db.getDDLCreationTable("ttt", fields));
			System.out.println(dbs.getDDLCreationTable("ttt", fields));
			System.out.println(dbs.getDDLCreationTable("ttt", f));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testEtl(){
		EventETLServiceImpl es = new EventETLServiceImpl();
		String jsonStr ="{\"operation_type\":\"create_entity_table\",\"id_database\":1,\"table_desc\":\"测试\",\"table\":\"test_f\",\"fields\":[{\"name\":\"id\",\"field_desc\":\"ID\",\"ispk\":true},{\"name\":\"test_p_id\",\"field_desc\":\"父ID\",\"ispk\":false},{\"name\":\"test_fname\",\"field_desc\":\"名称\",\"ispk\":false}]}";
		Map data=JSONUtil.parserStrToMap(jsonStr);
		try {
			es.setEntityColumns(data, null, null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	static KettleDatabaseRepository repository;
	static DatabaseMeta localDatabaseMeta ;
	static LoggingObjectInterface loggingObject ;

	@SuppressWarnings("serial")
	@Test
	public void testEES(){
		try {
			KettleEnvironment.init();
			String jsonStr ="{\"name\":\"1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\",\"table\":\"bar_a1_03\",\"entity_id\":\"t_e0aaf3e13561477fba9fa1a7ab3dbd13\",\"id_database\":\"1\",\"sql\":\"select * from bar_a1_03\","
					+"\"fields\":[{\"name\":\"id\",\"entity_id\":\"f_eac3a58b465b40099bf994d1cf441c89\"},{\"name\":\"geom\",\"entity_id\":\"f_e58f237e43fd44e9b4b7f3e2fc931b8a\"},{\"name\":\"name\",\"entity_id\":\"f_269997a9f0bc419e8d38dee2b047da14\"},{\"name\":\"data\",\"entity_id\":\"f_f2b79396292c4fc49fb070609030760e\"}]}";
			//			String jsonStr ="{\"name\":\"127.0.0.1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\",\"table\":\"test_f\",\"entity_id\":\"t_49f92fda2492423c84c81d4bab9a2733\",\"id_database\":\"1\",\"sql\":\"select * from test_f\",\"fields\":[{\"name\":\"id\",\"entity_id\":\"f_61b22c416bc2491b8c81ec86002de615\"},{\"name\":\"test_fname\",\"entity_id\":\"f_060bc2d1d8be4f458e10884a4c94e805\"},{\"name\":\"test_p_id\",\"entity_id\":\"f_6a820b0829f043dc9b671af80907958a\"}]}";
			//			String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databasename\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\",\"fields\":[{\"name\":\"f1\",\"entity_id\":\"123456\"},{\"name\":\"f2\",\"entity_id\":\"654321\"}]}";
			//			String jsonStrs ="{\"name\":\"10.211.55.7\",\"type\":\"POSTGRESQL\",\"hostname\":\"10.211.55.7\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\"}";
			Map data=JSONUtil.parserStrToMap(jsonStr);
			//			Map datas=JSONUtil.parserStrToMap(jsonStrs);
			data.put("sql", "select id,\"name\",geom,data from bar_a1_03");
			loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );

			Map localdata = new HashMap<>();
			localdata.put("id_database", 1);
			localDatabaseMeta = DatabaseCodec.decode(data);//query local connection config

			KettleDatabaseRepositoryMeta repInfo = new KettleDatabaseRepositoryMeta();
			repInfo.setConnection(localDatabaseMeta);
			repository = new KettleDatabaseRepository();
			repository.init( repInfo );
			repository.connect( "admin", "admin" );

			RepositoriesMeta repositories = new RepositoriesMeta();
			if(repositories.readData()) {
				DatabaseMeta previousMeta = repositories.searchDatabase(localDatabaseMeta.getName());
				if(previousMeta != null) {
					repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
				}
				repositories.addDatabase( localDatabaseMeta );
				repositories.writeData();
			}
			DatabaseMeta previousMeta = repositories.searchDatabase("1");

			RepositoryDirectoryInterface directory = repository.findDirectory(MapUtil.getString(data, "user_id"));
			if(directory == null)
				directory = repository.getUserHomeDirectory();

			if(repository.exists(transName, directory, RepositoryObjectType.TRANSFORMATION)) {
				throw new RuntimeException("该转换已经存在，请重新输入！");
			}
			TransMeta transMeta = new TransMeta();
			transMeta.setRepository(repository);
			transMeta.setName(transName);
			transMeta.setRepositoryDirectory(directory);
			transMeta.setTransstatus(-1);

			transMeta.importFromMetaStore();

			DatabaseMeta exist = transMeta.findDatabase(previousMeta.getName());
			if (exist == null) {
				transMeta.addDatabase(previousMeta);
			} else {
				if (!exist.isShared()) {
					int idx = transMeta.indexOfDatabase(exist);
					transMeta.removeDatabase(idx);
					transMeta.addDatabase(idx, previousMeta);
				}
			}

			List<Map> fields;
			fields = (List) data.get("fields");

			//in
			String instepid="TableInput";
			String instepname=MapUtil.getString(data, "table");
			PluginRegistry registry = PluginRegistry.getInstance();
			PluginInterface sp = registry.findPluginWithId( StepPluginType.class, instepid );
			StepMetaInterface stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );

			TableInputMeta tableInputMeta = (TableInputMeta) stepMetaInterface;

			tableInputMeta.setDatabaseMeta(DatabaseMeta.findDatabase( transMeta.getDatabases(),MapUtil.getString(data, "id_database") ));
			tableInputMeta.setSQL(StringEscapeHelper.decode(MapUtil.getString(data, "sql" )));
			tableInputMeta.setRowLimit(MapUtil.getString(data, "limit" ));

			tableInputMeta.setExecuteEachInputRow("Y".equalsIgnoreCase("N"));
			tableInputMeta.setVariableReplacementActive("Y".equalsIgnoreCase("N"));
			tableInputMeta.setLazyConversionActive("Y".equalsIgnoreCase("N"));
			//			tableInputMeta.setExecuteEachInputRow("Y".equalsIgnoreCase(MapUtil.getString(data,"execute_each_row" )));
			//			tableInputMeta.setVariableReplacementActive("Y".equalsIgnoreCase(MapUtil.getString(data,"variables_active" )));
			//			tableInputMeta.setLazyConversionActive("Y".equalsIgnoreCase(MapUtil.getString(data,"lazy_conversion_active" )));

			String lookupFromStepname = MapUtil.getString(data,"lookup");
			StreamInterface infoStream = tableInputMeta.getStepIOMeta().getInfoStreams().get(0);
			infoStream.setSubject(lookupFromStepname);

			StepMeta inStepMeta = new StepMeta(instepid, instepname, stepMetaInterface);
			inStepMeta.setParentTransMeta( transMeta );
			if (inStepMeta.isMissing()) {
				transMeta.addMissingTrans((MissingTrans) inStepMeta.getStepMetaInterface());
			}
			StepMeta check = transMeta.findStep(inStepMeta.getName());
			if (check != null) {
				if (!check.isShared()) {
					// Don't overwrite shared objects
					transMeta.addOrReplaceStep(inStepMeta);
				} else {
					check.setDraw(inStepMeta.isDrawn()); // Just keep the  drawn flag  and location
					check.setLocation(inStepMeta.getLocation());
				}
			} else {
				transMeta.addStep(inStepMeta); // simply add it.
			}

			//out
			String outstepid="TableOutput";
			String outstepname=MapUtil.getString(data, "entity_id");
			sp = registry.findPluginWithId( StepPluginType.class, outstepid );
			stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
			TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMetaInterface;

			tableOutputMeta.setDatabaseMeta(DatabaseMeta.findDatabase(transMeta.getDatabases(),MapUtil.getString(data, "id_database")));
			tableOutputMeta.setSchemaName("public");
			tableOutputMeta.setTableName(outstepname);
			//			tableOutputMeta.setCommitSize(cell.getAttribute("commit"));
			//			tableOutputMeta.setTruncateTable("Y".equalsIgnoreCase(cell.getAttribute("truncate")));
			//			tableOutputMeta.setIgnoreErrors("Y".equalsIgnoreCase(cell.getAttribute("ignore_errors")));
			//			tableOutputMeta.setUseBatchUpdate("Y".equalsIgnoreCase(cell.getAttribute("use_batch")));
			//
			//			tableOutputMeta.setSpecifyFields("Y".equalsIgnoreCase(cell.getAttribute("specify_fields")));
			//			tableOutputMeta.setPartitioningEnabled("Y".equalsIgnoreCase(cell.getAttribute("partitioning_enabled")));
			//			tableOutputMeta.setPartitioningField(cell.getAttribute("partitioning_field"));
			//			tableOutputMeta.setPartitioningDaily("Y".equalsIgnoreCase(cell.getAttribute("partitioning_daily")));
			//			tableOutputMeta.setPartitioningMonthly("Y".equalsIgnoreCase(cell.getAttribute("partitioning_monthly")));
			//
			//			tableOutputMeta.setTableNameInField("Y".equalsIgnoreCase(cell.getAttribute("tablename_in_field")));
			//			tableOutputMeta.setTableNameField(cell.getAttribute("tablename_field"));
			//			tableOutputMeta.setTableNameInTable("Y".equalsIgnoreCase(cell.getAttribute("tablename_in_table")));
			//			tableOutputMeta.setReturningGeneratedKeys("Y".equalsIgnoreCase(cell.getAttribute("return_keys")));
			//			tableOutputMeta.setGeneratedKeyField(cell.getAttribute("return_field"));
			tableOutputMeta.setCommitSize("1000");
			tableOutputMeta.setTruncateTable(false);
			tableOutputMeta.setIgnoreErrors(false);
			tableOutputMeta.setUseBatchUpdate(true);

			tableOutputMeta.setSpecifyFields(true);
			tableOutputMeta.setPartitioningEnabled(false);
			tableOutputMeta.setPartitioningField("");
			tableOutputMeta.setPartitioningDaily(false);
			tableOutputMeta.setPartitioningMonthly(true);

			tableOutputMeta.setTableNameInField(false);
			tableOutputMeta.setTableNameField("");
			tableOutputMeta.setTableNameInTable(true);
			tableOutputMeta.setReturningGeneratedKeys(false);
			tableOutputMeta.setGeneratedKeyField("");

			List<String> fieldDatabase = new ArrayList<>();
			List<String> fieldStream = new ArrayList<>();
			for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
				fieldDatabase.add(MapUtil.getString(field,"entity_id"));
				//				fieldStream.add(localDatabaseMeta.quoteField(MapUtil.getString(field,"name")));
				fieldStream.add(MapUtil.getString(field,"name"));
			}
			tableOutputMeta.setFieldDatabase(fieldDatabase.toArray(new String[fields.size()]));
			tableOutputMeta.setFieldStream(fieldStream.toArray(new String[fields.size()]));

			StepMeta outStepMeta = new StepMeta(outstepid, outstepname, stepMetaInterface);
			outStepMeta.setParentTransMeta( transMeta );
			if (outStepMeta.isMissing()) {
				transMeta.addMissingTrans((MissingTrans) outStepMeta.getStepMetaInterface());
			}

			check = transMeta.findStep(outStepMeta.getName());
			if (check != null) {
				if (!check.isShared()) {
					// Don't overwrite shared objects
					transMeta.addOrReplaceStep(outStepMeta);
				} else {
					check.setDraw(outStepMeta.isDrawn()); // Just keep the  drawn flag  and location
					check.setLocation(outStepMeta.getLocation());
				}
			} else {
				transMeta.addStep(outStepMeta); // simply add it.
			}
			//			for (int i = 0; i < transMeta.nrSteps(); i++) {
			//				StepMeta stepMeta = transMeta.getStep(i);
			//				StepMetaInterface sii = stepMeta.getStepMetaInterface();
			//				if (sii != null) {
			//					sii.searchInfoAndTargetSteps(transMeta.getSteps());
			//				}
			//			}

			TransHopMeta hopinf = new TransHopMeta(null, null, true);
			String[] stepNames = transMeta.getStepNames();
			for (int j = 0; j < stepNames.length; j++) {
				if (stepNames[j].equalsIgnoreCase(instepname))
					hopinf.setFromStep(transMeta.getStep(j));
				if (stepNames[j].equalsIgnoreCase(outstepname))
					hopinf.setToStep(transMeta.getStep(j));
			}
			transMeta.addTransHop(hopinf);
			String executionConfiguration="{\"exec_local\":\"Y\",\"exec_remote\":\"N\",\"pass_export\":\"N\",\"exec_cluster\":\"N\",\"cluster_post\":\"Y\",\"cluster_prepare\":\"Y\",\"cluster_start\":\"Y\",\"cluster_show_trans\":\"N\",\"parameters\":[],\"variables\":[{\"name\":\"Internal.Entry.Current.Directory\",\"value\":\"/\"},{\"name\":\"Internal.Job.Filename.Directory\",\"value\":\"Parent Job File Directory\"},{\"name\":\"Internal.Job.Filename.Name\",\"value\":\"Parent Job Filename\"},{\"name\":\"Internal.Job.Name\",\"value\":\"Parent Job Name\"},{\"name\":\"Internal.Job.Repository.Directory\",\"value\":\"Parent Job Repository Directory\"}],\"arguments\":[],\"safe_mode\":\"N\",\"log_level\":\"Basic\",\"clear_log\":\"Y\",\"gather_metrics\":\"Y\",\"log_file\":\"N\",\"log_file_append\":\"N\",\"show_subcomponents\":\"Y\",\"create_parent_folder\":\"N\",\"remote_server\":\"\",\"replay_date\":\"\"}";
			JSONObject jsonObject = JSONObject.fromObject(executionConfiguration);
			TransExecutionConfiguration transExecutionConfiguration = TransExecutionConfigurationCodec.decode(jsonObject, transMeta);

			TransExecutor transExecutor = TransExecutor.initExecutor(transExecutionConfiguration, transMeta, repository, null, new LogService() {

				@Override
				public Logger getLogger(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Logger getLogger(Class clazz) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			Thread tr = new Thread(transExecutor, "TransExecutor_" + transExecutor.getExecutionId());
			tr.start();
			//			executions.put(transExecutor.getExecutionId(), transExecutor);

			Thread.sleep(1000);

			while(!transExecutor.isFinished()){
				jsonObject.put("1stepMeasure", transExecutor.getStepMeasure());
				jsonObject.put("log", transExecutor.getExecutionLog());
				jsonObject.put("stepStatus", transExecutor.getStepStatus());
				System.out.println(jsonObject);
			}

			jsonObject.put("1stepMeasure", transExecutor.getStepMeasure());
			jsonObject.put("log", transExecutor.getExecutionLog());
			jsonObject.put("stepStatus", transExecutor.getStepStatus());
			System.out.println(jsonObject);

			//			repository.save( transMeta, "add: " + new Date(), null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings({  "serial" })
	@Test
	public void testCSVES(){
		try {
			KettleEnvironment.init();
			String jsonStr ="{\"name\":\"1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\",\"table\":\"bar_a1_03\",\"entity_id\":\"t_e0aaf3e13561477fba9fa1a7ab3dbd13\",\"id_database\":\"1\",\"sql\":\"select * from bar_a1_03\","
					+"\"fields\":[{\"name\":\"id\",\"entity_id\":\"f_eac3a58b465b40099bf994d1cf441c89\"},{\"name\":\"geom\",\"entity_id\":\"f_e58f237e43fd44e9b4b7f3e2fc931b8a\"},{\"name\":\"name\",\"entity_id\":\"f_269997a9f0bc419e8d38dee2b047da14\"},{\"name\":\"data\",\"entity_id\":\"f_f2b79396292c4fc49fb070609030760e\"}]}";
			jsonStr ="{\"name\":\"1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\",\"table\":\"camera\",\"entity_id\":\"t_058426787f8f4a3aac57f3a0233571b6\",\"id_database\":\"1\",\"sql\":\"select * from camera\","
					+"\"fields\":[{\"name\":\"id\",\"entity_id\":\"f_ef29cfe8e5ce4b1d805ee349f281045e\"},{\"name\":\"x\",\"entity_id\":\"f_6d5503c78dac4ffc97e097b34f29cddd\"},{\"name\":\"geom\",\"entity_id\":\"f_37a5bc724eca44b787ba9648ca922bfd\"},{\"name\":\"y\",\"entity_id\":\"f_318e74aa50364036bb0f6d0463ce2d30\"}]}";
			//			String jsonStr ="{\"name\":\"127.0.0.1\",\"type\":\"POSTGRESQL\",\"hostname\":\"127.0.0.1\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\",\"table\":\"test_f\",\"entity_id\":\"t_49f92fda2492423c84c81d4bab9a2733\",\"id_database\":\"1\",\"sql\":\"select * from test_f\",\"fields\":[{\"name\":\"id\",\"entity_id\":\"f_61b22c416bc2491b8c81ec86002de615\"},{\"name\":\"test_fname\",\"entity_id\":\"f_060bc2d1d8be4f458e10884a4c94e805\"},{\"name\":\"test_p_id\",\"entity_id\":\"f_6a820b0829f043dc9b671af80907958a\"}]}";
			//			String jsonStr ="{\"name\":\"10.211.55.7\",\"type\":\"MYSQL\",\"hostname\":\"10.211.55.7\",\"port\":\"3306\",\"databasename\":\"kettle\",\"username\":\"root\",\"password\":\"mysql\",\"fields\":[{\"name\":\"f1\",\"entity_id\":\"123456\"},{\"name\":\"f2\",\"entity_id\":\"654321\"}]}";
			//			String jsonStrs ="{\"name\":\"10.211.55.7\",\"type\":\"POSTGRESQL\",\"hostname\":\"10.211.55.7\",\"port\":\"5432\",\"databasename\":\"ltmap\",\"username\":\"postgres\",\"password\":\"123456\"}";
			Map data=JSONUtil.parserStrToMap(jsonStr);
			//			Map datas=JSONUtil.parserStrToMap(jsonStrs);
			//			data.put("sql", "select id,\"name\",geom,data from bar_a1_03");
			loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );

			Map localdata = new HashMap<>();
			localdata.put("id_database", 1);
			localDatabaseMeta = DatabaseCodec.decode(data);//query local connection config

			KettleDatabaseRepositoryMeta repInfo = new KettleDatabaseRepositoryMeta();
			repInfo.setConnection(localDatabaseMeta);
			repository = new KettleDatabaseRepository();
			repository.init( repInfo );
			repository.connect( "admin", "admin" );

			RepositoriesMeta repositories = new RepositoriesMeta();
			if(repositories.readData()) {
				DatabaseMeta previousMeta = repositories.searchDatabase(localDatabaseMeta.getName());
				if(previousMeta != null) {
					repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
				}
				repositories.addDatabase( localDatabaseMeta );
				repositories.writeData();
			}
			DatabaseMeta previousMeta = repositories.searchDatabase("1");

			RepositoryDirectoryInterface directory = repository.findDirectory(MapUtil.getString(data, "user_id"));
			if(directory == null)
				directory = repository.getUserHomeDirectory();

			if(repository.exists(transName, directory, RepositoryObjectType.TRANSFORMATION)) {
				throw new RuntimeException("该转换已经存在，请重新输入！");
			}
			TransMeta transMeta = new TransMeta();
			transMeta.setRepository(repository);
			transMeta.setName(transName);
			transMeta.setRepositoryDirectory(directory);
			transMeta.setTransstatus(-1);

			transMeta.importFromMetaStore();

			DatabaseMeta exist = transMeta.findDatabase(previousMeta.getName());
			if (exist == null) {
				transMeta.addDatabase(previousMeta);
			} else {
				if (!exist.isShared()) {
					int idx = transMeta.indexOfDatabase(exist);
					transMeta.removeDatabase(idx);
					transMeta.addDatabase(idx, previousMeta);
				}
			}

			List<Map> fields;
			fields = (List) data.get("fields");

			//in
			String instepid="CsvInput";
			String instepname=MapUtil.getString(data, "table","csv");
			PluginRegistry registry = PluginRegistry.getInstance();
			PluginInterface sp = registry.findPluginWithId( StepPluginType.class, instepid );
			StepMetaInterface stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
			//"id","geom","district","name","newclass"
			//			TextFileInputField[] tfields =new TextFileInputField[5];
			//			tfields[0]=new TextFileInputField("id", 50, 20);
			//			tfields[1]=new TextFileInputField("geom", 50, 20);
			//			tfields[2]=new TextFileInputField("district", 50, 20);
			//			tfields[3]=new TextFileInputField("name", 50, 20);
			//			tfields[4]=new TextFileInputField("newclass", 50, 20);
			CsvInputMeta tableInputMeta = (CsvInputMeta) stepMetaInterface;
			//			tableInputMeta.setFilename("/Users/Every/Desktop/test.csv");
			tableInputMeta.setDelimiter( "," );
			tableInputMeta.setEncoding( "utf-8" );
			tableInputMeta.setEnclosure( "\"" );
			tableInputMeta.setBufferSize( "1024" );
			tableInputMeta.setHeaderPresent( true );
			tableInputMeta.setRunningInParallel( true );
			//			tableInputMeta.setInputFields(tfields);

			tableInputMeta.setFilename("/Users/Every/Desktop/test.csv");

			//			inputMeta.setDelimiter( wDelimiter.getText() );
			//			inputMeta.setEnclosure( wEnclosure.getText() );
			//			inputMeta.setBufferSize( wBufferSize.getText() );
			//			inputMeta.setLazyConversionActive( wLazyConversion.getSelection() );
			//			inputMeta.setHeaderPresent( wHeaderPresent.getSelection() );
			//			inputMeta.setRowNumField( wRowNumField.getText() );
			//			inputMeta.setAddResultFile( wAddResult.getSelection() );
			//			inputMeta.setRunningInParallel( wRunningInParallel.getSelection() );
			//			inputMeta.setNewlinePossibleInFields( wNewlinePossible.getSelection() );
			//			inputMeta.setEncoding( wEncoding.getText() );

			String header = "id,geom,x,y";
			String[] headers = header.split(",");
			int nrNonEmptyFields =headers.length;
			tableInputMeta.allocate( nrNonEmptyFields );

			for ( int i = 0; i < nrNonEmptyFields; i++ ) {
				//CHECKSTYLE:Indentation:OFF
				tableInputMeta.getInputFields()[i] = new TextFileInputField();
				tableInputMeta.getInputFields()[i].setName( headers[i] );
				//				tableInputMeta.getInputFields()[i].setType( ValueMetaFactory.getIdForValueMeta( "String") );
				if (headers[i].equals("id")) {
					tableInputMeta.getInputFields()[i].setType(ValueMetaFactory.getIdForValueMeta("Integer"));
				} else if (headers[i].equals("x")||headers[i].equals("y")) {
					tableInputMeta.getInputFields()[i].setType(ValueMetaFactory.getIdForValueMeta("Number"));
				} else {
					tableInputMeta.getInputFields()[i].setType( ValueMetaFactory.getIdForValueMeta( "String") );
				}

				//				int colnr = 1;
				//				inputMeta.getInputFields()[i].setName( item.getText( colnr++ ) );
				//				inputMeta.getInputFields()[i].setType( ValueMetaFactory.getIdForValueMeta( item.getText( colnr++ ) ) );
				//				inputMeta.getInputFields()[i].setFormat( item.getText( colnr++ ) );
				//				inputMeta.getInputFields()[i].setLength( Const.toInt( item.getText( colnr++ ), -1 ) );
				//				inputMeta.getInputFields()[i].setPrecision( Const.toInt( item.getText( colnr++ ), -1 ) );
				//				inputMeta.getInputFields()[i].setCurrencySymbol( item.getText( colnr++ ) );
				//				inputMeta.getInputFields()[i].setDecimalSymbol( item.getText( colnr++ ) );
				//				inputMeta.getInputFields()[i].setGroupSymbol( item.getText( colnr++ ) );
				//				inputMeta.getInputFields()[i].setTrimType( ValueMetaString.getTrimTypeByDesc( item.getText( colnr++ ) ) );
			}

			//			tableInputMeta.setLazyConversionActive("Y".equalsIgnoreCase("N"));
			//			tableInputMeta.setExecuteEachInputRow("Y".equalsIgnoreCase(MapUtil.getString(data,"execute_each_row" )));
			//			tableInputMeta.setVariableReplacementActive("Y".equalsIgnoreCase(MapUtil.getString(data,"variables_active" )));
			//			tableInputMeta.setLazyConversionActive("Y".equalsIgnoreCase(MapUtil.getString(data,"lazy_conversion_active" )));

			//			String lookupFromStepname = MapUtil.getString(data,"lookup");
			//			StreamInterface infoStream = tableInputMeta.getStepIOMeta().getInfoStreams().get(0);
			//			infoStream.setSubject(lookupFromStepname);

			StepMeta inStepMeta = new StepMeta(instepid, instepname, stepMetaInterface);
			inStepMeta.setParentTransMeta( transMeta );
			if (inStepMeta.isMissing()) {
				transMeta.addMissingTrans((MissingTrans) inStepMeta.getStepMetaInterface());
			}
			StepMeta check = transMeta.findStep(inStepMeta.getName());
			if (check != null) {
				if (!check.isShared()) {
					// Don't overwrite shared objects
					transMeta.addOrReplaceStep(inStepMeta);
				} else {
					check.setDraw(inStepMeta.isDrawn()); // Just keep the  drawn flag  and location
					check.setLocation(inStepMeta.getLocation());
				}
			} else {
				transMeta.addStep(inStepMeta); // simply add it.
			}

			//out
			String outstepid="TableOutput";
			String outstepname=MapUtil.getString(data, "entity_id");
			sp = registry.findPluginWithId( StepPluginType.class, outstepid );
			stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
			TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMetaInterface;

			tableOutputMeta.setDatabaseMeta(DatabaseMeta.findDatabase(transMeta.getDatabases(),MapUtil.getString(data, "id_database")));
			tableOutputMeta.setSchemaName("public");
			tableOutputMeta.setTableName(outstepname);
			//			tableOutputMeta.setCommitSize(cell.getAttribute("commit"));
			//			tableOutputMeta.setTruncateTable("Y".equalsIgnoreCase(cell.getAttribute("truncate")));
			//			tableOutputMeta.setIgnoreErrors("Y".equalsIgnoreCase(cell.getAttribute("ignore_errors")));
			//			tableOutputMeta.setUseBatchUpdate("Y".equalsIgnoreCase(cell.getAttribute("use_batch")));
			//
			//			tableOutputMeta.setSpecifyFields("Y".equalsIgnoreCase(cell.getAttribute("specify_fields")));
			//			tableOutputMeta.setPartitioningEnabled("Y".equalsIgnoreCase(cell.getAttribute("partitioning_enabled")));
			//			tableOutputMeta.setPartitioningField(cell.getAttribute("partitioning_field"));
			//			tableOutputMeta.setPartitioningDaily("Y".equalsIgnoreCase(cell.getAttribute("partitioning_daily")));
			//			tableOutputMeta.setPartitioningMonthly("Y".equalsIgnoreCase(cell.getAttribute("partitioning_monthly")));
			//
			//			tableOutputMeta.setTableNameInField("Y".equalsIgnoreCase(cell.getAttribute("tablename_in_field")));
			//			tableOutputMeta.setTableNameField(cell.getAttribute("tablename_field"));
			//			tableOutputMeta.setTableNameInTable("Y".equalsIgnoreCase(cell.getAttribute("tablename_in_table")));
			//			tableOutputMeta.setReturningGeneratedKeys("Y".equalsIgnoreCase(cell.getAttribute("return_keys")));
			//			tableOutputMeta.setGeneratedKeyField(cell.getAttribute("return_field"));
			tableOutputMeta.setCommitSize("1000");
			tableOutputMeta.setTruncateTable(false);
			tableOutputMeta.setIgnoreErrors(false);
			tableOutputMeta.setUseBatchUpdate(true);

			tableOutputMeta.setSpecifyFields(true);
			tableOutputMeta.setPartitioningEnabled(false);
			tableOutputMeta.setPartitioningField("");
			tableOutputMeta.setPartitioningDaily(false);
			tableOutputMeta.setPartitioningMonthly(true);

			tableOutputMeta.setTableNameInField(false);
			tableOutputMeta.setTableNameField("");
			tableOutputMeta.setTableNameInTable(true);
			tableOutputMeta.setReturningGeneratedKeys(false);
			tableOutputMeta.setGeneratedKeyField("");

			List<String> fieldDatabase = new ArrayList<>();
			List<String> fieldStream = new ArrayList<>();
			for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
				fieldDatabase.add(MapUtil.getString(field,"entity_id"));
				//				fieldStream.add(localDatabaseMeta.quoteField(MapUtil.getString(field,"name")));
				fieldStream.add(MapUtil.getString(field,"name"));
			}
			tableOutputMeta.setFieldDatabase(fieldDatabase.toArray(new String[fields.size()]));
			tableOutputMeta.setFieldStream(fieldStream.toArray(new String[fields.size()]));

			StepMeta outStepMeta = new StepMeta(outstepid, outstepname, stepMetaInterface);
			outStepMeta.setParentTransMeta( transMeta );
			if (outStepMeta.isMissing()) {
				transMeta.addMissingTrans((MissingTrans) outStepMeta.getStepMetaInterface());
			}

			check = transMeta.findStep(outStepMeta.getName());
			if (check != null) {
				if (!check.isShared()) {
					// Don't overwrite shared objects
					transMeta.addOrReplaceStep(outStepMeta);
				} else {
					check.setDraw(outStepMeta.isDrawn()); // Just keep the  drawn flag  and location
					check.setLocation(outStepMeta.getLocation());
				}
			} else {
				transMeta.addStep(outStepMeta); // simply add it.
			}
			//			for (int i = 0; i < transMeta.nrSteps(); i++) {
			//				StepMeta stepMeta = transMeta.getStep(i);
			//				StepMetaInterface sii = stepMeta.getStepMetaInterface();
			//				if (sii != null) {
			//					sii.searchInfoAndTargetSteps(transMeta.getSteps());
			//				}
			//			}

			TransHopMeta hopinf = new TransHopMeta(null, null, true);
			String[] stepNames = transMeta.getStepNames();
			for (int j = 0; j < stepNames.length; j++) {
				if (stepNames[j].equalsIgnoreCase(instepname))
					hopinf.setFromStep(transMeta.getStep(j));
				if (stepNames[j].equalsIgnoreCase(outstepname))
					hopinf.setToStep(transMeta.getStep(j));
			}
			transMeta.addTransHop(hopinf);
			String executionConfiguration="{\"exec_local\":\"Y\",\"exec_remote\":\"N\",\"pass_export\":\"N\",\"exec_cluster\":\"N\",\"cluster_post\":\"Y\",\"cluster_prepare\":\"Y\",\"cluster_start\":\"Y\",\"cluster_show_trans\":\"N\",\"parameters\":[],\"variables\":[{\"name\":\"Internal.Entry.Current.Directory\",\"value\":\"/\"},{\"name\":\"Internal.Job.Filename.Directory\",\"value\":\"Parent Job File Directory\"},{\"name\":\"Internal.Job.Filename.Name\",\"value\":\"Parent Job Filename\"},{\"name\":\"Internal.Job.Name\",\"value\":\"Parent Job Name\"},{\"name\":\"Internal.Job.Repository.Directory\",\"value\":\"Parent Job Repository Directory\"}],\"arguments\":[],\"safe_mode\":\"N\",\"log_level\":\"Basic\",\"clear_log\":\"Y\",\"gather_metrics\":\"Y\",\"log_file\":\"N\",\"log_file_append\":\"N\",\"show_subcomponents\":\"Y\",\"create_parent_folder\":\"N\",\"remote_server\":\"\",\"replay_date\":\"\"}";
			JSONObject jsonObject = JSONObject.fromObject(executionConfiguration);
			TransExecutionConfiguration transExecutionConfiguration = TransExecutionConfigurationCodec.decode(jsonObject, transMeta);

			TransExecutor transExecutor = TransExecutor.initExecutor(transExecutionConfiguration, transMeta, repository, null, new LogService() {

				@Override
				public Logger getLogger(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Logger getLogger(Class clazz) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			Thread tr = new Thread(transExecutor, "TransExecutor_" + transExecutor.getExecutionId());
			tr.start();
			//			executions.put(transExecutor.getExecutionId(), transExecutor);

			Thread.sleep(1000);

			while(!transExecutor.isFinished()){
				jsonObject.put("1stepMeasure", transExecutor.getStepMeasure());
				jsonObject.put("log", transExecutor.getExecutionLog());
				jsonObject.put("stepStatus", transExecutor.getStepStatus());
				System.out.println(jsonObject);
			}

			jsonObject.put("1stepMeasure", transExecutor.getStepMeasure());
			jsonObject.put("log", transExecutor.getExecutionLog());
			jsonObject.put("stepStatus", transExecutor.getStepStatus());
			System.out.println(jsonObject);

			//			repository.save( transMeta, "add: " + new Date(), null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
