package org.oiue.service.debug.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.io.Handler;
import org.oiue.service.io.Session;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.sql.SqlService;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServerHandler implements Handler {
	private FactoryService factoryService;
	private SqlService sqlService;
	
	public ServerHandler(FactoryService factoryService, SqlService sqlService) {
		this.factoryService = factoryService;
		this.sqlService = sqlService;
	}
	
	@Override
	public void received(Session session, String line, byte[] bytes) {
		
		if (line.startsWith("c")) {
			session.write("c");
			session.close();
			// } else if (line.startsWith("q1")) {
			// String event_name = "mobileSortQueryEvent";
			// String data_source_name = "mysql";
			// String service_id = "移动端大栏目查询接口";
			// String bundle_service_name = "mobileSortQuery";
			// String bundle_name = "com.leauto.service.leRadio.query.clientQuery.sortQuery";
			// Map map = new HashMap<>();
			//
			// try {
			// IResource iResource = factoryService.getBmo(IResource.class.getName());
			// Object o = iResource.callEvent(event_name, data_source_name, service_id, bundle_service_name, bundle_name, map);
			// session.write(o + "");
			// } catch (Throwable e) {
			// e.printStackTrace();
			// }
			// } else if (line.startsWith("q2")) {
			// String event_name = "systemQueryResAllEvents";
			// String data_source_name = "mysql";
			// String service_name = "系统维护查询服务";
			// String bundle_service_name = "systemServiceManager";
			// String bundle_name = "com.lingtu.zion.manager";
			// Map map = new HashMap<>();
			//
			// try {
			// IResource iResource = factoryService.getBmo(IResource.class.getName());
			// Object o = iResource.callEvent(event_name, data_source_name, service_name, bundle_service_name, bundle_name, map);
			// session.write(o+"");
			// } catch (Throwable e) {
			// e.printStackTrace();
			// }
		} else if (line.startsWith("q3")) {
			String event_id = "fm_system_service_query_user";
			String data_source_name = "mysql";
			Map map = new HashMap<>();
			map.put("source_id", "104929410");
			map.put("user_origin_id", "0");
			
			try {
				IResource iResource = factoryService.getBmo(IResource.class.getName());
				Object o = iResource.callEvent(event_id, data_source_name, map);
				session.write(o + "");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (line.startsWith("q4")) {
			String event_id = "fm_system_service_query_user";
			String data_source_name = "mysql";
			Map map = new HashMap<>();
			map.put("source_id", "1049294100");
			map.put("user_origin_id", "0");
			
			try {
				IResource iResource = factoryService.getBmo(IResource.class.getName());
				Object o = iResource.callEvent(event_id, data_source_name, map);
				session.write(o + "");
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			// session.write("k");
		} else if (line.startsWith("q5")) {
			List userInfoList = new ArrayList<>();
			userInfoList.add(UUID.randomUUID().toString());
			userInfoList.add("1527038616");
			userInfoList.add(System.currentTimeMillis() / 1000);
			userInfoList.add(116.286415d);
			userInfoList.add(40.04131);
			userInfoList.add("0e945a90-384f-492b-b81e-64d009712ceb");
			
			System.out.println(userInfoList.toString());
			// 将用户接班信息数据存储到数据库中
			String alias = "postgis";
			String sql = "insert into mall_trace(trace_id,product_id,scan_time,location,user_id) values(?,?,?,ST_GeometryFromText('POINT('||?||' '||?||')',4326),?)";
			try {
				sqlService.insertUpdateOrDelete(alias, sql, userInfoList);// 参数alias是数据库连接池
				userInfoList.clear();// 避免数据叠加造成参数值过多
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			// session.write(bufferScript.eval(line)+"");
		}
	}
	
	@Override
	public void closed(Session session) {
		
	}
	
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
