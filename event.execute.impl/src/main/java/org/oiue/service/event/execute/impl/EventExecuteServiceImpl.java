package org.oiue.service.event.execute.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.event.execute.EventExecuteService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.dmo.CallBack;
import org.oiue.service.odp.event.api.EventField;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.list.ListUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings("serial")
public class EventExecuteServiceImpl implements EventExecuteService {
	
	protected static AnalyzerService analyzerService;
	protected static Logger logger;
	protected static CacheServiceManager cache;
	protected static OnlineService onlineService;
	protected static FactoryService factoryService;
	private static String data_source_name = null;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object execute(Map data, Map event, String tokenId) {
		IResource iresource = factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			String export = MapUtil.getString(event, "export");
			if ("csv".equals(export)) {
				// PrintWriter wr = (PrintWriter) event.remove("PrintWriter");
				OutputStream stream = (OutputStream) event.remove("OutputStream");
				try {
					String[] keys;
					List<Map> results = iresource.callEvent("8e0cea64-ba64-414a-9ac8-3178e1b6a5b0", data_source_name, event);
					if (results != null && results.size() > 0) {
						List desc = new ArrayList<>();
						List alias = new ArrayList<>();
						for (Map result : results) {
							desc.add(MapUtil.get(result, "desc"));
							alias.add(MapUtil.get(result, "alias"));
						}
						// wr.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }));
						// wr.write(ListUtil.ListJoin(desc, ","));
						
						try {
							stream.write(ListUtil.ListJoin(desc, ",").getBytes("GBK"));
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
						keys = (String[]) alias.toArray(new String[0]);
					} else
						return null;
					
					CallBack cb = new CallBack() {
						@Override
						public boolean callBack(Map paramMap) {
							// wr.write("\r\n");
							// wr.write(ListUtil.ListJoin(MapUtil.toList(paramMap, keys), ","));
							try {
								stream.write("\r\n".getBytes("GBK"));
								stream.write(ListUtil.ListJoinCsv(MapUtil.toList(paramMap, keys)).getBytes("GBK"));
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
							return true;
						}
					};
					iresource = factoryService.getBmo(IResource.class.getName());
					Map eventm = iresource.getEventByIDName(MapUtil.getString(event, EventField.service_event_id), data_source_name);
					eventm.put("EVENT_TYPE", "selects");
					iresource = factoryService.getBmo(IResource.class.getName());
					return iresource.executeEvent(eventm, data_source_name, data, cb);
				} finally {
					try {
						stream.flush();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
					// wr.flush();
				}
			} else {
				if (!StringUtil.isTrue(MapUtil.getString(data, "system_test_execute", "n"))) {
					iresource = factoryService.getBmo(IResource.class.getName());
					return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
				} else {
					String processKey = UUID.randomUUID().toString().replaceAll("-", "");
					try {
						iresource = factoryService.getBmoByProcess(IResource.class.getName(), processKey);
						return iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data);
					} finally {
						try {
							factoryService.RollbackByProcess(processKey);
						} catch (SQLException e) {
							throw new OIUEException(StatusResult._data_error, e.getMessage());
						}
					}
				}
			}
		}
		throw new RuntimeException("service can not init！");
	}
	
	@Override
	public Object execute(List datas, Map event, String tokenId) {
		IResource iresource = factoryService.getBmo(IResource.class.getName());
		if (onlineService != null && iresource != null) {
			List rtnList = new ArrayList();
			for (Map data : (List<Map>) datas) {
				try {
					rtnList.add(iresource.callEvent(MapUtil.getString(event, EventField.service_event_id), data_source_name, data));
				} catch (Throwable e) {
					rtnList.add(e.getMessage());
				}
			}
			return rtnList;
		}
		throw new RuntimeException("service can not init！");
	}
	
}
