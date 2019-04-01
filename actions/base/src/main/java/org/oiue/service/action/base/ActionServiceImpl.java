/**
 *
 */
package org.oiue.service.action.base;

import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oiue.service.action.api.ActionFilter;
import org.oiue.service.action.api.ActionResultFilter;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineImpl;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.service.system.analyzer.TimeLogger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.serializ.CloneTools;
import org.oiue.tools.string.StringUtil;

/**
 * @author Every
 */
@SuppressWarnings({ "serial", "unused", "unchecked", "rawtypes" })
public class ActionServiceImpl implements ActionService {
	
	private Map<String, ActionFilter> beforeActionFilter = new HashMap<String, ActionFilter>();
	private Map<Integer, String> beforeFilterSort = new TreeMap<Integer, String>();
	
	private Map<String, ActionResultFilter> afterActionFilter = new HashMap<String, ActionResultFilter>();
	private Map<Integer, String> afterFilterSort = new TreeMap<Integer, String>();
	
	private Logger logger;
	private TimeLogger tLogger;
	private FrameActivator tracker;
	private Map defaultErrorMap;
	
	private boolean isDebug = false;
	
	public ActionServiceImpl(LogService logService, AnalyzerService analyzerService, FrameActivator tracker) {
		this.logger = logService.getLogger(this.getClass());
		this.tLogger = analyzerService.getLogger(this.getClass());
		this.tracker = tracker;
		logger.info("ActionService init");
	}
	
