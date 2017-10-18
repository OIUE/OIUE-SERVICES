package org.oiue.service.event.etl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseConnectionPoolParameter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.core.database.MSSQLServerNativeDatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;

public class DatabaseCodec {

	public static Map encode(DatabaseMeta databaseMeta) {
		Map jsonObject = new HashMap<>();

		jsonObject.put("name", databaseMeta.getDisplayName());
		jsonObject.put("type", databaseMeta.getPluginId());
		jsonObject.put("access", databaseMeta.getAccessType());

		jsonObject.put("hostname", databaseMeta.getHostname());
		jsonObject.put("databaseName", databaseMeta.getDatabaseName());
		jsonObject.put("username", databaseMeta.getUsername());
		jsonObject.put("password", Encr.decryptPasswordOptionallyEncrypted(databaseMeta.getPassword()));
		if(databaseMeta.isStreamingResults())
			jsonObject.put("streamingResults", databaseMeta.isStreamingResults());
		jsonObject.put("dataTablespace", databaseMeta.getDataTablespace());
		jsonObject.put("indexTablespace", databaseMeta.getIndexTablespace());
		if(databaseMeta.getSQLServerInstance() != null)
			jsonObject.put("sqlServerInstance", databaseMeta.getSQLServerInstance());
		if(databaseMeta.isUsingDoubleDecimalAsSchemaTableSeparator())
			jsonObject.put("usingDoubleDecimalAsSchemaTableSeparator", databaseMeta.isUsingDoubleDecimalAsSchemaTableSeparator());
		//		jsonObject.put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, databaseMeta.getAttributes().getProperty( SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE ));
		//		jsonObject.put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, databaseMeta.getAttributes().getProperty( SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER ));
		//		jsonObject.put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, databaseMeta.getAttributes().getProperty( SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT ));

		jsonObject.put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL ));
		jsonObject.put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS ));
		jsonObject.put("servername", databaseMeta.getServername());

		Object v = databaseMeta.getAttributes().get(MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY);
		if (v != null && v instanceof String) {
			String useIntegratedSecurity = (String) v;
			jsonObject.put(MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY, Boolean.parseBoolean(useIntegratedSecurity));
		} else {
			jsonObject.put(MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY, false);
		}

		jsonObject.put("port", databaseMeta.getDatabasePortNumberString());

		// Option parameters:

		Map<String, String> extraOptions = databaseMeta.getExtraOptions();
		List options = new ArrayList();
		if (extraOptions != null) {
			Iterator<String> keys = extraOptions.keySet().iterator();
			String currentType = databaseMeta.getPluginId();
			while (keys.hasNext()) {

				String parameter = keys.next();
				String value = extraOptions.get(parameter);
				if (StringUtil.isEmptys(value)) {
					value = "";
				}

				int dotIndex = parameter.indexOf('.');
				if (dotIndex >= 0) {
					String parameterOption = parameter.substring(dotIndex + 1);
					String databaseType = parameter.substring(0, dotIndex);
					if (currentType != null && currentType.equals(databaseType)) {
						Map jsonObject2 = new HashMap();
						jsonObject2.put("name", parameterOption);
						jsonObject2.put("value", value);
						options.add(jsonObject2);
					}
				}
			}
		}
		jsonObject.put("extraOptions", options);

		// Advanced panel settings:
		jsonObject.put("supportBooleanDataType", databaseMeta.supportsBooleanDataType());
		jsonObject.put("supportTimestampDataType", databaseMeta.supportsTimestampDataType());
		jsonObject.put("quoteIdentifiersCheck", databaseMeta.isQuoteAllFields());
		jsonObject.put("lowerCaseIdentifiersCheck", databaseMeta.isForcingIdentifiersToLowerCase());
		jsonObject.put("upperCaseIdentifiersCheck", databaseMeta.isForcingIdentifiersToUpperCase());
		jsonObject.put("preserveReservedCaseCheck", databaseMeta.preserveReservedCase());
		jsonObject.put("preferredSchemaName", databaseMeta.getPreferredSchemaName());
		jsonObject.put("connectSQL", databaseMeta.getConnectSQL());

		// Cluster panel settings
		jsonObject.put("partitioned", databaseMeta.isPartitioned() ? "Y" : "N");

		List partitionInfo = new ArrayList();
		PartitionDatabaseMeta[] clusterInformation = databaseMeta.getPartitioningInformation();
		if(clusterInformation != null) {
			for ( int i = 0; i < clusterInformation.length; i++ ) {
				PartitionDatabaseMeta meta = clusterInformation[i];
				Map jsonObject2 = new HashMap();
				jsonObject2.put("partitionId", meta.getPartitionId());
				jsonObject2.put("hostname", meta.getHostname());
				jsonObject2.put("port", meta.getPort());
				jsonObject2.put("databaseName", meta.getDatabaseName());
				jsonObject2.put("username", meta.getUsername());
				jsonObject2.put("password", meta.getPassword());

				partitionInfo.add(jsonObject2);
			}
		}
		jsonObject.put("partitionInfo", partitionInfo);

		// Pooling panel settings
		jsonObject.put("usingConnectionPool", databaseMeta.isUsingConnectionPool() ? "Y" : "N");
		jsonObject.put("initialPoolSize", databaseMeta.getInitialPoolSize());
		jsonObject.put("maximumPoolSize", databaseMeta.getMaximumPoolSize());
		Properties properties = databaseMeta.getConnectionPoolingProperties();
		List jsonArray2 = new ArrayList();
		for (DatabaseConnectionPoolParameter parameter : BaseDatabaseMeta.poolingParameters) {
			Map jsonObject2 = new HashMap();
			jsonObject2.put("enabled", properties.containsKey(parameter.getParameter()));
			jsonObject2.put("name", parameter.getParameter());
			jsonObject2.put("defValue", parameter.getDefaultValue());
			jsonObject2.put("description", parameter.getDescription());
			jsonArray2.add(jsonObject2);
		}

		jsonObject.put("pool_params", jsonArray2);
		jsonObject.put("read_only", databaseMeta.isReadOnly());

		return jsonObject;
	}

	public static DatabaseMeta decode(Map jsonObject) throws KettleDatabaseException {
		String name=MapUtil.getString(jsonObject,"name");
		String type=MapUtil.getString(jsonObject,"type");
		String access=MapUtil.getString(jsonObject,"access");
		String host=MapUtil.getString(jsonObject,"hostname");
		String db=MapUtil.getString(jsonObject,"databasename");
		String port=MapUtil.getString(jsonObject,"port");
		String user=MapUtil.getString(jsonObject,"username");
		String pass=MapUtil.getString(jsonObject,"password");

		DatabaseMeta databaseMeta = new DatabaseMeta(name,type,access,host,db,port,user,pass);
		databaseMeta.setDisplayName(databaseMeta.getName());

		if(jsonObject.containsKey("hostname"))
			databaseMeta.setHostname(MapUtil.getString(jsonObject,"hostname"));
		if(jsonObject.containsKey("databasename"))
			databaseMeta.setDBName(MapUtil.getString(jsonObject,"databasename"));
		if(jsonObject.containsKey("username"))
			databaseMeta.setUsername(MapUtil.getString(jsonObject,"username"));
		if(jsonObject.containsKey("password"))
			databaseMeta.setPassword(MapUtil.getString(jsonObject,"password"));
		if(jsonObject.containsKey("streamingResults"))	// infobright-jndi
			databaseMeta.setStreamingResults(true);
		if(jsonObject.containsKey("dataTablespace"))	//oracle-jndi
			databaseMeta.setDataTablespace(MapUtil.getString(jsonObject,"dataTablespace"));
		if(jsonObject.containsKey("indexTablespace"))	//oracle-jndi
			databaseMeta.setIndexTablespace(MapUtil.getString(jsonObject,"indexTablespace"));
		if(jsonObject.containsKey("sqlServerInstance"))		//mssql-native
			databaseMeta.setSQLServerInstance(MapUtil.getString(jsonObject,"sqlServerInstance"));
		if(jsonObject.containsKey("usingDoubleDecimalAsSchemaTableSeparator"))	//mssql-jndi
			databaseMeta.setUsingDoubleDecimalAsSchemaTableSeparator(MapUtil.getBoolean(jsonObject,"usingDoubleDecimalAsSchemaTableSeparator"));

		//		// SAP Attributes...
		//		if ( jsonObject.containsKey(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE) ) {
		//			databaseMeta.getAttributes().put( SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, jsonObject.optString(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE) );
		//		}
		//		if ( jsonObject.containsKey(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER) ) {
		//			databaseMeta.getAttributes().put( SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, jsonObject.optString(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER) );
		//		}
		//		if ( jsonObject.containsKey(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT) ) {
		//			databaseMeta.getAttributes().put( SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, jsonObject.optString(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT) );
		//		}

		// Generic settings...
		if ( jsonObject.containsKey(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL) ) {
			databaseMeta.getAttributes().put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, MapUtil.getString(jsonObject,GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL) );
		}
		if ( jsonObject.containsKey(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS) ) {
			databaseMeta.getAttributes().put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, MapUtil.getString(jsonObject,GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS) );
		}

		// Server Name: (Informix)
		if ( jsonObject.containsKey("servername") ) {
			databaseMeta.setServername(MapUtil.getString(jsonObject,"servername"));
		}

		// Microsoft SQL Server Use Integrated Security
		if ( jsonObject.containsKey(MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY) ) {
			boolean flag = MapUtil.getBoolean(jsonObject,MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY);
			if(flag) databaseMeta.getAttributes().put(MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY, flag);
		}

		if(jsonObject.containsKey("port"))
			databaseMeta.setDBPort(MapUtil.getString(jsonObject,"port"));

		// Option parameters:

		List options = (List) jsonObject.get("extraOptions");
		if(options != null) {
			for(int i=0; i<options.size(); i++) {
				Map jsonObject2 = (Map) options.get(i);
				String parameter = MapUtil.getString(jsonObject2,"name");
				String value = MapUtil.getString(jsonObject2,"value");

				if (value == null) {
					value = "";
				}

				if ((parameter != null) && (parameter.trim().length() > 0)) {
					if (value.trim().length() <= 0) {
						value = DatabaseMeta.EMPTY_OPTIONS_STRING;
					}

					databaseMeta.addExtraOption(databaseMeta.getPluginId(), parameter, value);
				}
			}
		}

		// Advanced panel settings:

		if ( jsonObject.containsKey("supportBooleanDataType") ) {
			databaseMeta.setSupportsBooleanDataType( MapUtil.getBoolean(jsonObject,"supportBooleanDataType") );
		}

		if ( jsonObject.containsKey("supportTimestampDataType") ) {
			databaseMeta.setSupportsTimestampDataType( MapUtil.getBoolean(jsonObject,"supportTimestampDataType") );
		}

		if ( jsonObject.containsKey("quoteIdentifiersCheck") ) {
			databaseMeta.setQuoteAllFields( MapUtil.getBoolean(jsonObject,"quoteIdentifiersCheck") );
		}

		if ( jsonObject.containsKey("lowerCaseIdentifiersCheck") ) {
			databaseMeta.setForcingIdentifiersToLowerCase( MapUtil.getBoolean(jsonObject,"lowerCaseIdentifiersCheck") );
		}

		if ( jsonObject.containsKey("upperCaseIdentifiersCheck") ) {
			databaseMeta.setForcingIdentifiersToUpperCase( MapUtil.getBoolean(jsonObject,"upperCaseIdentifiersCheck") );
		}

		if ( jsonObject.containsKey("preserveReservedCaseCheck") ) {
			databaseMeta.setPreserveReservedCase( MapUtil.getBoolean(jsonObject,"preserveReservedCaseCheck") );
		}

		if ( jsonObject.containsKey("preferredSchemaName") ) {
			databaseMeta.setPreferredSchemaName( MapUtil.getString(jsonObject,"preferredSchemaName") );
		}

		if ( jsonObject.containsKey("connectSQL") ) {
			databaseMeta.setConnectSQL( MapUtil.getString(jsonObject,"connectSQL") );
		}

		// Cluster panel settings
		databaseMeta.setPartitioned("Y".equalsIgnoreCase(MapUtil.getString(jsonObject,"partitioned")));
		if ( "Y".equalsIgnoreCase(MapUtil.getString(jsonObject,"partitioned")) ) {
			List partitionInfo = (List) jsonObject.get("partitionInfo");
			if(partitionInfo != null) {
				ArrayList<PartitionDatabaseMeta> list = new ArrayList<PartitionDatabaseMeta>();
				for (int i = 0; i < partitionInfo.size(); i++) {
					Map jsonObject2 = (Map) partitionInfo.get(i);
					PartitionDatabaseMeta meta = new PartitionDatabaseMeta();

					String partitionId = MapUtil.getString(jsonObject2,"partitionId");
					if ((partitionId == null) || (partitionId.trim().length() <= 0)) {
						continue;
					}

					meta.setPartitionId(MapUtil.getString(jsonObject2,"partitionId"));
					meta.setHostname(MapUtil.getString(jsonObject2,"hostname"));
					meta.setPort(MapUtil.getString(jsonObject2,"port"));
					meta.setDatabaseName(MapUtil.getString(jsonObject2,"databaseName"));
					meta.setUsername(MapUtil.getString(jsonObject2,"username"));
					meta.setPassword(MapUtil.getString(jsonObject2,"password"));
					list.add(meta);
				}
				if (list.size() > 0)
					databaseMeta.setPartitioningInformation(list.toArray( new PartitionDatabaseMeta[list.size()] ));
			}
		}

		if("Y".equalsIgnoreCase(MapUtil.getString(jsonObject,"usingConnectionPool"))) {
			databaseMeta.setUsingConnectionPool( true );

			try {
				databaseMeta.setInitialPoolSize(MapUtil.getInt(jsonObject,"initialPoolSize"));
			} catch (Exception e) {
			}

			try {
				databaseMeta.setMaximumPoolSize(MapUtil.getInt(jsonObject,"maximumPoolSize"));
			} catch (Exception e) {
			}

			List pool_params = (List) jsonObject.get("pool_params");
			if(pool_params != null) {
				Properties properties = new Properties();
				for(int i=0; i<pool_params.size(); i++) {
					Map jsonObject2 = (Map) pool_params.get(i);
					Boolean enabled = MapUtil.getBoolean(jsonObject2,"enabled");
					String parameter = MapUtil.getString(jsonObject2,"name");
					String value = MapUtil.getString(jsonObject2,"defValue");

					if (!enabled) {
						continue;
					}

					if( !StringUtil.isEmptys(value)) {
						properties.setProperty( parameter, value );
					}
				}
				databaseMeta.setConnectionPoolingProperties( properties );
			}
		}

		try {
			databaseMeta.setReadOnly(MapUtil.getBoolean(jsonObject,"read_only"));
		} catch (Exception e) {
		}
		return databaseMeta;
	}
}