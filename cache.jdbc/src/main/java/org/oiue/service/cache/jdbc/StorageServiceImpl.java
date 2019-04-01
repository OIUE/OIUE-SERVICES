package org.oiue.service.cache.jdbc;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.sql.SqlService;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

public class StorageServiceImpl implements CacheService, Runnable {
	
	private Logger logger;
	private SqlService sqlService;
	private CacheServiceManager cacheServiceManager;
	
	private Map<String, LinkedList<Collection<Object>>> storageParamsMap = new HashMap<>();
	
	public StorageServiceImpl(LogService logService, SqlService sqlService, CacheServiceManager cacheServiceManager) {
		logger = logService.getLogger(this.getClass());
		this.sqlService = sqlService;
		this.cacheServiceManager=cacheServiceManager;
		new Thread(this, "SystemJDBCStorageService").start();
	}
	
	@Override
	public void put(String name, Object object, Type type) {
		synchronized (storageParamsMap) {
			LinkedList<Collection<Object>> params = storageParamsMap.get(name);
			if (params == null) {
				params = new LinkedList<Collection<Object>>();
				storageParamsMap.put(name, params);
			}
			
			if (object instanceof Collection) {
				params.add((Collection<Object>) object);
			} else {
				List<Object> t = new ArrayList<>();
				t.add(object);
				params.add(t);
			}
		}
	}
	
	public void storage(Map event, Collection<Collection<Object>> paramslist) {
		if (logger.isDebugEnabled()) {
			logger.debug("Storage [{}] [{}]", event, paramslist);
		}
		Connection conn = sqlService.getConnection(MapUtil.getString(event, "ds_name"));
		if (conn == null) {
			logger.error("can't get connect from sqlService, event = " + event);
			return;
		}
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(MapUtil.getString(event, "content"));
			String expression = MapUtil.getString(event, "expression");
			for (Collection<Object> params : paramslist) {
				try {
					if(params.size()==1){
						Object v = params.iterator().next();
						if(v instanceof Map){
							if (expression != null && !StringUtil.isEmptys(expression)) {
								String[] expressions = expression.split(",");
								Collection per=new ArrayList<>(expressions.length);
								for (String ep : expressions) {
									if (ep != null)
										ep = ep.trim();
									per.add(MapUtil.get((Map)v, ep));
								}
								params=per;
							}
						}
					}
					
					this.setQueryParams(pstmt, params);
					pstmt.addBatch();
				} catch (Exception e) {
					logger.error("event:{} params:{} msg:{}", event,params,e.getMessage());
				}
			}
			pstmt.executeBatch();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			paramslist.clear();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			
			try {
				conn.close();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}
	
	@Override
	public void put(String name, String key, Object object, Type type) {
		
	}
	
	@Override
	public void put(String name, Object object, Type type, int expire) {
		
	}
	
	@Override
	public void put(String name, String key, Object object, Type type, int expire) {
		
	}
	
	@Override
	public void put(String name, String key, Type type, Object... objects) {
		
	}
	
	@Override
	public Object get(String name) {
		return null;
	}
	
	@Override
	public boolean contains(String name, String... keys) {
		return false;
	}
	
	@Override
	public Object get(String name, String key) {
		return null;
	}
	
	@Override
	public long delete(String name) {
		return 0;
	}
	
	@Override
	public long delete(String name, String... keys) {
		return 0;
	}
	
	@Override
	public boolean exists(String name) {
		return false;
	}
	
	@Override
	public void swap(String nameA, String nameB) {
		
	}
	
	/**
	 * 参JDBCUtil实现 设置prepared的参数
	 *
	 * @param column 参数的标号
	 * @param obj Object obj是参数值
	 * @throws SQLException sql异常
	 */
	public void setParameter(PreparedStatement pstmt, int column, Object obj) {
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
			} else if (obj instanceof URL) {
				pstmt.setString(column, ((URL) obj).getPath());
			} else if (obj instanceof URI) {
				pstmt.setString(column, ((URI) obj).getPath());
			} else if (obj instanceof Map) {
				pstmt.setString(column, JSONUtil.parserToStr((Map) obj));
			} else if (obj instanceof List) {
				pstmt.setString(column, JSONUtil.parserToStr((List) obj));
			} else {// if(obj instanceof Boolean)
				pstmt.setObject(column, obj);
			}
			// else logger.error("不支持的参数类型!");
			
		} catch (Exception e) {
			throw new RuntimeException("参数设置出错[" + column + "," + obj + "]：" + e);
		}
	}
	
	public void setQueryParams(PreparedStatement pstmt, Collection queryParams) {
		if ((queryParams == null) || (queryParams.isEmpty())) {
			return;
		}
		Iterator iter = queryParams.iterator();
		int i = 1;
		while (iter.hasNext()) {
			Object key = iter.next();
			setParameter(pstmt, i, key);
			i++;
		}
	}
	
	@Override
	public void run() {
//		String alias = "postgis";
//		String selectEvent = "select se.service_event_id,sep.rule as ds_name,sep.content,sep.expression from oiue.fm_service_event se,oiue.fm_service_event_parameters sep where se.service_event_id=? and se.service_event_id=sep.service_event_id and se.type='storage' ";
		Map<String, Map> events = new HashMap<>();
		while (true) {
			Set<String> tmk = new HashSet();
			synchronized (storageParamsMap) {
				tmk.addAll(storageParamsMap.keySet());
			}
			for (String key : tmk) {
				Map event = events.get(key);
				try {
					if (event == null) {
						List params = new ArrayList();
//						params.add(key);
//						SqlServiceResult sr = sqlService.selectMap(alias, selectEvent, params);
//						if (sr.getData() instanceof List) {
//							List datas = (List) sr.getData();
//							if (datas.size() == 1)
//								event = (Map) datas.get(0);
//							else {
//								logger.error("query event is error:" + datas+"|event:"+key);
//								synchronized (storageParamsMap) {
//									storageParamsMap.remove(key);
//								}
//							}
//						}
						event=(Map) cacheServiceManager.get("system_storage_jdbc",key);
						if (event != null) {
							LinkedList<Collection<Object>> list = null;
							synchronized (storageParamsMap) {
								list = storageParamsMap.remove(key);
							}
							if (list != null)
								storage(event, list);
						}
					}
				} catch (Throwable e) {
					logger.error("call event is error:" + e.getMessage(), e);
				}
			}
			try {
				Thread.sleep(5);
			} catch (Throwable e) {}
		}
		
	}
}