	public void updated(Map props) {
		String errorStr = props.get("action.msg") + "";
		try {
			// defaultErrorMap = new TreeMap();
			// List list = JSONUtil.parserStrToList(errorStr);
			// for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			// Map msg = (Map) iterator.next();
			// defaultErrorMap.put(msg.get("key"), msg);
			// }
			defaultErrorMap = JSONUtil.parserStrToMap(errorStr);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		isDebug = MapUtil.getBoolean(props, "isdebug", false);
		logger.debug("beforeFilterSort:{} \tafterFilterSort:{} ", beforeFilterSort, afterFilterSort);
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see com.lingtu.web.action.core.Action#request(java.util.Map)
	 */
	@Override
	public Map request(Map per) {
		PrintWriter writer = null;
		OutputStream stream = null;
//		int a=1;
//		while (true) {
//			if(a==2)
//				break;
//			logger.debug("执行action业务。。。。。。。。。");
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		}
		
		try {
			writer = (PrintWriter) per.remove("PrintWriter");
		} catch (Exception e) {}
		try {
			stream = (OutputStream) per.remove("OutputStream");
		} catch (Exception e) {}
		
		long starttime = 0l;
		Map req_per = null;
		if (logger.isDebugEnabled()) {
			logger.debug("action request data :" + JSONUtil.parserToStr(per));
		}
		if (tLogger.isDebugEnabled()) {
			starttime = System.currentTimeMillis();
			try {
				req_per = (Map) CloneTools.clone(per);
			} catch (NotSerializableException e) {
				req_per = new HashMap<>();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				req_per = new HashMap<>();
			}
		}
		String component_instance_event_id = null;
		String data_source = null;
		String modulename = MapUtil.getString(per, "modulename");
		String operation = MapUtil.getString(per, "operation");
		
		Online online = null;
		boolean run = true;
		anchor: while (run) {
			try {
				long startbfTime = 0l;
				long startTime = 0l;
				if (tLogger.isDebugEnabled()) {
					startbfTime = System.currentTimeMillis();
				}
				
				StatusResult afr = beforeActionFilter(modulename, operation, per);
				
				if (afr.getResult() == StatusResult._SUCCESS_OVER || afr.getResult() < StatusResult._NoncriticalAbnormal) {
					per.put("status", afr.getResult());
					per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
					run = false;
					break anchor;
				}
				
				// -----------------------call action start-------------------------------------
				startbfTime = System.currentTimeMillis();
				Object temppermission = isDebug ? per.get(PermissionConstant.permission_key) : per.remove(PermissionConstant.permission_key);
				online = (Online) per.remove(PermissionConstant.permission_user_key);
				
				if (online == null)
					online = new OnlineImpl();
				Map serviceOperation = null;
				
				if (temppermission instanceof Map) {
					serviceOperation = (Map) temppermission;
				}
				component_instance_event_id = MapUtil.getString(serviceOperation, "component_instance_event_id", null);
				data_source = MapUtil.getString(serviceOperation, "source", null);
				if (serviceOperation == null) {
					per.put("status", StatusResult._permissionDenied);
					per.put("exception", "权限错误！");
					run = false;
					break anchor;
				}
				
				String serviceName = (String) serviceOperation.get("serviceName");
				String methodName = (String) serviceOperation.get("methodName");
				
				if (StringUtil.isEmptys(serviceName) || StringUtil.isEmptys(methodName)) {
					per.put("status", StatusResult._ncriticalAbnormal);
					per.put("exception", "错误的访问！");
					run = false;
					break anchor;
				}
				
				Object service = tracker.getServiceForce(serviceName);
				
				if (service == null) {
					throw new OIUEException(StatusResult._service_can_not_found, "service can not found![" + serviceName + "]");
				}
				Object data = per.remove("data");
				Object source_data = null;
				if (afterActionFilter.values().size() > 0) {
					source_data = (data instanceof String) ? data : (data instanceof Map) ? JSONUtil.parserToStr((Map) data) : (data instanceof List) ? JSONUtil.parserToStr((List) data) : "";
					// source_data = (source_data_str.startsWith("{"))?JSONUtil.parserStrToMap(source_data_str):source_data_str.startsWith("[")?JSONUtil.parserStrToList(source_data_str):CloneTools.clone(data);
				}
				serviceOperation.put("export", per.remove("export"));
				serviceOperation.put("PrintWriter", writer);
				serviceOperation.put("OutputStream", stream);
				
				if (data == null) {
					Method method = service.getClass().getMethod(methodName, Map.class, String.class);
					per.put("data", method.invoke(service, serviceOperation, online.getTokenId()));
				} else if (data instanceof Map) {
					Method method = service.getClass().getMethod(methodName, Map.class, Map.class, String.class);
					per.put("data", method.invoke(service, data, serviceOperation, online.getTokenId()));
				} else if (data instanceof List) {
					Method method = service.getClass().getMethod(methodName, List.class, Map.class, String.class);
					per.put("data", method.invoke(service, data, serviceOperation, online.getTokenId()));
				} else if (data instanceof String) {
					Method method = service.getClass().getMethod(methodName, String.class, Map.class, String.class);
					per.put("data", method.invoke(service, data, serviceOperation, online.getTokenId()));
				} else {
					throw new OIUEException(StatusResult._mismatch_type, "data type error!");
				}
				
				if (tLogger.isDebugEnabled()) {
					Map para_tmp = new HashMap();
					para_tmp.put("startTime", startbfTime);
					para_tmp.put("endTime", System.currentTimeMillis());
					para_tmp.put("desc", "Action [" + serviceName + "." + methodName + "([Map/List/String,]Map,String)]");
					para_tmp.put("para", JSONUtil.parserToStr(per));
					tLogger.debug(para_tmp);
				}
				// -----------------------call action end----------------------------------------
				
				afr = afterActionFilter(modulename, operation, per, source_data);
				
				if (afr.getResult() == StatusResult._SUCCESS_OVER || afr.getResult() < StatusResult._NoncriticalAbnormal) {
					per.put("status", afr.getResult());
					per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
					run = false;
					break anchor;
				}
				
				if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
					String pers = per + "";
					logger.info("action gateway >>>" + (System.currentTimeMillis() - starttime) + "：" + (pers.length() > 1024 ? "too long response data." : pers));
				}
				per.put("status", StatusResult._SUCCESS);
			} catch (OIUEException e) {
				logger.error(e.getMessage(), e);
				String ex = ExceptionUtil.getCausedBySrcMsg(e);
				int status = ((OIUEException) e).getStatus().getResult();
				per.put("rtn", ((OIUEException) e).getRtnObject());
				per.put("status", status);
				if (defaultErrorMap!=null&&defaultErrorMap.containsKey(status + "")) {
					per.put("msg", defaultErrorMap.get(status + ""));
					per.put("exception", ex);
					run = false;
					break anchor;
				}else if(status==StatusResult._sql_error) {
					per.put("msg", "操作失败");
					per.put("exception", ex);
					run = false;
					break anchor;
					
				}else {
					per.put("msg", "操作失败");
					per.put("exception", ex);
					run = false;
					break anchor;
				}
			} catch (Throwable e) {
//				logger.error(e.getMessage(), e);
				if (e instanceof InvocationTargetException)
					e = ((InvocationTargetException) e).getTargetException();
				if (e instanceof UndeclaredThrowableException)
					e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
				if (e instanceof InvocationTargetException)
					e = ((InvocationTargetException) e).getTargetException();
				if (e instanceof UndeclaredThrowableException)
					e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
				
				String ex = ExceptionUtil.getCausedBySrcMsg(e);
				
				if (e instanceof OIUEException) {
					int status = ((OIUEException) e).getStatus().getResult();
					per.put("rtn", ((OIUEException) e).getRtnObject());
					per.put("status", status);
					logger.error(per+"",e);
					if (defaultErrorMap!=null&&defaultErrorMap.containsKey(status + "")) {
						per.put("msg", defaultErrorMap.get(status + ""));
						per.put("exception", ex);
						run = false;
						break anchor;
					}else if(status==StatusResult._sql_error) {
						Map de = (Map) ((OIUEException) e).getRtnObject();
						String sqlstate=MapUtil.getString(de, "sqlstate");
						per.put("msg",sqlstate==null?"操作失败":sqlstate.startsWith("02")?"没有数据": sqlstate.startsWith("03")?"SQL语句尚未结束": sqlstate.startsWith("08000")?"连接异常": sqlstate.startsWith("08003")?"连接不存在":
							sqlstate.startsWith("08006")?"连接失败": sqlstate.startsWith("08")?"连接异常": sqlstate.startsWith("09")?"触发器动作异常": sqlstate.startsWith("22")?"数据异常":
								sqlstate.startsWith("23")?"违反完成性约束": sqlstate.startsWith("26")?"非法SQL语句名": sqlstate.startsWith("2F")?"SQL过程异常": sqlstate.startsWith("3D")?"非法数据库名":
									sqlstate.startsWith("42")?"语法错误或者违反访问规则": sqlstate.startsWith("P0")?"语法错误或者违反访问规则": sqlstate.startsWith("53")?"资源不够":"操作失败");
						per.put("exception", ex);
						run = false;
						break anchor;
					}else {
						per.put("msg", "操作失败");
						per.put("exception", ex);
						run = false;
						break anchor;
					}
						
				}

				logger.error(ex, e);
				if (defaultErrorMap != null && ex != null) {
					for (Iterator iterator = defaultErrorMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						if (ex.indexOf(key) > 0) {
							Object msg = defaultErrorMap.get(key);
							if (msg instanceof Map) {
								Map msgm = (Map) msg;
								try {
									Map msgn = (Map) msgm.get(modulename);
									per.put("status", MapUtil.getInt(msgn, "status", -1));
									per.put("msg", MapUtil.getString(msgn, "msg",ex));
									per.put("exception", ex);
									
									run = false;
									break anchor;
								} catch (Exception e2) {}
								
								per.put("status", MapUtil.getInt(msgm, "status", -1));
								per.put("msg", MapUtil.getString(msgm, "msg",ex));
								per.put("exception", ex);
								
								run = false;
								break anchor;
							} else if (msg instanceof String) {
								per.put("status", key);
								per.put("msg", msg);
								per.put("exception", ex);
							}
							run = false;
							break anchor;
						}
					}
				}
				per.put("status", (e instanceof OIUEException) ? ((OIUEException) e).getStatus().getResult() : StatusResult._ncriticalAbnormal);
				per.put("msg", "操作错误，不允许的操作！");
				per.put("exception", ex);
			}
			run = false;
		}
		if (tLogger.isDebugEnabled()) {
			Map para = new HashMap();
			para.put("startTime", starttime);
			para.put("endTime", System.currentTimeMillis());
			para.put("user_id", online != null ? online.getUser_id() : null);
			para.put("component_instance_event_id", component_instance_event_id == null ? "fm_managed_global" : component_instance_event_id);
			para.put("source", data_source);
			para.put("desc", modulename + "/" + operation);
			req_per.remove("token");
			para.put("para", req_per);
			para.put("status", MapUtil.getInt(per, "status", -1));
			para.put("resp_para", per);
			tLogger.debug(para);
		}
		if (logger.isDebugEnabled()) {
			String pers=JSONUtil.parserToStr(per);
			logger.debug("action response data :" +  (pers.length() > 1024 ? "too long response data." : pers));
		}
		return per;
	}
	
