package org.oiue.service.event.etl.impl;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.etl.ETLService;
import org.oiue.service.event.etl.utils.DatabaseCodec;
import org.oiue.service.event.etl.utils.JSONObject;
import org.oiue.service.event.etl.utils.StringEscapeHelper;
import org.oiue.service.event.etl.utils.TransExecutionConfigurationCodec;
import org.oiue.service.event.etl.utils.TransExecutor;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.Application;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.list.ListUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.MetricsInterface;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

@SuppressWarnings("serial")
public class EventETLServiceImpl implements ETLService {
	public EventETLServiceImpl() {
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			e.printStackTrace();
		}
		//		pt.put("TableOutput", (StepDecoder) new TableOutput(null, null, 0, new TransMeta(), null));
	}
	//	public static Map<String, StepDecoder> pt = new HashMap<>();

	public static LogChannelInterface log=new LogChannelInterface() {

		@Override
		public void snap(MetricsInterface metric, String subject, long... value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void snap(MetricsInterface metric, long... value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setLogLevel(LogLevel logLevel) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setGatheringMetrics(boolean gatheringMetrics) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setForcingSeparateLogging(boolean forcingSeparateLogging) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setFilter(String filter) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setContainerObjectId(String containerObjectId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logRowlevel(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logRowlevel(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logMinimal(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logMinimal(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logError(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logError(String message, Throwable e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logError(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logDetailed(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logDetailed(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logDebug(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logDebug(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logBasic(String message, Object... arguments) {
			// TODO Auto-generated method stub

		}

		@Override
		public void logBasic(String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isRowLevel() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isGatheringMetrics() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isForcingSeparateLogging() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isError() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isDetailed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isDebug() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isBasic() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public LogLevel getLogLevel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getLogChannelId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContainerObjectId() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static KettleDatabaseRepository repository;
	static DatabaseMeta localDatabaseMeta ;
	public static LoggingObjectInterface loggingObject ;
	@Override
	public void updated(Dictionary<String, ?> props) {
		try {
			path = (String) props.get("rootPath");
		} catch (Exception e) {}
		try {
			loggingObject = new SimpleLoggingObject("DatabaseController", LoggingObjectType.DATABASE, null );

			Map localdata = new HashMap<>();
			localdata.put("id_database", 1);
			IResource iresource=factoryService.getBmo(IResource.class.getName());
			localDatabaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, localdata));//query local connection config

			KettleDatabaseRepositoryMeta repInfo = new KettleDatabaseRepositoryMeta();
			repInfo.setConnection(localDatabaseMeta);
			repository = new KettleDatabaseRepository();
			repository.init( repInfo );
			repository.connect( "admin", "admin" );
		} catch (Throwable e) {
			repository=null;
			logger.error(e.getMessage(), e);
		}
		if(path==null){
			path= Application.getRootPath()+"/reposity/";
		}
	}

	protected String path = null;
	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static LogService logService;
	protected static CacheServiceManager cache;
	protected static OnlineService onlineService;
	protected static FactoryService factoryService ;
	private static String data_source_name = null;

	@Override
	public Object getRepository(Map data, Map event, String tokenid) throws Throwable {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getDatabaseType(Map data, Map event, String tokenid) throws Throwable {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getAccessMethod(Map data, Map event, String tokenid) throws Throwable {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getAccessSetting(Map data, Map event, String tokenid) throws Throwable {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object setRepository(Map data, Map event, String tokenid) throws Throwable {
		String type = data.get("operation_type")+"";
		DatabaseMeta dbinfo;
		String[] remarks;
		switch (type) {
		case "test":
			dbinfo = DatabaseCodec.decode(data);
			remarks = dbinfo.checkParameters();
			if ( remarks.length == 0 ) {

				Database db = new Database( loggingObject, dbinfo );
				try {
					db.connect();
					String reportMessage = dbinfo.testConnection();
					return reportMessage;
				} finally {
					db.disconnect();
				}
			}else{
				throw new RuntimeException("parameters is error");
			}
		case "save":
			dbinfo = DatabaseCodec.decode(data);
			remarks = dbinfo.checkParameters();
			if ( remarks.length == 0 ) {
				Database db = new Database( loggingObject, dbinfo );
				try {
					db.connect();

					RepositoriesMeta repositories = new RepositoriesMeta();
					if(repositories.readData()) {
						DatabaseMeta previousMeta = repositories.searchDatabase(dbinfo.getName());
						if(previousMeta != null) {
							repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
						}
						repositories.addDatabase( dbinfo );
						repositories.writeData();
					}

					IResource iresource=factoryService.getBmo(IResource.class.getName());
					if (iresource != null) {
						Object rto = iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
						if(rto instanceof Map){
							iresource=factoryService.getBmo(IResource.class.getName());
							data.put("id_database", MapUtil.getString((Map<String, Object>) rto, "id_database"));
							iresource.callEvent("47e18608-d632-4c8f-88dc-36838be5c7c5", data_source_name, data);
						}
						return rto;
					}else{
						throw new RuntimeException("service can not init！");
					}
				} finally {
					db.disconnect();
				}
			}else{
				throw new RuntimeException("parameters is error");
			}

		default:
			break;
		}
		return null;
	}
	@SuppressWarnings("deprecation")
	@Override
	public Object getEntity(Map data, Map event, String tokenid) throws Throwable {
		String type = MapUtil.getString(data, "operation_type");
		ArrayList result = new ArrayList();

		IResource iresource=factoryService.getBmo(IResource.class.getName());
		switch (type) {
		case "res":
			if (iresource != null) {
				return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
			}else{
				throw new RuntimeException("service can not init！");
			}

		case "repository":
			//		String databases="{\"streamingResults\":true,\"MSSQLUseIntegratedSecurity\":false,\"extraOptions\":[],\"supportBooleanDataType\":true,\"supportTimestampDataType\":true,\"quoteIdentifiersCheck\":false,\"lowerCaseIdentifiersCheck\":false,\"upperCaseIdentifiersCheck\":false,\"preserveReservedCaseCheck\":true,\"partitioned\":\"N\",\"partitionInfo\":[],\"usingConnectionPool\":\"N\",\"initialPoolSize\":5,\"maximumPoolSize\":10,\"access\":0,\"pool_params\":[{\"enabled\":false,\"name\":\"defaultAutoCommit\",\"defValue\":\"true\",\"description\":\"The%20default%20auto-commit%20state%20of%20connections%20created%20by%20this%20pool.\"},{\"enabled\":false,\"name\":\"defaultReadOnly\",\"description\":\"The%20default%20read-only%20state%20of%20connections%20created%20by%20this%20pool.%0AIf%20not%20set%20then%20the%20setReadOnly%20method%20will%20not%20be%20called.%0A%20%28Some%20drivers%20don%27t%20support%20read%20only%20mode%2C%20ex%3A%20Informix%29\"},{\"enabled\":false,\"name\":\"defaultTransactionIsolation\",\"description\":\"the%20default%20TransactionIsolation%20state%20of%20connections%20created%20by%20this%20pool.%20One%20of%20the%20following%3A%20%28see%20javadoc%29%0A%0A%20%20*%20NONE%0A%20%20*%20READ_COMMITTED%0A%20%20*%20READ_UNCOMMITTED%0A%20%20*%20REPEATABLE_READ%20%20*%20SERIALIZABLE%0A\"},{\"enabled\":false,\"name\":\"defaultCatalog\",\"description\":\"The%20default%20catalog%20of%20connections%20created%20by%20this%20pool.\"},{\"enabled\":false,\"name\":\"initialSize\",\"defValue\":\"0\",\"description\":\"The%20initial%20number%20of%20connections%20that%20are%20created%20when%20the%20pool%20is%20started.\"},{\"enabled\":false,\"name\":\"maxActive\",\"defValue\":\"8\",\"description\":\"The%20maximum%20number%20of%20active%20connections%20that%20can%20be%20allocated%20from%20this%20pool%20at%20the%20same%20time%2C%20or%20non-positive%20for%20no%20limit.\"},{\"enabled\":false,\"name\":\"maxIdle\",\"defValue\":\"8\",\"description\":\"The%20maximum%20number%20of%20connections%20that%20can%20remain%20idle%20in%20the%20pool%2C%20without%20extra%20ones%20being%20released%2C%20or%20negative%20for%20no%20limit.\"},{\"enabled\":false,\"name\":\"minIdle\",\"defValue\":\"0\",\"description\":\"The%20minimum%20number%20of%20connections%20that%20can%20remain%20idle%20in%20the%20pool%2C%20without%20extra%20ones%20being%20created%2C%20or%20zero%20to%20create%20none.\"},{\"enabled\":false,\"name\":\"maxWait\",\"defValue\":\"-1\",\"description\":\"The%20maximum%20number%20of%20milliseconds%20that%20the%20pool%20will%20wait%20%28when%20there%20are%20no%20available%20connections%29%20for%20a%20connection%20to%20be%20returned%20before%20throwing%20an%20exception%2C%20or%20-1%20to%20wait%20indefinitely.\"},{\"enabled\":false,\"name\":\"validationQuery\",\"description\":\"The%20SQL%20query%20that%20will%20be%20used%20to%20validate%20connections%20from%20this%20pool%20before%20returning%20them%20to%20the%20caller.%0AIf%20specified%2C%20this%20query%20MUST%20be%20an%20SQL%20SELECT%20statement%20that%20returns%20at%20least%20one%20row.\"},{\"enabled\":false,\"name\":\"testOnBorrow\",\"defValue\":\"true\",\"description\":\"The%20indication%20of%20whether%20objects%20will%20be%20validated%20before%20being%20borrowed%20from%20the%20pool.%0AIf%20the%20object%20fails%20to%20validate%2C%20it%20will%20be%20dropped%20from%20the%20pool%2C%20and%20we%20will%20attempt%20to%20borrow%20another.%0ANOTE%20-%20for%20a%20true%20value%20to%20have%20any%20effect%2C%20the%20validationQuery%20parameter%20must%20be%20set%20to%20a%20non-null%20string.\"},{\"enabled\":false,\"name\":\"testOnReturn\",\"defValue\":\"false\",\"description\":\"The%20indication%20of%20whether%20objects%20will%20be%20validated%20before%20being%20returned%20to%20the%20pool.%0ANOTE%20-%20for%20a%20true%20value%20to%20have%20any%20effect%2C%20the%20validationQuery%20parameter%20must%20be%20set%20to%20a%20non-null%20string.\"},{\"enabled\":false,\"name\":\"testWhileIdle\",\"defValue\":\"false\",\"description\":\"The%20indication%20of%20whether%20objects%20will%20be%20validated%20by%20the%20idle%20object%20evictor%20%28if%20any%29.%20If%20an%20object%20fails%20to%20validate%2C%20it%20will%20be%20dropped%20from%20the%20pool.%0ANOTE%20-%20for%20a%20true%20value%20to%20have%20any%20effect%2C%20the%20validationQuery%20parameter%20must%20be%20set%20to%20a%20non-null%20string.\"},{\"enabled\":false,\"name\":\"timeBetweenEvictionRunsMillis\",\"description\":\"The%20number%20of%20milliseconds%20to%20sleep%20between%20runs%20of%20the%20idle%20object%20evictor%20thread.%20When%20non-positive%2C%20no%20idle%20object%20evictor%20thread%20will%20be%20run.\"},{\"enabled\":false,\"name\":\"poolPreparedStatements\",\"defValue\":\"false\",\"description\":\"Enable%20prepared%20statement%20pooling%20for%20this%20pool.\"},{\"enabled\":false,\"name\":\"maxOpenPreparedStatements\",\"defValue\":\"-1\",\"description\":\"The%20maximum%20number%20of%20open%20statements%20that%20can%20be%20allocated%20from%20the%20statement%20pool%20at%20the%20same%20time%2C%20or%20zero%20for%20no%20limit.\"},{\"enabled\":false,\"name\":\"accessToUnderlyingConnectionAllowed\",\"defValue\":\"false\",\"description\":\"Controls%20if%20the%20PoolGuard%20allows%20access%20to%20the%20underlying%20connection.\"},{\"enabled\":false,\"name\":\"removeAbandoned\",\"defValue\":\"false\",\"description\":\"Flag%20to%20remove%20abandoned%20connections%20if%20they%20exceed%20the%20removeAbandonedTimout.%0AIf%20set%20to%20true%20a%20connection%20is%20considered%20abandoned%20and%20eligible%20for%20removal%20if%20it%20has%20been%20idle%20longer%20than%20the%20removeAbandonedTimeout.%20Setting%20this%20to%20true%20can%20recover%20db%20connections%20from%20poorly%20written%20applications%20which%20fail%20to%20close%20a%20connection.\"},{\"enabled\":false,\"name\":\"removeAbandonedTimeout\",\"defValue\":\"300\",\"description\":\"Timeout%20in%20seconds%20before%20an%20abandoned%20connection%20can%20be%20removed.\"},{\"enabled\":false,\"name\":\"logAbandoned\",\"defValue\":\"false\",\"description\":\"Flag%20to%20log%20stack%20traces%20for%20application%20code%20which%20abandoned%20a%20Statement%20or%20Connection.%0ALogging%20of%20abandoned%20Statements%20and%20Connections%20adds%20overhead%20for%20every%20Connection%20open%20or%20new%20Statement%20because%20a%20stack%20trace%20has%20to%20be%20generated.\"}],\"read_only\":false}";
			//		Map dm = JSONUtil.parserStrToMap(databases);

			String nodeId = MapUtil.getString(data, "node_id");
			//			String repositoryId = MapUtil.getString(data, "repository_id");
			DatabaseMeta databaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, data));

			if(!StringUtil.isEmptys(nodeId)) {
				if("schema".equals(nodeId)) {
					Database db = new Database( loggingObject, databaseMeta );
					try {
						db.connect();
						DatabaseMetaData dbmd = db.getDatabaseMetaData();
						Map<String, String> connectionExtraOptions = databaseMeta.getExtraOptions();
						if (dbmd.supportsSchemasInTableDefinitions()) {
							ArrayList<String> list = new ArrayList<String>();

							String schemaFilterKey = databaseMeta.getPluginId() + "." + DatabaseMetaInformation.FILTER_SCHEMA_LIST;
							if ((connectionExtraOptions != null) && connectionExtraOptions.containsKey(schemaFilterKey)) {
								String schemasFilterCommaList = connectionExtraOptions.get(schemaFilterKey);
								String[] schemasFilterArray = schemasFilterCommaList.split(",");
								for (int i = 0; i < schemasFilterArray.length; i++) {
									list.add(schemasFilterArray[i].trim());
								}
							}
							if (list.size() == 0) {
								String sql = databaseMeta.getSQLListOfSchemas();
								if (!Const.isEmpty(sql)) {
									Statement schemaStatement = db.getConnection().createStatement();
									ResultSet schemaResultSet = schemaStatement.executeQuery(sql);
									while (schemaResultSet != null && schemaResultSet.next()) {
										String schemaName = schemaResultSet.getString("name");
										list.add(schemaName);
									}
									schemaResultSet.close();
									schemaStatement.close();
								} else {
									ResultSet schemaResultSet = dbmd.getSchemas();
									while (schemaResultSet != null && schemaResultSet.next()) {
										String schemaName = schemaResultSet.getString(1);
										list.add(schemaName);
									}
									schemaResultSet.close();
								}
							}

							for(String schema : list){
								Map schemam = new HashMap<>();
								schemam.put("text", schema);
								schemam.put("value", "schema");
								result.add(schemam);
							}
						}

					} finally {
						db.disconnect();
					}
				}  else if("schemaTable".equals(nodeId)) {
					Database db = new Database( loggingObject, databaseMeta );
					try {
						db.connect();
						String text = MapUtil.getString(data, "text");
						DatabaseMetaData dbmd = db.getDatabaseMetaData();
						ResultSet rs = dbmd.getTables(null, text, null, null);
						try {
							while (rs.next()) {
								String tableName = rs.getString(3);
								if (!db.isSystemTable(tableName)) {
									Map tableN = new HashMap<>();
									tableN.put("text", tableName);
									tableN.put("value", tableName);
									result.add(tableN);
								}
							}
						} finally {
							rs.close();
						}

					} finally {
						db.disconnect();
					}
				} else if("table".equals(nodeId)) {
					Database db = new Database( loggingObject, databaseMeta );
					try {
						db.connect();

						Map<String, Collection<String>> tableMap = db.getTableMap();
						List<String> tableKeys = new ArrayList<String>(tableMap.keySet());
						Collections.sort(tableKeys);
						for (String schema : tableKeys) {
							List<String> tables = new ArrayList<String>(tableMap.get(schema));
							Collections.sort(tables);
							for (String tableName : tables){
								Map tableN = new HashMap<>();
								tableN.put("text", tableName);
								tableN.put("value", tableName);
								tableN.put("type", "datatable");
								result.add(tableN);
							}
						}
					} finally {
						db.disconnect();
					}
				} else if("view".equals(nodeId)) {
					Database db = new Database( loggingObject, databaseMeta );
					try {
						db.connect();

						Map<String, Collection<String>> viewMap = db.getViewMap();
						List<String> viewKeys = new ArrayList<String>(viewMap.keySet());
						Collections.sort(viewKeys);
						for (String schema : viewKeys) {
							List<String> views = new ArrayList<String>(viewMap.get(schema));
							Collections.sort(views);
							for (String viewName : views){
								Map tableN = new HashMap<>();
								tableN.put("text", viewName);
								tableN.put("value", viewName);
								tableN.put("type", "dataview");
								result.add(tableN);
							}
						}
					} finally {
						db.disconnect();
					}
				} else if("synonym".equals(nodeId) ) {
					Database db = new Database( loggingObject, databaseMeta );
					try {
						db.connect();

						Map<String, Collection<String>> synonymMap = db.getSynonymMap();
						List<String> synonymKeys = new ArrayList<String>(synonymMap.keySet());
						Collections.sort(synonymKeys);
						for (String schema : synonymKeys) {
							List<String> synonyms = new ArrayList<String>(synonymMap.get(schema));
							Collections.sort(synonyms);
							for (String synonymName : synonyms){
								Map tableN = new HashMap<>();
								tableN.put("text", synonymName);
								tableN.put("value", synonymName);
								tableN.put("type", "synonym");
								result.add(tableN);
							}
						}
					} finally {
						db.disconnect();
					}
				}
			} else {

				Database db = new Database( loggingObject, databaseMeta );
				try {
					db.connect();
				}finally {
					db.disconnect();
				}
				List root_child_node = new ArrayList<>();
				Map schema = new HashMap<>();
				schema.put("text", "模式");
				schema.put("value", "schema");
				//				schema.put("hasChild", true);
				root_child_node.add(schema);

				Map table = new HashMap<>();
				table.put("text", "表");
				table.put("value", "table");
				table.put("hasChild", true);
				root_child_node.add(table);

				Map view = new HashMap<>();
				view.put("text", "视图");
				view.put("value", "view");
				view.put("hasChild", true);
				root_child_node.add(view);

				Map synonym = new HashMap<>();
				synonym.put("text", "同义词");
				synonym.put("value", "synonym");
				synonym.put("hasChild", true);
				root_child_node.add(synonym);

				Map rootNode = new HashMap<>();
				rootNode.put("text", databaseMeta.getName());
				rootNode.put("value", databaseMeta.getName());
				rootNode.put("type", "root");
				rootNode.put("child",root_child_node);
				result.add(rootNode);
			}
			break;
		}

		return result;
	}
	@Override
	public Object getEntityColumns(Map data, Map event, String tokenid) throws Throwable {
		//		String databaseName = MapUtil.getString(data, "databaseName");
		String type = MapUtil.getString(data, "operation_type");

		List jsonArray = new ArrayList<>();
		IResource iresource=factoryService.getBmo(IResource.class.getName());

		DatabaseMeta databaseMeta ;
		Database db;
		switch (type) {
		case "res":
			if (iresource != null) {
				return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
			}else{
				throw new RuntimeException("service can not init！");
			}
		case "repository_sql":
			databaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, data));
			String sql = MapUtil.getString(data, "sql");

			db = new Database( loggingObject, databaseMeta );
			try {
				db.connect();
				RowMetaInterface fields = db.getQueryFieldsFromPreparedStatement(sql);
				if (fields != null) {
					for (int i = 0; i < fields.size(); i++) {
						ValueMetaInterface field = fields.getValueMeta(i);
						Map jsonObject = new HashMap();
						//				jsonObject.put("name", inf.quoteField(field.getName()));
						jsonObject.put("name", field.getName());
						jsonObject.put("type", field.getTypeDesc());
						jsonObject.put("comments", field.getComments());
						jsonObject.put("length", field.getLength());
						jsonArray.add(jsonObject);
					}
				}
			} finally {
				db.disconnect();
			}
			break;
		case "repository":
			databaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, data));

			String schema = MapUtil.getString(data, "schema");
			String table = MapUtil.getString(data, "table");

			if(schema==null)
				schema="";

			db = new Database( loggingObject, databaseMeta );
			try {
				db.connect();

				String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema,table);
				RowMetaInterface fields = db.getTableFields(schemaTable);
				if (fields != null) {
					for (int i = 0; i < fields.size(); i++) {
						ValueMetaInterface field = fields.getValueMeta(i);
						Map jsonObject = new HashMap();
						//				jsonObject.put("name", inf.quoteField(field.getName()));
						jsonObject.put("name", field.getName());
						jsonObject.put("type", field.getTypeDesc());
						jsonObject.put("comments", field.getComments());
						jsonObject.put("length", field.getLength());
						jsonArray.add(jsonObject);
					}
				}

			} finally {
				db.disconnect();
			}
			break;
		}
		return jsonArray;
	}

	private AbstractMeta getTrans(String transId) throws Exception{
		TransMeta transMeta = new TransMeta(path+transId+".ktr");
		return transMeta;
	}
	private Repository getRepository(String repositoryId) throws Exception{
		Repository repository = null;
		return repository;
	}
	private DelegatingMetaStore getMetaStore(String metaStoreId) throws Exception{
		DelegatingMetaStore metaStore = null;
		return metaStore;
	}

	@Override
	public Object setEntityColumns(Map data, Map event, String tokenid) throws Throwable {
		String type = MapUtil.getString(data, "operation_type");

		DatabaseMeta databaseMeta ;
		Database db;
		Map<String,ValueMetaInterface> temp_fields;
		RowMetaInterface rfields;

		IResource iresource=factoryService.getBmo(IResource.class.getName());
		switch (type) {
		case "create_entity_table":
			data.put("type", 1);
			String table = MapUtil.getString(data, "table");

			databaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, data));
			db = new Database( loggingObject, databaseMeta );
			try {
				db.connect();
				rfields =db.getTableFields(table);
				List tf = new ArrayList<>();
				temp_fields=new HashMap<>();
				if (rfields != null) {
					for (int i = 0; i < rfields.size(); i++) {
						ValueMetaInterface field = rfields.getValueMeta(i);
						temp_fields.put(field.getName(), field);
						tf.add(field.getName());
					}
					if(tf.size()>0)
						data.put("sql", "select "+ListUtil.ArrayJoin(tf.toArray(), ",") + " from "+ table );
				}
				insertAndCreateEntity(data,temp_fields);
				return insertEntitySource(data);
			}finally {
				db.disconnect();
			}
		case "create_entity_sql":
			data.put("type", 100);
			String sql = MapUtil.getString(data, "sql");

			databaseMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, data));//query from connection config
			db = new Database( loggingObject, databaseMeta );
			try {
				db.connect();
				rfields =db.getQueryFieldsFromPreparedStatement(sql);
				temp_fields=new HashMap<>();
				if (rfields != null) {
					for (int i = 0; i < rfields.size(); i++) {
						ValueMetaInterface field = rfields.getValueMeta(i);
						temp_fields.put(field.getName(), field);
					}
				}
				insertAndCreateEntity(data, temp_fields);
				return insertEntitySource(data);
			} finally {
				db.disconnect();
			}


		default:
			throw new RuntimeException("operation_type error!");
		}
	}
	private void insertAndCreateEntity(Map data,Map<String,ValueMetaInterface> src_fields)  throws Throwable{
		IResource iresource=factoryService.getBmo(IResource.class.getName());

		List<Map> fields;
		fields = (List) data.get("fields");
		Database localdb = new Database( loggingObject, localDatabaseMeta );
		try {
			localdb.connect();
			String n_table_name = "t_"+UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("92363b9e-e4b8-4e7e-bba2-78e53a10f313", data_source_name, data);//insert entity

			RowMetaInterface rmi=localdb.getQueryFieldsFromPreparedStatement("select");
			int i =0;
			for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
				ValueMetaInterface tf = src_fields.get(MapUtil.getString(field, "name"));
				String n_field_name = "f_"+UUID.randomUUID().toString().replace("-", "");
				field.put("entity_column_id", n_field_name);
				field.put("entity_id", n_table_name);
				field.put("type", tf.getType()+"");
				field.put("precision", tf.getPrecision());
				field.put("scale", tf.getOriginalScale());
				field.put("sort", i++);
				field.put("user_id", data.get("user_id"));
				field.put("ispk",(boolean) field.get("ispk")?1:0);
				tf.setName(n_field_name);
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("8e656a8f-5864-4583-8a5c-99a4963c4250", data_source_name, field);//insert entity column
				rmi.addValueMeta(tf);
			}
			data.put("relation", JSONUtil.parserToStr(fields));
			localdb.execStatement(localdb.getDDL(n_table_name, rmi));
		}finally {
			localdb.disconnect();
		}
	}

	private Object insertEntitySource(Map data)throws Throwable{
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		Object ro = iresource.callEvent("46efdb82-eec3-464f-93f7-8a21afa9886e", data_source_name, data);//insert entity source

		RepositoryDirectoryInterface directory = repository.findDirectory(MapUtil.getString(data, "user_id"));
		if(directory == null)
			directory = repository.getUserHomeDirectory();

		String transName = MapUtil.getString(data, "transName");
		if(StringUtil.isEmptys(transName)){
			if(ro instanceof Map)
				transName = MapUtil.get((Map) ro, "root.entity_source_id")+"";
		}
		if(repository.exists(transName, directory, RepositoryObjectType.TRANSFORMATION)) {
			throw new RuntimeException("该转换已经存在，请重新输入！");
		}

		TransMeta transMeta = new TransMeta();
		transMeta.setRepository(repository);
		transMeta.setName(transName);
		transMeta.setRepositoryDirectory(directory);

		//		repository.save(transMeta, "add: " + new Date(), null);

		String transPath = directory.getPath();
		if(!transPath.endsWith("/"))
			transPath = transPath + '/';
		transPath = transPath + transName;

		ObjectId existingId = repository.getTransformationID( transMeta.getName(), transMeta.getRepositoryDirectory() );
		if(transMeta.getCreatedDate() == null)
			transMeta.setCreatedDate(new Date());
		if(transMeta.getObjectId() == null)
			transMeta.setObjectId(existingId);
		transMeta.setModifiedDate(new Date());

		boolean versioningEnabled = true;
		boolean versionCommentsEnabled = true;
		String fullPath = transMeta.getRepositoryDirectory() + "/" + transMeta.getName() + transMeta.getRepositoryElementType().getExtension();
		RepositorySecurityProvider repositorySecurityProvider = repository.getSecurityProvider() != null ? repository.getSecurityProvider() : null;
		if ( repositorySecurityProvider != null ) {
			versioningEnabled = repositorySecurityProvider.isVersioningEnabled( fullPath );
			versionCommentsEnabled = repositorySecurityProvider.allowsVersionComments( fullPath );
		}
		String versionComment = null;
		if (!versioningEnabled || !versionCommentsEnabled) {
			versionComment = "";
		} else {
			versionComment = "no comment";
		}

		transMeta.importFromMetaStore();

		RepositoriesMeta repositories = new RepositoriesMeta();
		if(repositories.readData()) {
			DatabaseMeta previousMeta = repositories.searchDatabase(localDatabaseMeta.getName());
			if(previousMeta != null) {
				repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
			}
			repositories.addDatabase( localDatabaseMeta );
			repositories.writeData();
		}
		DatabaseMeta previousMeta = repositories.searchDatabase(MapUtil.getString(data, "id_database"));

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

		if(existingId==null){
			repository.save( transMeta, "add: " + new Date(), null);
			existingId = repository.getTransformationID( transMeta.getName(), transMeta.getRepositoryDirectory() );
		}
		List<Map> fields;
		fields = (List) data.get("fields");

		//		//in
		//		String instepid="TableInput";
		//		String instepname=MapUtil.getString(data, "table");
		//		PluginRegistry registry = PluginRegistry.getInstance();
		//		PluginInterface sp = registry.findPluginWithId( StepPluginType.class, instepid );
		//		StepMetaInterface stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
		//
		//		StepMeta inStepMeta = new StepMeta(instepid, instepname, stepMetaInterface);
		//		inStepMeta.setParentTransMeta( transMeta );
		//		if (inStepMeta.isMissing()) {
		//			transMeta.addMissingTrans((MissingTrans) inStepMeta.getStepMetaInterface());
		//		}
		//
		//		StepMeta check = transMeta.findStep(inStepMeta.getName());
		//		if (check != null) {
		//			if (!check.isShared()) {
		//				// Don't overwrite shared objects
		//				transMeta.addOrReplaceStep(inStepMeta);
		//			} else {
		//				check.setDraw(inStepMeta.isDrawn()); // Just keep the  drawn flag  and location
		//				check.setLocation(inStepMeta.getLocation());
		//			}
		//		} else {
		//			transMeta.addStep(inStepMeta); // simply add it.
		//		}
		//
		//		//out
		//		String outstepid="TableOutput";
		//		String outstepname=MapUtil.getString(data, "entity_id");
		//		sp = registry.findPluginWithId( StepPluginType.class, outstepid );
		//		stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
		//		TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMetaInterface;
		//		List<String> fieldDatabase = new ArrayList<>();
		//		List<String> fieldStream = new ArrayList<>();
		//		for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
		//			fieldDatabase.add(MapUtil.getString(field,"entity_id"));
		//			fieldStream.add(MapUtil.getString(field,"name"));
		//		}
		//		String[] a=new String[fields.size()];
		//		tableOutputMeta.setFieldDatabase(fieldDatabase.toArray(a));
		//		tableOutputMeta.setFieldStream(fieldStream.toArray(a));
		//
		//		StepMeta outStepMeta = new StepMeta(outstepid, outstepname, stepMetaInterface);
		//		outStepMeta.setParentTransMeta( transMeta );
		//		if (outStepMeta.isMissing()) {
		//			transMeta.addMissingTrans((MissingTrans) outStepMeta.getStepMetaInterface());
		//		}
		//
		//		check = transMeta.findStep(outStepMeta.getName());
		//		if (check != null) {
		//			if (!check.isShared()) {
		//				// Don't overwrite shared objects
		//				transMeta.addOrReplaceStep(outStepMeta);
		//			} else {
		//				check.setDraw(outStepMeta.isDrawn()); // Just keep the  drawn flag  and location
		//				check.setLocation(outStepMeta.getLocation());
		//			}
		//		} else {
		//			transMeta.addStep(outStepMeta); // simply add it.
		//		}
		//
		//		repository.save( transMeta, versionComment, null);
		//
		//		String graphXml="";
		//		String executionConfiguration="";
		//		GraphCodec codec = new TransMetaCodec();
		//		transMeta = (TransMeta) codec.decode(graphXml);
		//
		//		JSONObject jsonObject = JSONObject.fromObject(executionConfiguration);
		//		TransExecutionConfiguration transExecutionConfiguration = TransExecutionConfigurationCodec.decode(jsonObject, transMeta);
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
			fieldDatabase.add(MapUtil.getString(field,"entity_column_id"));
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

		TransExecutor transExecutor = TransExecutor.initExecutor(transExecutionConfiguration, transMeta, repository, null, logService);
		Thread tr = new Thread(transExecutor, "TransExecutor_" + transExecutor.getExecutionId());
		tr.start();
		executions.put(transExecutor.getExecutionId(), transExecutor);
		if(ro instanceof Map){
			((Map) ro).put("execution_id", transExecutor.getExecutionId());
			((Map) ro).put("id_transformation", transMeta.getObjectId());
		}
		return ro;
	}

	@Override
	public Object setEntityRelation(Map data, Map event, String tokenid) throws Throwable {
		String type = MapUtil.getString(data, "operation_type");
		List<Map> fields = (List) data.get("fields");
		switch (type) {
		case "entity_table":
			data.put("type", 1);
			String table = MapUtil.getString(data, "table");
			if(fields!=null){
				List tf = new ArrayList<>();
				for (Map field : fields) {
					tf.add(MapUtil.getString(field, "name"));
				}
				if(tf.size()>0)
					data.put("sql", "select "+ListUtil.ArrayJoin(tf.toArray(), ",") + " from "+ table );
			}
			data.put("transName", "transformation_"+table+"_to_"+MapUtil.getString(data, "entity_id"));
			break;
		case "entity_sql":
			data.put("type", 100);
			break;

		default:
			break;
		}
		data.put("relation", JSONUtil.parserToStr(fields));
		return insertEntitySource(data);

	}
	@Override
	public Object savaTrans(Map data, Map event, String tokenid) throws Throwable {
		return null;
	}
	@Override
	public Object runTrans(Map data, Map event, String tokenid) throws Throwable {
		return null;
	}
	@Override
	public Object initRun(Map data, Map event, String tokenid) throws Throwable {
		String transId = MapUtil.getString(data, "trans_id");
		String repositoryId = MapUtil.getString(data, "id_database");
		String metaStoreId = MapUtil.getString(data, "metaStore_id");
		TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
		TransExecutor transExecutor = TransExecutor.initExecutor(executionConfiguration, (TransMeta) getTrans(transId), getRepository(repositoryId), getMetaStore(metaStoreId), logService);
		Thread tr = new Thread(transExecutor, "TransExecutor_" + transExecutor.getExecutionId());
		tr.start();
		executions.put(transExecutor.getExecutionId(), transExecutor);

		return transExecutor.getExecutionId();
	}

	private static HashMap<String, TransExecutor> executions = new HashMap<String, TransExecutor>();

	@Override
	public Object result(Map data, Map event, String tokenid) throws Throwable{
		String executionId = MapUtil.getString(data, "execution_id");
		Map jsonObject = new HashMap<>();

		TransExecutor transExecutor = executions.get(executionId);

		jsonObject.put("finished", transExecutor.isFinished());
		if(transExecutor.isFinished()) {
			executions.remove(executionId);

			jsonObject.put("stepMeasure", transExecutor.getStepMeasure());
			jsonObject.put("log", transExecutor.getExecutionLog());
			jsonObject.put("stepStatus", transExecutor.getStepStatus());
			//			jsonObject.put("previewData", transExecutor.getPreviewData());
		} else {
			jsonObject.put("stepMeasure", transExecutor.getStepMeasure());
			jsonObject.put("log", transExecutor.getExecutionLog());
			jsonObject.put("stepStatus", transExecutor.getStepStatus());
			//			jsonObject.put("previewData", transExecutor.getPreviewData());
		}
		return jsonObject;

	}
}
