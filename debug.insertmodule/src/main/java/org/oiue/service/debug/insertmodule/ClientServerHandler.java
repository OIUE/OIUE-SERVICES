package org.oiue.service.debug.insertmodule;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.oiue.service.sql.SqlService;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;
import org.oiue.service.tcp.TcpService;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.sql.SQL;

public class ClientServerHandler implements Handler {
	@SuppressWarnings("unused")
	private TcpService tcpService = null;
	private SqlService sqlService = null;
	
	public ClientServerHandler(TcpService tcpScript, SqlService sqlService) {
		this.tcpService = tcpScript;
		this.sqlService = sqlService;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void received(Session session, String line, byte[] bytes) {
		System.out.println("reader line:" + line);
		
		try {
			if (line.startsWith("c")) {
				String sql1 = "INSERT INTO `fm_bundle` (`bundle_id`, `name`, `version`, `desc`, `status`, `md5`, `remark`, `update_time`, `bundle_class_id`, `update_user_id`) " + "VALUES ([bundle_id],[name],[version],[desc],1,[md5],[remark],0,[bundle_class_id],[update_user_id])";
				String sql2 = "INSERT INTO `fm_bundle_service` (`bundle_id`, `bundle_service_class_id`, `bundle_service_id`, `name`, `desc`, `remark`, `short_code`, `status`, `sort`, `update_time`, `update_user_id`) " + "VALUES ([bundle_id],[bundle_service_class_id],[bundle_service_id],[service_name],[desc],[remark],NULL,1,0,0,[update_user_id])";
				String sql3 = "INSERT INTO `fm_service` (`service_id`, `service_class_id`, `bundle_service_id`, `name`, `desc`, `status`, `update_user_id`, `update_time`) " + "VALUES ([service_id],[service_class_id],[bundle_service_id],[desc],NULL,1,[update_user_id],0)";
				String sql4 = "INSERT INTO `fm_service_event` (`service_event_id`, `service_id`, `name`, `desc`, `remark`, `type`, `adapter`, `update_time`, `update_user_id`) " + "VALUES ([service_event_id],[service_id],[service_event_name],[desc],[remark],[event_type],[adapter],0,[update_user_id])";
				String sql5 = "INSERT INTO `fm_service_event_parameters` (`service_event_id`, `service_id`, `desc`, `remark`, `rule`, `content`, `expression`, `update_time`, `service_event_parameters_id`, `data_type_class_id`, `update_user_id`) " + "VALUES ([service_event_id],[service_id],[desc],[remark],'',[content],[expression],0,uuid(),[data_type_class_id],[update_user_id])";
				String sql6 = "INSERT INTO `fm_component` (`component_id`, `component_class_id`, `name`, `desc`, `remark`, `path`, `short_code`, `status`, `sort`, `update_time`, `update_user_id`) " + "VALUES ([component_id],[component_class_id],[component_name],'',NULL,NULL,NULL,1,0,0,[update_user_id])";
				String sql7 = "INSERT INTO `fm_component_event` (`component_event_id`, `component_id`, `name`, `desc`, `remark`, `short_code`, `status`, `sort`, `event_type_id`, `update_time`, `update_user_id`) " + "VALUES ([component_event_id],[component_id],[component_event_name],'',NULL,NULL,1,0,NULL,0,[update_user_id])";
				String sql8 = "INSERT INTO `fm_component_instance` (`component_instance_id`, `component_id`, `component_instance_name`, `component_instance_desc`, `parent_component_id`, `service_config_id`, `model`, `service_id`, `update_time`, `update_user_id`) " + "VALUES ([component_instance_id],[component_id],[component_instance_name],NULL,0,NULL,NULL,NULL,0,[update_user_id])";
				String sql9 = "INSERT INTO `fm_component_instance_event` (`component_instance_event_id`, `component_instance_id`, `component_id`, `component_event_id`, `event_code`, `update_time`, `update_user_id`) " + "VALUES ([component_instance_event_id],[component_instance_id],[component_id],[component_event_id],'',0,[update_user_id])";
				String sql10 = "INSERT INTO `fm_event_component_service` (`event_component_service_id`, `component_instance_event_id`, `service_event_id`, `update_time`, `update_user_id`) " + "VALUES ([event_component_service_id],[component_instance_event_id],[service_event_id],0,[update_user_id])";
				
				Map data = new HashMap<>();
				data.put("name", "org.oiue.service.event.system.time");// org.oiue.service.event.execute
				data.put("version", "1.0.0");
				data.put("component_instance_name", "systime");// chat_config
				data.put("component_event_name", "systime");// query
				data.put("service_name", "EventSystemTimeService");// EventExecuteService
				data.put("service_event_name", "getTime");// execute
				
				String id = data.get("component_instance_name") + "_" + data.get("component_event_name") + "_ESTS_" + data.get("service_event_name");
				
				data.put("service_id", id);// mobile_service_event_execute
				data.put("service_event_id", id);// system_service_insert_ll_userconfig
				data.put("component_id", id);// leliao_chat_config
				data.put("component_event_id", id);// leliao_chat_config_user_config_q
				data.put("component_name", id);// chat_config
				data.put("component_instance_id", id);// leliao_chat_config
				data.put("component_instance_event_id", id);// leliao_cie_chat_config_ucq
				data.put("event_component_service_id", id);// leliao_cie_chat_config_ucq
				data.put("desc", "获取服务器时间");
				data.put("content", "");
				data.put("expression", "");
				data.put("event_type", "query");
				data.put("bundle_id", data.get("name") + "(" + data.get("version") + ")");// org.oiue.service.event.execute(1.0.0)
				data.put("bundle_service_id", data.get("name") + "." + data.get("service_name"));// org.oiue.service.event.execute.EventExecuteService
				data.put("data_type_class_id", "system_data_type_mysql");
				data.put("component_class_id", "leliao_system_service");// leliao_system_service
				
				data.put("update_user_id", "17f3f93a-4580-11e5-b785-fa163e6f7961");
				data.put("adapter", "Every");
				data.put("bundle_class_id", "system_service_class");
				data.put("md5", "3f0d54728e5725c9791d51620609315b");
				data.put("bundle_service_class_id", "system_service_class");
				data.put("service_class_id", "db64b933-9cdb-11e5-bb46-fa163e51eb24");
				data.put("remark", "系统服务，请勿修改！");
				
				Connection conn = this.sqlService.getConnection("mysql");
				conn.setAutoCommit(false);
				PreparedStatement pstmt = null;
				try {
					SQL sql = null;
					
					sql = this.AnalyzeSql(sql1, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql2, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql3, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql4, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql5, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql6, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql7, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql8, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql9, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					sql = this.AnalyzeSql(sql10, data);
					pstmt = conn.prepareStatement(sql.sql);
					this.setQueryParams(sql.pers, pstmt);
					pstmt.executeUpdate();
					
					if (conn != null)
						conn.commit();
					
				} catch (Throwable e) {
					e.printStackTrace();
					if (conn != null)
						conn.rollback();
				} finally {
					if (pstmt != null)
						pstmt.close();
					if (conn != null)
						conn.close();
				}
			} else if (line.startsWith("r")) {// push
			} else {
				session.write("");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			session.write(e.getMessage());
		}
	}
	
	/**
	 * 通过反射 将字段属性替换到Sql语句中(注意大小写)
	 * 
	 * @param sourceStr
	 * @param tb
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public SQL AnalyzeSql(String sourceStr, Map tb) {
		Collection<Object> pers = new ArrayList<Object>();
		StringBuffer s = new StringBuffer();
		String sourcetemp = sourceStr;
		String splitStr = "@";
		sourcetemp = sourcetemp.replace("[", splitStr);
		String[] SQLtemps = sourcetemp.split(splitStr);
		s.append(SQLtemps[0]);
		for (int i = 1; i < SQLtemps.length; i++) {
			String tt = SQLtemps[i].replace("]", splitStr);
			String[] temps = tt.split(splitStr);
			if (temps.length == 2) {
				pers.add(MapUtil.get(tb, temps[0]));
				s.append("?").append(temps[1]);
			} else
				throw new RuntimeException("");
		}
		SQL sql = new SQL();
		sql.sql = "" + s;
		sql.pers = pers;
		return sql;
	}
	
	/**
	 * 参JDBCUtil实现 设置prepared的参数
	 * 
	 * @param column 参数的标号
	 * @param obj Object obj是参数值
	 * @throws SQLException
	 */
	public void setParameter(int column, Object obj, PreparedStatement pstmt) {
		try {
			if (obj instanceof java.lang.String) {
				String keyStrs = (String) obj;
				pstmt.setString(column, keyStrs);
			} else if (obj instanceof Integer) {
				pstmt.setInt(column, ((Integer) obj).intValue());
			} else if (obj instanceof Float) {
				pstmt.setFloat(column, ((Float) obj).floatValue());
			} else if (obj instanceof Long) {
				pstmt.setLong(column, ((Long) obj).longValue());
			} else if (obj instanceof Date) {
				pstmt.setTimestamp(column, new Timestamp(((Date) obj).getTime()));
			} else if (obj instanceof BigDecimal) {
				pstmt.setBigDecimal(column, (BigDecimal) obj);
				// ------Blob,Clob,Binary--------
				// } else if (obj instanceof Blob) {
				// BlobType blobType = new BlobType();
				// blobType.set(pstmt, obj, column);
				// } else if (obj instanceof Clob) {
				// ClobType clobType = new ClobType();
				// clobType.set(pstmt, obj, column);
			} else if (obj instanceof URL) {
				pstmt.setString(column, ((URL) obj).getPath());
			} else if (obj instanceof URI) {
				pstmt.setString(column, ((URI) obj).getPath());
			} else {// if(obj instanceof Boolean)
				pstmt.setObject(column, obj);
			}
			// else logger.error("不支持的参数类型!");
			
		} catch (Exception e) {
			throw new RuntimeException("参数设置出错：" + e);
		}
	}
	
	/**
	 * 根据参数值queryParams集合
	 * 
	 * @param queryParams @
	 */
	@SuppressWarnings("rawtypes")
	public void setQueryParams(Collection queryParams, PreparedStatement pstmt) {
		if ((queryParams == null) || (queryParams.isEmpty())) {
			return;
		}
		Iterator iter = queryParams.iterator();
		int i = 1;
		while (iter.hasNext()) {
			Object key = iter.next();
			setParameter(i, key, pstmt);
			i++;
		}
	}
	
	@Override
	public void closed(Session session) {}
	
	@Override
	public void opened(Session session) {
		session.write("debug Cache Tree Service");
	}
	
	@Override
	public void idled(Session session) {
		session.close();
	}
	
	@Override
	public void sent(Session session) {
		
	}
	
	@Override
	public int getReaderIdleCount() {
		return 0;
	}
}
