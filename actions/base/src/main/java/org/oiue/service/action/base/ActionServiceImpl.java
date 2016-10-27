/**
 * 
 */
package org.oiue.service.action.base;

import java.lang.reflect.Method;
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
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.permission.PermissionConstant;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.service.system.analyzer.TimeLogger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
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

    public ActionServiceImpl(LogService logService, AnalyzerService analyzerService, FrameActivator tracker) {
        this.logger = logService.getLogger(this.getClass());
        this.tLogger = analyzerService.getLogger(this.getClass());
        this.tracker = tracker;
        logger.info("ActionService init");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.lingtu.web.action.core.Action#request(java.util.Map)
     */
    @Override
    public Map request(Map per) {
        long starttime = 0l;
        if (logger.isDebugEnabled()) {
            logger.debug("action request data :" + per);
        }
        Map rtnMap;
        try {
            Object data = null;
            StatusResult afr;


            // -----------------------before filter start-------------------------------------
            if (logger.isDebugEnabled()) {
                logger.debug("action actionPoolFilter :" + beforeActionFilter);
            }
            long startbfTime = System.currentTimeMillis();
            for (ActionFilter afilter : beforeActionFilter.values()) {
                long startTime = System.currentTimeMillis();

                afr = afilter.doFilter(per);

                if (tLogger.isDebugEnabled()) {
                    long endTime = System.currentTimeMillis();
                    Map para = new HashMap();
                    para.put("startTime", startTime);
                    para.put("endTime", endTime);
                    para.put("desc", "ActionFilter[" + afilter.getClass().getName() + "]" + afr);
                    try {
                        para.put("para", CloneTools.clone(per));
                    } catch (Throwable e) {
                        para.put("para", per);
                    }
                    this.tLogger.debug(para);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("run before ActionFilter [" + afilter + "]：" + afr + ";per=" + per);
                }
                if (afr.getResult() == StatusResult._SUCCESS_OVER) {
                    per.put("status", afr.getResult());
                    per.put("msg", afr.getDescription());
                    return per;
                } else if (afr.getResult() == StatusResult._SUCCESS) {
                    continue;
                } else if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
                    per.put("status", afr.getResult());
                    per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
                    return per;
                }
            }
            if (tLogger.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                Map para = new HashMap();
                para.put("startTime", startbfTime);
                para.put("endTime", endTime);
                para.put("desc", "Before ActionFilters [" + beforeActionFilter + "]");
                try {
                    para.put("para", CloneTools.clone(per));
                } catch (Throwable e) {
                    para.put("para", per);
                }
                this.tLogger.debug(para);
            }
            // -----------------------before filter end-------------------------------------

            // -----------------------call action start-------------------------------------
            startbfTime = System.currentTimeMillis();
            Object temppermission = per.remove(PermissionConstant.permission_key);

            Map tempPer = new HashMap();
            try {
                tempPer = (Map) CloneTools.clone(per);
            } catch (Throwable e) {
            }

            Map permission = null;

            if (temppermission instanceof Map) {
                permission = (Map) temppermission;
            }
            if (permission == null) {
                per.put("status", StatusResult._ncriticalAbnormal);
                per.put("exception", "错误的访问！");
                return per;
            }

            String serviceName = (String) permission.get("serviceName");
            String methodName = (String) permission.get("methodName");

            if (StringUtil.isEmptys(serviceName) || StringUtil.isEmptys(methodName)) {
                per.put("status", StatusResult._ncriticalAbnormal);
                per.put("exception", "错误的访问！");
                return per;
            }

            Object service = tracker.getServiceForce(serviceName);

            if(service==null){
                logger.error("service can not found!["+serviceName+"]");
                throw new RuntimeException("service can not found!");
            }
            data = per.remove("data");
            if (data == null) {
                Method method = service.getClass().getMethod(methodName, Map.class, String.class);
                per.put("data", method.invoke(service, permission, per.get("tokenid")));
            } else {
                if (data instanceof Map) {
                    Method method = service.getClass().getMethod(methodName, Map.class, Map.class, String.class);
                    per.put("data", method.invoke(service, data, permission, per.get("tokenid")));
                } else if (data instanceof List) {
                    Method method = service.getClass().getMethod(methodName, List.class, Map.class, String.class);
                    per.put("data", method.invoke(service, data, permission, per.get("tokenid")));
                } else if (data instanceof String) {
                    Method method = service.getClass().getMethod(methodName, String.class, Map.class, String.class);
                    per.put("data", method.invoke(service, data, permission, per.get("tokenid")));
                } else {
                    throw new RuntimeException("data type error!");
                }
            }

            if (tLogger.isDebugEnabled()) {
                Map para_tmp = new HashMap();
                para_tmp.put("startTime", startbfTime);
                para_tmp.put("endTime", System.currentTimeMillis());
                para_tmp.put("desc", "Action [" + "]");
                para_tmp.put("para", tempPer);
                tLogger.debug(para_tmp);
            }
            // -----------------------call action end----------------------------------------

            // -----------------------after filter start-------------------------------------
            if (logger.isDebugEnabled()) {
                logger.debug("action actionRPoolFilter :" + afterActionFilter);
            }

            startbfTime = System.currentTimeMillis();
            for (ActionResultFilter afilter : afterActionFilter.values()) {
                long startTime = System.currentTimeMillis();

                afr = afilter.doFilter(per);

                if (tLogger.isDebugEnabled()) {
                    Map para = new HashMap();
                    para.put("startTime", startTime);
                    long endTime = System.currentTimeMillis();
                    para.put("endTime", endTime);
                    para.put("desc", "after ActionFilter[" + afilter.getClass().getName() + "]" + afr);
                    try {
                        para.put("para", CloneTools.clone(per));
                    } catch (Throwable e) {
                        para.put("para", per);
                    }
                    tLogger.debug(para);
                }

                if (afr.getResult() == StatusResult._SUCCESS_OVER) {
                    per.put("status", afr.getResult());
                    per.put("msg", afr.getDescription());
                    return per;
                } else if (afr.getResult() == StatusResult._SUCCESS) {
                    continue;
                } else if (afr.getResult() < StatusResult._NoncriticalAbnormal) {
                    per.put("status", afr.getResult());
                    per.put(afr.getResult() <= StatusResult._permissionDenied ? "exception" : "msg", afr.getDescription());
                    return per;
                }
            }
            if (tLogger.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                Map para = new HashMap();
                para.put("startTime", startbfTime);
                para.put("endTime", endTime);
                para.put("desc", "Before ActionFilters [" + beforeActionFilter + "]");
                try {
                    para.put("para", CloneTools.clone(per));
                } catch (Throwable e) {
                    para.put("para", per);
                }
                tLogger.debug(para);
            }
            // -----------------------after filter start-------------------------------------

            if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
                logger.info("action gateway >>>" + (System.currentTimeMillis() - starttime) + "：" + per);
            }
            per.put("status", StatusResult._SUCCESS);
        } catch (Throwable e) {
            logger.error(ExceptionUtil.getCausedBySrcMsg(e), e);
            per.put("status", StatusResult._permissionDenied);
            per.put("exception", ExceptionUtil.getCausedBySrcMsg(e));
        }
        return per;
    }

    public String request(String perStr) {
        return JSONUtil.parserToStr(request(MapUtil.fromString(perStr)));
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
            throw new RuntimeException("index conflict! name=" + requestAction + ", old index is " + beforeFilterSort.get(index));
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
}
