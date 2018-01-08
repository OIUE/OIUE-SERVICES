package org.oiue.service.event.etl.impl;

import java.nio.charset.Charset;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
import org.oiue.service.event.etl.utils.TransExecutionConfigurationCodec;
import org.oiue.service.event.etl.utils.TransExecutor;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.event.api.EventConvertService;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.Application;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
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
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
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
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import com.csvreader.CsvReader;

@SuppressWarnings({ "rawtypes", "unchecked", "serial"})
public class EventETLServiceImpl implements ETLService {
	public EventETLServiceImpl() {
	}

//	public static LogChannelInterface log=new LogChannel(EventETLServiceImpl.class);

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
			localdata.put("id_database", "local");
			IResource iresource=factoryService.getBmo(IResource.class.getName());
			localdata =  (Map)iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, localdata);
			
			KettleEnvironment.init();
			localDatabaseMeta = DatabaseCodec.decode(localdata);//local connection config

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

	public static String path = null;
	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static LogService logService;
	protected static CacheServiceManager cache;
	protected static OnlineService onlineService;
	protected static FactoryService factoryService ;
	private static String data_source_name = null;

	@Override
	public Object getRepository(Map data, Map event, String tokenid) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getDatabaseType(Map data, Map event, String tokenid) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getAccessMethod(Map data, Map event, String tokenid) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object getAccessSetting(Map data, Map event, String tokenid) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
		}
		throw new RuntimeException("service can not init！");
	}
	@Override
	public Object setRepository(Map data, Map event, String tokenid) {
			String type = data.get("operation_type") + "";
			DatabaseMeta dbinfo;
			String[] remarks;
			switch (type) {
			case "test":
				dbinfo = DatabaseCodec.decode(data);
				remarks = dbinfo.checkParameters();
				if (remarks.length == 0) {

					Database db = new Database(loggingObject, dbinfo);
					try {
						try {
							db.connect();
						} catch (KettleDatabaseException e) {
							throw new OIUEException(StatusResult._conn_error, dbinfo,e);
						}
						String reportMessage = dbinfo.testConnection();
						return reportMessage;
					} finally {
						db.disconnect();
					}
				} else {
					throw new RuntimeException("parameters is error");
				}
			case "save":
				dbinfo = DatabaseCodec.decode(data);
				remarks = dbinfo.checkParameters();
				if (remarks.length == 0) {
					Database db = new Database(loggingObject, dbinfo);
					try {
						try {
							db.connect();
						} catch (KettleDatabaseException e) {
							throw new OIUEException(StatusResult._conn_error, dbinfo,e);
						}

						IResource iresource = factoryService.getBmo(IResource.class.getName());
						if (iresource != null) {
							data.put("port", MapUtil.getInt(data, "port", 0));
							Object rto = iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
							if (rto instanceof Map) {
								Object dbname = MapUtil.get(data, "name");
								iresource = factoryService.getBmo(IResource.class.getName());
								data.put("id_database", dbname);
								iresource.callEvent("47e18608-d632-4c8f-88dc-36838be5c7c5", data_source_name, data);

								dbinfo.setName(dbname + "");

								RepositoriesMeta repositories = new RepositoriesMeta();
								try {
									if (repositories.readData()) {
										DatabaseMeta previousMeta = repositories.searchDatabase(dbinfo.getName());
										if (previousMeta != null) {
											repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
										}
										repositories.addDatabase(dbinfo);
										repositories.writeData();
									}

								} catch (Exception e) {
									throw new OIUEException(StatusResult._conn_error, data,e);
								}
							}
							return rto;
						} else {
							throw new RuntimeException("service can not init！");
						}
					} finally {
						db.disconnect();
					}
				} else {
					throw new RuntimeException("parameters is error");
				}

			default:
				throw new OIUEException(StatusResult._mismatch_type, null);
			}
	}

	@Override
	public Object delRepository(Map data, Map event, String tokenid) {
		RepositoriesMeta repositories = new RepositoriesMeta();
		try {
			if(repositories.readData()) {
				DatabaseMeta previousMeta = repositories.searchDatabase(MapUtil.getString(data, "id_database"));
				if(previousMeta != null) {
					repositories.removeDatabase(repositories.indexOfDatabase(previousMeta));
				}
				repositories.writeData();
			}
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		return iresource.callEvent("a9e58714-7765-4405-9c1a-a7f55a22c225", data_source_name, data);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object getEntity(Map data, Map event, String tokenid) {
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
					} catch (Exception e) {
						throw new OIUEException(StatusResult._conn_error, data,e);

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

					} catch (Exception e) {
						throw new OIUEException(StatusResult._conn_error, data,e);
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
					} catch (Exception e) {
						throw new OIUEException(StatusResult._conn_error, data,e);
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
					} catch (Exception e) {
						throw new OIUEException(StatusResult._conn_error, data,e);
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
					} catch (Exception e) {
						throw new OIUEException(StatusResult._conn_error, data,e);
					} finally {
						db.disconnect();
					}
				}
			} else {

				Database db = new Database( loggingObject, databaseMeta );
				try {
					db.connect();
				} catch (Exception e) {
					throw new OIUEException(StatusResult._conn_error, data,e);
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
	public Object getEntityColumns(Map data, Map event, String tokenid) {
		//		String databaseName = MapUtil.getString(data, "databaseName");
		String type = MapUtil.getString(data, "operation_type");
		if(StringUtil.isEmptys(type))
			type="csv";

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
			} catch (Exception e) {
				throw new OIUEException(StatusResult._conn_error, data,e);
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

			} catch (Exception e) {
				throw new OIUEException(StatusResult._conn_error, data,e);
			} finally {
				db.disconnect();
			}
			break;


		case "txt":
			try {

			} finally {
			}
			break;
		case "csv":
			try {
				Map upload_files = (Map) data.get("upload_file");
				if(upload_files.size()==1){
					String fpath = path +"/uploadfile/" +upload_files.values().toArray()[0];
					String charset = MapUtil.getString(data, "charset","UTF-8");
					CsvReader reader = new CsvReader(fpath, ',', Charset.forName(charset));
					reader.readHeaders();
					String[] headers = reader.getHeaders();

					for (int i = 0; i < headers.length; i++) {
						Map jsonObject = new HashMap();
						//				jsonObject.put("name", inf.quoteField(field.getName()));
						jsonObject.put("name",headers[i]);
						//						jsonObject.put("type", field.getTypeDesc());
						//						jsonObject.put("comments", field.getComments());
						//						jsonObject.put("length", field.getLength());
						jsonArray.add(jsonObject);
					}
					data.put("fields", jsonArray);
					return data;
				}
			} catch (Exception e) {
				throw new OIUEException(StatusResult._format_error, data,e);
			} finally {
			}
			break;
		case "excel":
			try {

			} finally {
			}
			break;

		default:
			break;
		}
		return jsonArray;
	}

	private Repository getRepository(String repositoryId) {
		Repository repository = null;
		return repository;
	}
	private DelegatingMetaStore getMetaStore(String metaStoreId) {
		DelegatingMetaStore metaStore = null;
		return metaStore;
	}

	@Override
	public Object setEntityColumns(Map data, Map event, String tokenid) {
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
						tf.add(localDatabaseMeta.quoteField(field.getName()));
					}
					if(tf.size()>0)
						data.put("sql", "select * from "+ table );