	@Override
	public String request(String perStr) {
		return JSONUtil.parserToStr(request(MapUtil.fromString(perStr)));
	}
	
	private StatusResult beforeActionFilter(String modulename, String operation, Map per) {
		StatusResult afr = new StatusResult();
		afr.setResult(StatusResult._SUCCESS);
		
		// -----------------------before filter start-------------------------------------
		if (logger.isDebugEnabled()) {
			logger.debug("action actionPoolFilter :" + beforeActionFilter);
		}
		long startbfTime = 0l;
		long startTime = 0l;
		if (tLogger.isDebugEnabled()) {
			startbfTime = System.currentTimeMillis();
		}
		for (ActionFilter afilter : beforeActionFilter.values()) {
			startTime = System.currentTimeMillis();
			
			afr = afilter.doFilter(per);
			
			if (tLogger.isDebugEnabled()) {
				Map para = new HashMap();
				para.put("startTime", startTime);
				para.put("endTime", System.currentTimeMillis());
				para.put("desc", "ActionFilter[" + afilter.getClass().getName() + "]" + afr);
				para.put("para", JSONUtil.parserToStr(per));
				this.tLogger.debug(para);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("run before ActionFilter [" + afilter + "]：" + afr + ";per=" + per);
			}
			if (afr.getResult() == StatusResult._SUCCESS_OVER) {
				per.put("status", afr.getResult());
				per.put("msg", afr.getDescription());
				break;
			} else if (afr.getResult() == StatusResult._SUCCESS) {
				continue;
			} else if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
				per.put("status", afr.getResult());
				per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
				per.put("msg", afr.getDescription());
				break;
			}
		}
		if (tLogger.isDebugEnabled()) {
			Map para = new HashMap();
			para.put("startTime", startbfTime);
			para.put("endTime", System.currentTimeMillis());
			para.put("desc", "Before ActionFilters [" + beforeActionFilter + "]");
			para.put("para", JSONUtil.parserToStr(per));
			this.tLogger.debug(para);
		}
		// -----------------------before filter end-------------------------------------
		return afr;
	}
	
	private StatusResult afterActionFilter(String modulename, String operation, Map per, Object source_data) {
		
		StatusResult afr = new StatusResult();
		afr.setResult(StatusResult._SUCCESS);
		
		// -----------------------after filter start-------------------------------------
		if (logger.isDebugEnabled()) {
			logger.debug("action actionRPoolFilter :" + afterActionFilter);
		}
		long startbfTime = 0l;
		long startTime = 0l;
		if (tLogger.isDebugEnabled()) {
			startbfTime = System.currentTimeMillis();
		}
		for (ActionResultFilter afilter : afterActionFilter.values()) {
			startTime = System.currentTimeMillis();
			
			afr = afilter.doFilter(per, source_data);
			
			if (tLogger.isDebugEnabled()) {
				Map para = new HashMap();
				para.put("startTime", startTime);
				para.put("endTime", System.currentTimeMillis());
				para.put("desc", "after ActionFilter[" + afilter.getClass().getName() + "]" + afr);
				para.put("para", JSONUtil.parserToStr(per));
				tLogger.debug(para);
			}
			
			if (afr.getResult() == StatusResult._SUCCESS_OVER) {
				per.put("status", afr.getResult());
				per.put("msg", afr.getDescription());
				break;
			} else if (afr.getResult() == StatusResult._SUCCESS) {
				continue;
			} else if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
				per.put("status", afr.getResult());
				per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
				per.put("msg", afr.getDescription());
				break;
			}
		}
		if (tLogger.isDebugEnabled()) {
			Map para = new HashMap();
			para.put("startTime", startbfTime);
			para.put("endTime", System.currentTimeMillis());
			para.put("desc", "After ActionFilters [" + afterActionFilter + "]");
			para.put("para", JSONUtil.parserToStr(per));
			tLogger.debug(para);
		}
		// -----------------------after filter start-------------------------------------
		return afr;
	}
	
	@Override
	public void unregisterAllActionFilter() {
		// synchronized (actionPoolFilter) {
		beforeActionFilter.clear();
		afterActionFilter.clear();
		// }
		// synchronized (actionPoolFilterList) {
		beforeFilterSort.clear();
		afterFilterSort.clear();
		// }
		
	}
	
	@Override
	public synchronized boolean registerActionFilter(String requestAction, ActionFilter actionFilter, int index) {
		if (beforeFilterSort.get(index) != null) {
			throw new OIUEException(StatusResult._blocking_errors, "index conflict! name=" + requestAction + ", old index is " + beforeFilterSort.get(index)+",list:"+beforeFilterSort);
		}
		if (beforeActionFilter.get(requestAction) == null) {
			beforeActionFilter.put(requestAction, actionFilter);
			beforeFilterSort.put(index, requestAction);
			
			Map<String, ActionFilter> actionPoolFilterTemp = new LinkedHashMap<String, ActionFilter>();
			for (Iterator iterator = beforeFilterSort.values().iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				actionPoolFilterTemp.put(value, beforeActionFilter.get(value));
			}
			beforeActionFilter = actionPoolFilterTemp;
			return true;
		}
		return false;
	}
	
	@Override
	public void unregisterActionFilter(String requestAction) {
		ActionFilter actionFilter = beforeActionFilter.remove(requestAction);
		for (Iterator iterator = beforeFilterSort.values().iterator(); iterator.hasNext();) {
			String requestActions = (String) iterator.next();
			if (requestAction.equals(requestActions))
				iterator.remove();
		}
	}
	
	@Override
	public synchronized boolean registerActionResultFilter(String requestAction, ActionResultFilter actionResultFilter, int index) {
		
		if (afterFilterSort.get(index) != null) {
			throw new RuntimeException("index conflict! name=" + requestAction + ", old index is " + afterFilterSort.get(index));
		}
		if (afterActionFilter.get(requestAction) == null) {
			afterActionFilter.put(requestAction, actionResultFilter);
			afterFilterSort.put(index, requestAction);
			
			Map<String, ActionResultFilter> actionPoolFilterTemp = new LinkedHashMap<String, ActionResultFilter>();
			for (Iterator iterator = afterFilterSort.values().iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				actionPoolFilterTemp.put(value, afterActionFilter.get(value));
			}
			afterActionFilter = actionPoolFilterTemp;
			return true;
		}
		return false;
	}
	
	@Override
	public void unregisterActionResultFilter(String requestAction) {
		ActionResultFilter actionFilter = afterActionFilter.remove(requestAction);
		for (Iterator iterator = afterFilterSort.values().iterator(); iterator.hasNext();) {
			String requestActions = (String) iterator.next();
			if (requestAction.equals(requestActions))
				iterator.remove();
		}
	}
	
	@Override
	public Map<String, ActionFilter> getBeforeActionFilterPool() {
		return beforeActionFilter;
	}
	
	private static Map sortByComparator(Map unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());
		
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		
		// put sorted list into map again
		// LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	public void finalize(){
		logger.debug("回收资源了。。。。。");
	}
}