//						data.put("sql", "select "+ListUtil.ArrayJoin(tf.toArray(), ",") + " from "+ table );
				}
				insertAndCreateEntity(data,temp_fields);
				return insertEntitySource(data);
			} catch (Exception e) {
				throw new OIUEException(StatusResult._conn_error, data,e);
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
			} catch (Exception e) {
				throw new OIUEException(StatusResult._conn_error, data,e);
			} finally {
				db.disconnect();
			}
		case "create_entity_text":
		case "create_entity_execl":
		case "create_entity_csv":
			data.put("type", 100);
				insertAndCreateFileEntity(data);
				data.put("input_type", MapUtil.getString(data, "input_type","csv"));
				return insertEntitySource(data);

		default:
			throw new RuntimeException("operation_type error!");
		}
	}

	private void insertAndCreateEntity(Map data,Map<String,ValueMetaInterface> src_fields) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());

		List<Map> fields;
		fields = (List) data.get("fields");
		Database localdb = new Database( loggingObject, localDatabaseMeta );
		try {
			localdb.connect();
			String n_table_name = "t_"+UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			data.put("table_type","table");
			data.put("table_name",n_table_name);
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("92363b9e-e4b8-4e7e-bba2-78e53a10f313", data_source_name, data);//insert entity

			RowMetaInterface rmi=localdb.getQueryFieldsFromPreparedStatement("select");
			int i =0;
			for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
				ValueMetaInterface tf = src_fields.get(MapUtil.getString(field, "name"));
				String n_field_name = "f_"+UUID.randomUUID().toString().replace("-", "");
				field.put("entity_column_id", n_field_name);
				field.put("column_name", n_field_name);
				field.put("entity_id", n_table_name);
				field.put("type", tf.getType()+"");
				field.put("precision", tf.getLength());
				field.put("scale", tf.getPrecision());
				field.put("sort", i++);
				field.put("null_able", 1);
				field.put("user_id", data.get("user_id"));
				field.put("primary_key",(boolean) field.get("ispk")?1:0);
				tf.setName(n_field_name);
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("8e656a8f-5864-4583-8a5c-99a4963c4250", data_source_name, field);//insert entity column
				rmi.addValueMeta(tf);
			}
			data.put("relation", JSONUtil.parserToStr(fields));
			localdb.execStatement(localdb.getDDL(n_table_name, rmi));
			insertServiceEvent(data, null, null);
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}finally {
			localdb.disconnect();
		}
	}
	private void insertAndCreateFileEntity(Map data) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());

		List<Map> fields;
		fields = (List) data.get("fields");
		Database localdb = new Database( loggingObject, localDatabaseMeta );
		try {
			localdb.connect();
			String n_table_name = "t_"+UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			data.put("table_type","table");
			data.put("table_name", n_table_name);
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("92363b9e-e4b8-4e7e-bba2-78e53a10f313", data_source_name, data);//insert entity

			RowMetaInterface rmi=localdb.getQueryFieldsFromPreparedStatement("select");
			int i =0;
			for (Map field : fields) {//type,entity_id,entity_column_id,entity_column_id,field_desc,field_desc,precision,scale,ispk,sort,user_id
				String n_field_name = "f_"+UUID.randomUUID().toString().replace("-", "");
				field.put("entity_column_id", n_field_name);
				field.put("entity_id", n_table_name);
				field.put("column_name", n_field_name);
				field.put("sort", i++);
				field.put("null_able", 1);
				field.put("scale", MapUtil.getInt(field, "precision"));
				field.put("precision", MapUtil.getInt(field, "length"));
				field.put("type", MapUtil.getString(field, "type"));
				field.put("user_id", data.get("user_id"));
				field.put("primary_key",(boolean) field.get("ispk")?1:0);
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("8e656a8f-5864-4583-8a5c-99a4963c4250", data_source_name, field);//insert entity column
				ValueMetaInterface tf = new ValueMetaBase(n_field_name,MapUtil.getInt(field, "type"),MapUtil.getInt(field, "precision"),MapUtil.getInt(field, "scale"));
				rmi.addValueMeta(tf);
			}
			data.put("relation", JSONUtil.parserToStr(fields));
			data.put("id_database", MapUtil.getString(data, "id_database","local"));
			localdb.execStatement(localdb.getDDL(n_table_name, rmi));
			insertServiceEvent(data, null, null);
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}finally {
			localdb.disconnect();
		}
	}
	private void insertAndCreateView(Map data) {
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		String relation_service_event_id = MapUtil.getString(data, "relation_service_event_id");
		String service_event_id = MapUtil.getString(data, "service_event_id");
		
		Map relation_entity = null;
		String relation_entity_name = null;
		if (!StringUtil.isEmptys(relation_service_event_id)) {
			Map<String,Object> dp = new HashMap<>();
			dp.put("service_event_id", relation_service_event_id);
			iresource = factoryService.getBmo(IResource.class.getName());// select relation entity
			relation_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
			relation_entity_name = MapUtil.getString(relation_entity, "name");
		}
		
		Map source_entity = null;
		String source_entity_name = null;
		if (!StringUtil.isEmptys(service_event_id)) {
			Map<String,Object> dp = new HashMap<>();
			dp.put("service_event_id", service_event_id);
			iresource = factoryService.getBmo(IResource.class.getName());// select relation entity
			source_entity = (Map) iresource.callEvent("8d2cc15f-9e0c-4d5e-8208-56727863a5d3", data_source_name, dp);
			source_entity_name = MapUtil.getString(source_entity, "name");
		}
		
		List<Map> fields;
		fields = (List) data.get("fields");
		Database localdb = new Database( loggingObject, localDatabaseMeta );
		try {
			localdb.connect();
			String n_table_name = "v_"+UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			data.put("table_type","view");
			data.put("table_name",n_table_name);
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("92363b9e-e4b8-4e7e-bba2-78e53a10f313", data_source_name, data);//insert entity
			
			List<String> rtnField = new ArrayList<>();
			List<String> whereStr = new ArrayList<>();
			int i =0;
			for (Map<String, Object> field : fields) {//entity_id,entity_column_id,entity_column_id,field_desc,primary_key,sort,user_id,o_entity_column_id

				String old_field_name = MapUtil.getString(field, "name");
				String n_field_name = "f_"+UUID.randomUUID().toString().replace("-", "");
				field.put("entity_id", n_table_name);
				
				field.put("o_entity_column_id", old_field_name);
				field.put("entity_column_id", n_field_name);
				field.put("sort", i++);
				field.put("user_id", data.get("user_id"));
				field.put("primary_key",(boolean) field.get("ispk")?1:0);
				iresource=factoryService.getBmo(IResource.class.getName());
				Map<String, Object> rf = (Map) iresource.callEvent("478925a7-3ce3-42dc-ae11-653e61a2a14c", data_source_name, field);//insert entity column
				
				rf=((List<Map>)rf.get("root")).get(0);
				String fname = MapUtil.getString(rf,"remark",old_field_name);
				String data_type_id = MapUtil.getString(rf,"data_type_id");
				if("postgres_point".equals(data_type_id)){
					data.put("geo_type", 10);					
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
				}else if("postgres_line".equals(data_type_id)){
					data.put("geo_type", 20);					
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
				}else if("postgres_polygon".equals(data_type_id)){
					data.put("geo_type", 30);					
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("b22417ae-2e0e-42ba-98fe-b3af0fde1c6b", data_source_name, data);
				}
				MapUtil.mergeDifference(field,rf);
				String relation_entity_column_id =  MapUtil.getString(field, "relation_entity_column_id");
				
				if(!StringUtil.isEmptys(relation_entity_column_id)&&relation_entity!=null){
					Map<String, Object> dp = new HashMap<>();
					dp.put("entity_column_id", relation_entity_column_id);
					iresource = factoryService.getBmo(IResource.class.getName());// query entity column
					dp = (Map<String, Object>) iresource.callEvent("04ebc4b3-7368-4b20-a8fe-7c6613742c27", data_source_name, dp);
					whereStr.add("r."+fname+" = l."+ MapUtil.getString(dp,"name",relation_entity_column_id));
					rtnField.add("l."+ MapUtil.getString(dp,"name",relation_entity_column_id) +" as "+n_field_name);
				}else{
					rtnField.add(fname+" as "+n_field_name);
				}
				
			}
			data.put("relation", JSONUtil.parserToStr(fields));
			
			StringBuffer sql = new StringBuffer("select ").append(ListUtil.ListJoin(rtnField, ",")).append(" from ");
			if(relation_entity_name!=null){
				sql.append(relation_entity_name).append(" as l left join ").append(source_entity_name).append(" as r on ").append(ListUtil.ListJoin(whereStr, " and "));
			}else{
				sql.append(source_entity_name);
			}
			String createView = null;
			EventConvertService convert = null;
			try {
				convert = factoryService.getDmo(EventConvertService.class.getName(), MapUtil.getString(source_entity, "dbtype"));
				Map event = new HashMap<>();
				event.put("RULE", "_intelligent");
				event.put("CONTENT", sql.toString());
				event.put("EVENT_TYPE", "select");
				List<Map<?, ?>> events = convert.convert(event, data);
				convert.setConn(localdb.getConnection());
				createView = events.get(0).get(EventField.content)+"";
				PreparedStatement pstmt =convert.getConn().prepareStatement(createView);
				convert.getIdmo().setPstmt(pstmt);
				List pers = (List) events.get(0).get(EventField.contentList);
				convert.getIdmo().setQueryParams(pers);
				
				createView="CREATE OR REPLACE VIEW "+n_table_name+" as "+pstmt.toString();
			} finally {
				if(convert!=null)
					convert.close();
				localdb.disconnect();
				localdb.connect();
			}
			localdb.execStatement(createView);
			insertServiceEvent(data, null, null);
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}finally {
			localdb.disconnect();
		}
	}
	//table,entity_desc
	@Override
	public void readAndInsertEntiry(Map data, Map event, String tokenid){

		Database db;
		List columns = new ArrayList<>();
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		String schema = MapUtil.getString(data, "schema");
		String table = MapUtil.getString(data, "table");

		if(schema==null)
			schema="";

		db = new Database( loggingObject, localDatabaseMeta );
		try {
			db.connect();
			String n_table_name = "t_"+UUID.randomUUID().toString().replace("-", "");
			data.put("entity_id", n_table_name);
			data.put("table_type","systemtable");
			data.put("table_name", table);
			data.put("entity_desc", MapUtil.getString(data, "entity_desc",table));
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("92363b9e-e4b8-4e7e-bba2-78e53a10f313", data_source_name, data);//insert entity

			String schemaTable = localDatabaseMeta.getQuotedSchemaTableCombination(schema,table);
			RowMetaInterface fields = db.getTableFields(schemaTable);

			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					ValueMetaInterface field = fields.getValueMeta(i);
					Map column = new HashMap();
					String n_field_name = "f_"+UUID.randomUUID().toString().replace("-", "");
					column.put("entity_column_id", n_field_name);
					column.put("column_name", field.getName());
					column.put("name", field.getName());
					column.put("alias", field.getName());
					column.put("entity_id", n_table_name);
					column.put("type", field.getType()+"");
					column.put("field_desc", field.getComments());
					column.put("precision", field.getLength());
					column.put("scale", field.getPrecision());
					column.put("sort", i);
					column.put("user_id", data.get("user_id"));
					column.put("ispk",false);
					column.put("null_able",1);
					column.put("primary_key",(boolean) column.get("ispk")?1:0);
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("8e656a8f-5864-4583-8a5c-99a4963c4250", data_source_name, column);//insert entity column
					columns.add(column);
				}
			}
			data.put("fields",columns);
			insertServiceEvent(data, null, null);

		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		} finally {
			db.disconnect();
		}
	}

	//{entity_id,user_id,fields:{precision,scale,name,alias,entity_column_id,type,ispk},entity_desc,query,expression_query,insert,expression_insert,update,expression_update,delete,expression_delete}
	private void insertServiceEvent(Map data, Map event, String tokenid){
		IResource iresource;

		String entity_id =  MapUtil.getString(data, "entity_id");
		String table_name =  MapUtil.getString(data, "table_name");
		String user_id =  MapUtil.getString(data, "user_id");

		List<Map> fields = (List) data.get("fields");
		List<String> allFields = new ArrayList<>();
		List<String> allPkFields = new ArrayList<>();
		List<String> otherFields = new ArrayList<>();
		List<String> allFieldVs = new ArrayList<>();
		for (Map field : fields) {
			String name  = MapUtil.getString(field, "column_name");
			name = localDatabaseMeta.quoteField(name);
			allFields.add(name);
			allFieldVs.add("?");

			if ((boolean) field.get("ispk")) {
				allPkFields.add(name);
			} else {
				otherFields.add(name);
			}
		}

		String allFieldstr = ListUtil.ListJoin(allFields, ",");
		String allPkFieldstr = ListUtil.ListJoin(allPkFields, ",");
		String allPkFieldstrv = ListUtil.ListJoin(allPkFields, "= ? ,")+"=? ";

		//service_id ,name ,description ,type ,content ,expression,user_id
		Map selectMap = new HashMap<>();
		selectMap.put("service_id", "fm_system_service_execute");
		selectMap.put("user_id", user_id);
		selectMap.put("name",entity_id+"_select");
		selectMap.put("description",MapUtil.getString(data, "entity_desc",entity_id));
		selectMap.put("type","query");
		selectMap.put("rule", "intelligent");
		selectMap.put("expression",MapUtil.getString(data, "expression_query","{\"conjunction\":\"and\",\"filters\":[]}"));
		selectMap.put("content",MapUtil.getString(data, "query","select * from "+ table_name));
		iresource=factoryService.getBmo(IResource.class.getName());
		Object selecto = iresource.callEvent("fm_system_add_services_event", data_source_name, selectMap);//insert service event query

		//service_event_parameters_id,entity_id,service_event_id
		Map eventEntity = new HashMap<>();
		eventEntity.put("service_event_parameters_id", ((Map)selecto).get("service_event_id"));
		eventEntity.put("entity_id", entity_id);
		eventEntity.put("service_event_id",((Map)selecto).get("service_event_id"));
		eventEntity.put("operation_type", "query");
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("38b6c070-0133-470f-ad60-7344b31a1f34", data_source_name, eventEntity);//insert service event entity

		Map insertMap = new HashMap<>();
		insertMap.put("service_id", "fm_system_service_execute");
		insertMap.put("user_id", user_id);
		insertMap.put("description",MapUtil.getString(data, "entity_desc",entity_id)+"_insert");
		insertMap.put("name",entity_id+"_insert");
		insertMap.put("type","insert");
		insertMap.put("rule", "");
		insertMap.put("content",MapUtil.getString(data, "insert","insert into " + table_name +"("+allFieldstr+") values("+ListUtil.ListJoin(allFieldVs, ",")+")"+" returning *"));
		insertMap.put("expression",MapUtil.getString(data, "expression_insert",allFieldstr));
		iresource=factoryService.getBmo(IResource.class.getName());
		Object inserto = iresource.callEvent("fm_system_add_services_event", data_source_name, insertMap);//insert service event insert

		eventEntity.put("service_event_parameters_id", ((Map)inserto).get("service_event_id"));
		eventEntity.put("service_event_id",((Map)inserto).get("service_event_id"));
		eventEntity.put("operation_type", "insert");
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("38b6c070-0133-470f-ad60-7344b31a1f34", data_source_name, eventEntity);//insert service event entity

		Map deleteMap = new HashMap<>();
		deleteMap.put("service_id", "fm_system_service_execute");
		deleteMap.put("user_id", user_id);
		deleteMap.put("description",MapUtil.getString(data, "entity_desc",entity_id)+"_delete");
		deleteMap.put("name",entity_id+"_delete");
		deleteMap.put("type","delete");
		deleteMap.put("rule", "");
		deleteMap.put("content",MapUtil.getString(data, "delete","delete from "+table_name + " where "+allPkFieldstrv+" returning *"));
		deleteMap.put("expression",MapUtil.getString(data, "expression_delete",allPkFieldstr));
		iresource=factoryService.getBmo(IResource.class.getName());
		Object deleteo = iresource.callEvent("fm_system_add_services_event", data_source_name, deleteMap);//insert service event delete

		eventEntity.put("service_event_parameters_id", ((Map)deleteo).get("service_event_id"));
		eventEntity.put("service_event_id",((Map)deleteo).get("service_event_id"));
		eventEntity.put("operation_type", "delete");
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("38b6c070-0133-470f-ad60-7344b31a1f34", data_source_name, eventEntity);//insert service event entity

		Map updateMap = new HashMap<>();
		updateMap.put("service_id", "fm_system_service_execute");
		updateMap.put("user_id", user_id);
		updateMap.put("description",MapUtil.getString(data, "entity_desc",entity_id)+"_update");
		updateMap.put("name",entity_id+"_update");
		updateMap.put("type","update");
		updateMap.put("rule", "");
		updateMap.put("content",MapUtil.getString(data, "update","update "+table_name +" set "+ ListUtil.ListJoin(otherFields, "= ? ,")+"=?"+" where "+allPkFieldstrv+" returning *"));
		updateMap.put("expression",MapUtil.getString(data, "expression_update",ListUtil.ListJoin(otherFields, ",")+","+allPkFieldstr));
		iresource=factoryService.getBmo(IResource.class.getName());
		Object updateo = iresource.callEvent("fm_system_add_services_event", data_source_name, updateMap);//insert service event update

		eventEntity.put("service_event_parameters_id", ((Map)updateo).get("service_event_id"));
		eventEntity.put("service_event_id",((Map)updateo).get("service_event_id"));
		eventEntity.put("operation_type", "update");
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("38b6c070-0133-470f-ad60-7344b31a1f34", data_source_name, eventEntity);//insert service event entity

		//service_event_id,entity_id,alias,entity_column_id,desc,data_type_id,precision,scale,sort,config_type,user_id,service_event_parameters_id,component_instance_id
		Map insertConfigMap = new HashMap<>();
		insertConfigMap.put("user_id", user_id);
		insertConfigMap.put("entity_id", entity_id);
		int sort = 1;
		boolean addOperstion = true;
		for (Map field : fields) {//precision,scale,name,alias,entity_column_id,type,ispk
			insertConfigMap.put("precision", field.get("precision"));
			insertConfigMap.put("scale", field.get("scale"));
			insertConfigMap.put("alias",MapUtil.getString(field, "alias", field.get("entity_column_id")+""));
			insertConfigMap.put("entity_column_id", field.get("entity_column_id"));
			insertConfigMap.put("desc", MapUtil.getString(field, "desc", field.get("name")+""));
			insertConfigMap.put("data_type_id", MapUtil.getString(field, "data_type_id", field.get("type")+""));
			insertConfigMap.put("sort", sort++);
			insertConfigMap.put("null_able", field.get("null_able"));

			insertConfigMap.put("component_instance_id", null);
			insertConfigMap.put("config_type", "insert");
			//			insertConfigMap.put("service_event_id", ((Map)inserto).get("service_event_id"));
			//			insertConfigMap.put("service_event_parameters_id",  ((Map)inserto).get("service_event_id"));
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config insert

			if ((boolean) field.get("ispk")) {
				if(addOperstion){
					addOperstion=false;
					insertConfigMap.put("desc", "添加");
					insertConfigMap.put("component_instance_id", "fm_lt_operation_insert");
					insertConfigMap.put("config_type", "operation");
					insertConfigMap.put("service_event_id", ((Map)inserto).get("service_event_id"));
					insertConfigMap.put("service_event_parameters_id",  ((Map)inserto).get("service_event_id"));
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config insert

					insertConfigMap.put("desc", "删除");
					insertConfigMap.put("component_instance_id", "fm_lt_operation_delete");
					insertConfigMap.put("config_type", "operation");
					insertConfigMap.put("service_event_id", ((Map)deleteo).get("service_event_id"));
					insertConfigMap.put("service_event_parameters_id", ((Map)deleteo).get("service_event_id"));
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config delete

					insertConfigMap.put("desc", "修改");
					insertConfigMap.put("component_instance_id", "fm_lt_operation_update");
					insertConfigMap.put("config_type", "operation");
					insertConfigMap.put("service_event_id", ((Map)updateo).get("service_event_id"));
					insertConfigMap.put("service_event_parameters_id", ((Map)updateo).get("service_event_id"));
					iresource=factoryService.getBmo(IResource.class.getName());
					iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config update

					insertConfigMap.remove("service_event_id");
					insertConfigMap.remove("service_event_parameters_id");
				}

				insertConfigMap.put("desc", MapUtil.getString(field, "desc", field.get("name")+""));
				insertConfigMap.put("component_instance_id", null);
				insertConfigMap.put("config_type", "delete");
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config delete

				insertConfigMap.put("config_type", "updateKey");
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config update
			}else{
				insertConfigMap.put("config_type", "update");
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config update

				insertConfigMap.put("config_type", "result");
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config result

				insertConfigMap.put("config_type", "filter");
				iresource=factoryService.getBmo(IResource.class.getName());
				iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config filter
			}
		}
		if(addOperstion){
			addOperstion=false;
			insertConfigMap.put("desc", "添加");
			insertConfigMap.put("component_instance_id", "fm_lt_operation_insert");
			insertConfigMap.put("config_type", "operation");
			insertConfigMap.put("service_event_id", ((Map)inserto).get("service_event_id"));
			insertConfigMap.put("service_event_parameters_id",  ((Map)inserto).get("service_event_id"));
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config insert

			insertConfigMap.put("desc", "删除");
			insertConfigMap.put("component_instance_id", "fm_lt_operation_delete");
			insertConfigMap.put("config_type", "operation");
			insertConfigMap.put("service_event_id", ((Map)deleteo).get("service_event_id"));
			insertConfigMap.put("service_event_parameters_id", ((Map)deleteo).get("service_event_id"));
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config delete

			insertConfigMap.put("desc", "修改");
			insertConfigMap.put("component_instance_id", "fm_lt_operation_update");
			insertConfigMap.put("config_type", "operation");
			insertConfigMap.put("service_event_id", ((Map)updateo).get("service_event_id"));
			insertConfigMap.put("service_event_parameters_id", ((Map)updateo).get("service_event_id"));
			iresource=factoryService.getBmo(IResource.class.getName());
			iresource.callEvent("e535fb44-4d1a-46f9-907f-9aa931c8502f", data_source_name, insertConfigMap);//insert service event config update
		}

	}
	//{entity_id,user_id,tablename,entity_desc,insert,expression_insert,update,expression_update}
	private void updateServiceEvent(Map data, Map event, String tokenid){
		IResource iresource;

		iresource=factoryService.getBmo(IResource.class.getName());
		if(!data.containsKey("entity_column_id"))
			data.put("entity_column_id", MapUtil.getString(data, "x"));
		Object entity = iresource.callEvent("fa8f9b71-34ca-4d40-8b74-a03cf4c1f3d5", data_source_name, data);//insert service event query
		if(entity==null||!(entity instanceof Map)){
			throw new RuntimeException();
		}
		Map rentity = (Map) entity;
		String entity_id =  MapUtil.getString(rentity, "entity_id");
		String table_name =  MapUtil.getString(rentity, "tablename");
		String user_id =  MapUtil.getString(data, "user_id");

		iresource=factoryService.getBmo(IResource.class.getName());
		List<Map> fields = (List<Map>) iresource.callEvent("c51a1f14-0d47-4a64-b476-2fb1286b4d2a", data_source_name, rentity);//insert service event query
		List<String> allInsertFields = new ArrayList<>();
		List<String> allSelectFields = new ArrayList<>();
		List<String> allPkFields = new ArrayList<>();
		List<String> otherFields = new ArrayList<>();
		List<String> allFieldVs = new ArrayList<>();
		List<String> geoFields = new ArrayList<>();
		for (Map field : fields) {
			String name  = MapUtil.getString(field, "name");
			name = localDatabaseMeta.quoteField(name);
			String data_type_id  = MapUtil.getString(field, "data_type_id");
			allInsertFields.add(name);
			if("postgres_point".equals(data_type_id)||"postgres_line".equals(data_type_id)||"postgres_polygon".equals(data_type_id)||"postgres_geom".equals(data_type_id)){
				allFieldVs.add("ST_SetSRID(st_geomfromgeojson(?),4326)");
				allSelectFields.add("st_asgeojson("+name+") as "+name);
			}else {
				allFieldVs.add("?");
				allSelectFields.add(name);
			}

			if (MapUtil.getInt(field,"primary_key")==1) {
				allPkFields.add(name);
			} else if("postgres_point".equals(data_type_id)||"postgres_line".equals(data_type_id)||"postgres_polygon".equals(data_type_id)||"postgres_geom".equals(data_type_id)){
				geoFields.add(name);
			}else{
				otherFields.add(name);
			}
		}

		String allInsertFieldstr = ListUtil.ListJoin(allInsertFields, ",");
//		String allSelectFieldstr = ListUtil.ListJoin(allSelectFields, ",");
		String allPkFieldstr = ListUtil.ListJoin(allPkFields, ",");
		String allPkFieldstrv = ListUtil.ListJoin(allPkFields, "= ? ,")+"=? ";

		//service_id ,name ,description ,type ,content ,expression,user_id
//		Map selectMap = new HashMap<>();
//		selectMap.put("entity_id", entity_id);
//		selectMap.put("user_id", user_id);
//		selectMap.put("description",MapUtil.getString(data, "entity_desc",entity_id));
//		selectMap.put("operation_type","query");
//		selectMap.put("rule", "intelligent");
//		selectMap.put("content",MapUtil.getString(data, "query","select "+allSelectFieldstr+" from "+ table_name));
//		selectMap.put("expression",MapUtil.getString(data, "expression_query","{\"conjunction\":\"and\",\"filters\":[]}"));
//		iresource=factoryService.getBmo(IResource.class.getName());
//		iresource.callEvent("2cea7527-2e31-4e98-9d2e-6feb1c8f15b0", data_source_name, selectMap);//update service event query

		Map insertMap = new HashMap<>();
		insertMap.put("entity_id", entity_id);
		insertMap.put("user_id", user_id);
		insertMap.put("operation_type","insert");
		insertMap.put("content",MapUtil.getString(data, "insert","insert into " + table_name +"("+allInsertFieldstr+") values("+ListUtil.ListJoin(allFieldVs, ",")+")"+" returning *"));
		insertMap.put("expression",MapUtil.getString(data, "expression_insert",allInsertFieldstr));
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("2cea7527-2e31-4e98-9d2e-6feb1c8f15b0", data_source_name, insertMap);//update service event insert

		Map updateMap = new HashMap<>();
		updateMap.put("entity_id", entity_id);
		updateMap.put("user_id", user_id);
		updateMap.put("operation_type","update");
		updateMap.put("content",MapUtil.getString(data, "update","update "+table_name +" set "+ ListUtil.ListJoin(otherFields, "= ? ,")+"=? ,"+ ListUtil.ListJoin(geoFields, "= ST_SetSRID(st_geomfromgeojson(?),4326) ,")+"= ST_SetSRID(st_geomfromgeojson(?),4326)  where "+allPkFieldstrv+" returning *"));
		updateMap.put("expression",MapUtil.getString(data, "expression_update",ListUtil.ListJoin(otherFields, ",")+","+ListUtil.ListJoin(geoFields, ",")+","+allPkFieldstr));
		iresource=factoryService.getBmo(IResource.class.getName());
		iresource.callEvent("2cea7527-2e31-4e98-9d2e-6feb1c8f15b0", data_source_name, updateMap);//update service event update
	}

	private Object insertEntitySource(Map data){
		IResource iresource=factoryService.getBmo(IResource.class.getName());
		Object ro = iresource.callEvent("46efdb82-eec3-464f-93f7-8a21afa9886e", data_source_name, data);//insert entity source

		try {
			RepositoryDirectoryInterface directory = null;
			try {
				directory = repository.findDirectory(MapUtil.getString(data, "user_id"));
			} catch (Exception e) {
				try {
					directory = repository.createRepositoryDirectory(repository.getUserHomeDirectory(), MapUtil.getString(data, "user_id"));
				} catch (Exception e2) {}
			}
			if(directory == null)
				try {
					directory = repository.getUserHomeDirectory();
					
				} catch (Exception e) {
					throw new OIUEException(StatusResult._data_error, data,e);
				}
			
			String transName = MapUtil.getString(data, "transName");
			if(StringUtil.isEmptys(transName)){
				if(ro instanceof Map)
					transName = MapUtil.get((Map) ro, "root.0.entity_source_id")+"";
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
				versionComment = "add: " + new Date();
			} else {
				versionComment = "add: " + new Date();
			}
			
			transMeta.importFromMetaStore();
			
			//		RepositoriesMeta repositories = new RepositoriesMeta();
			//		repositories.readData();
			//		DatabaseMeta previousMeta = repositories.searchDatabase(MapUtil.getString(data, "id_database"));
			Map previousdata = new HashMap<>();
			previousdata.put("id_database", MapUtil.getString(data, "id_database"));
			iresource=factoryService.getBmo(IResource.class.getName());
			DatabaseMeta previousMeta = DatabaseCodec.decode((Map) iresource.callEvent("148bfb77-35ae-408f-915e-291c7a83f279", data_source_name, previousdata));//query local connection config
			
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
				repository.save( transMeta, versionComment, null);
				existingId = repository.getTransformationID( transMeta.getName(), transMeta.getRepositoryDirectory() );
			}
			List<Map> fields;
			fields = (List) data.get("fields");
			
			//in
			StepMeta inStepMeta = InputStepMetaManger.ConvertToStepMeta(data,transMeta);
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
			PluginRegistry registry = PluginRegistry.getInstance();
			PluginInterface sp = registry.findPluginWithId( StepPluginType.class, outstepid );
			StepMetaInterface stepMetaInterface = (StepMetaInterface) registry.loadClass( sp );
			TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMetaInterface;
			
			//		tableOutputMeta.setDatabaseMeta(DatabaseMeta.findDatabase(transMeta.getDatabases(),MapUtil.getString(data, "id_database")));
			tableOutputMeta.setDatabaseMeta(localDatabaseMeta);
			tableOutputMeta.setSchemaName("public");
			tableOutputMeta.setTableName(outstepname);
			
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
				if (stepNames[j].equalsIgnoreCase(inStepMeta.getName()))
					hopinf.setFromStep(transMeta.getStep(j));
				if (stepNames[j].equalsIgnoreCase(outStepMeta.getName()))
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
			
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}
	}

	@Override
	public Object setEntityRelation(Map data, Map event, String tokenid) {
		String type = MapUtil.getString(data, "operation_type");
		List<Map> fields = (List) data.get("fields");
		switch (type) {
		case "entity_table":
			data.put("type", 1);
			String table = MapUtil.getString(data, "table");
			if(fields!=null){
				List tf = new ArrayList<>();
				for (Map field : fields) {
					tf.add(localDatabaseMeta.quoteField(MapUtil.getString(field, "name")));
				}
				if(tf.size()>0)
					data.put("sql", "select "+ListUtil.ArrayJoin(tf.toArray(), ",") + " from "+ table );
			}
			data.put("transName", "transformation_"+table+"_to_"+MapUtil.getString(data, "entity_id"));
			break;
		case "entity_sql":
			data.put("type", 100);
			break;
		case "entity_csv":
			data.put("type", 100);
			break;

		default:
			break;
		}
		data.put("relation", JSONUtil.parserToStr(fields));
		return insertEntitySource(data);

	}

	@Override
	public Object savaTrans(Map data, Map event, String tokenid) {
		return null;
	}
	@Override
	public Object runTrans(Map data, Map event, String tokenid) {
		return null;
	}
	@Override
	public Object initRun(Map data, Map event, String tokenid) {
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

	private AbstractMeta getTrans(String transId) {
		TransMeta transMeta;
		try {
			transMeta = new TransMeta(path+transId+".ktr");
			return transMeta;
		} catch (KettleXMLException | KettleMissingPluginsException e) {
			throw new OIUEException(StatusResult._conn_error, transId,e);
		}
	}

	private static HashMap<String, TransExecutor> executions = new HashMap<String, TransExecutor>();

	@Override
	public Object result(Map data, Map event, String tokenid){
		String executionId = MapUtil.getString(data, "execution_id");
		Map jsonObject = new HashMap<>();

		TransExecutor transExecutor = executions.get(executionId);

		try {
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
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, data,e);
		}
	}
	@Override
	public Object convertToGeometry(Map data, Map event, String tokenid) {
		String type = MapUtil.getString(data, "operation_type");
		IResource iresource;
		int geo_type = 100;
		switch (type) {
		case "changeToGeometry":
			try {
				geo_type = MapUtil.getInt(data, "geo_type");
			} catch (Exception e) {
				data.put("geo_type", geo_type);
			}
			iresource=factoryService.getBmo(IResource.class.getName());
			Object ro = iresource.callEvent("c901e524-4c44-4525-ab87-5ef0328261bb", data_source_name, data);
			if(ro instanceof Map){
				int status = MapUtil.getInt((Map<String, Object>) ro, "status");
				if(status<StatusResult._ncriticalAbnormal){
					return ro;
				}
			}
			updateServiceEvent(data,null,tokenid);
			return ro;

		case "createCeometry":
			try {
				geo_type = MapUtil.getInt(data, "geo_type");
			} catch (Exception e) {
				data.put("geo_type", geo_type);
			}
			if(geo_type!=10){
				throw new RuntimeException("目前暂只支持转点空间！");
			}
			iresource=factoryService.getBmo(IResource.class.getName());
			Object roa = iresource.callEvent("bc13c8d3-29af-4420-b5bb-00f007a7ccd5", data_source_name, data);
			updateServiceEvent(data,null,tokenid);
			return roa;

		case "convertToGeometry":

			break;

		default:
			break;
		}

		return null;
	}

	public Object split(Map data, Map event, String tokenid)throws Throwable{
//		String event_id = MapUtil.getString(data, "service_event_id");
//		IResource iresource;
//		iresource=factoryService.getBmo(IResource.class.getName());
//		Map roa = iresource.getEventByIDType(event_id, null);
//		String DBType=MapUtil.getString(roa, EventField.event_dbtype);
//
//		EventConvertService convert = factoryService.getDmo(EventConvertService.class.getName(), DBType);
//		List<Map<?, ?>> events = convert.convert(roa, data);
//		logger.debug("events:"+events+"|event:"+event+"|data:"+data);
//		if (events == null||events.size()==0){
//			throw new RuntimeException("event can not found ！event:"+event+"|data:"+data);
//		}
//		if (events.size() == 1) {
//			Map event_t =events.get(0);
//			if(event_t==null||event_t.get(EventField.event_type)==null)
//				throw new RuntimeException("event error ！events:"+events+"|event:"+event+"|data:"+data);
//
//		}
		insertAndCreateView(data);
		return null;
	}
	public Object unite(Map data, Map event, String tokenid)throws Throwable{
		insertAndCreateView(data);
		return null;
	}
}
