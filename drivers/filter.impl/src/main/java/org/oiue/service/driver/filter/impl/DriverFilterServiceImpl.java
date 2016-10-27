package org.oiue.service.driver.filter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverFilter;
import org.oiue.service.driver.api.DriverFilterService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DriverFilterServiceImpl implements DriverFilterService {
    private static final long serialVersionUID = 1L;

    private ArrayList<DriverFilter> receiveFilterList = new ArrayList<DriverFilter>();
    private HashMap<String, ArrayList<DriverFilter>> receiveFilterMap = new HashMap<String, ArrayList<DriverFilter>>();

    private ArrayList<DriverFilter> sendFilterList = new ArrayList<DriverFilter>();
    private HashMap<String, ArrayList<DriverFilter>> sendFilterMap = new HashMap<String, ArrayList<DriverFilter>>();

    private Logger logger;
    private Dictionary props;


    public DriverFilterServiceImpl(LogService logService) {
        logger = logService.getLogger(this.getClass());
    }

    private void sortFilter(ArrayList<DriverFilter> filterList) {
        if (filterList != null && filterList.size() > 1)
            Collections.sort(filterList, new Comparator<DriverFilter>() {
                @Override
                public int compare(DriverFilter o1, DriverFilter o2) {
                    if (o1.getPriority() > o2.getPriority()) {
                        return 1;
                    } else if (o1.getPriority() < getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
    }

    @Override
    public boolean registerReceiveFilter(DriverFilter filter, int priority) {
        if (props != null) {
            Object priorityProperty = props.get("receive." + filter.getClass().getName());
            if (priorityProperty != null) {
                try {
                    priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace priority with property, filter = " + filter.getClass().getName() + ", from " + priority + ", to " + priorityProperty);
                } catch (Exception e) {
                    logger.warn("replace priority with property, filter = " + filter.getClass().getName(), e);
                }
            }
        }
        filter.setPriority(priority);
        if (logger.isInfoEnabled()) {
            logger.info("register receive filter, filter = " + filter);
        }
        if (receiveFilterList.contains(filter)) {
            logger.error("register receive filter error, filter already exits, filter = " + filter);
            return false;
        } else {
            receiveFilterList.add(filter);
            if (logger.isInfoEnabled()) {
                logger.info("sort receive filter by priority, filter count = " + receiveFilterList.size());
            }
            sortFilter(receiveFilterList);
            if (logger.isInfoEnabled()) {
                logger.info("register receive filter successed, filter = " + filter);
            }
            return true;
        }
    }

    @Override
    public boolean registerReceiveFilter(String driverName, DriverFilter filter, int priority) {
        if (props != null) {
            Object priorityProperty = props.get("receive." + filter.getClass().getName());
            if (priorityProperty != null) {
                try {
                    priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace priority with property, filter = " + filter.getClass().getName() + ", from " + priority + ", to " + priorityProperty);
                } catch (Exception e) {
                    logger.warn("replace priority with property, filter = " + filter.getClass().getName(), e);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("register receive filter, driver name = " + driverName + ", filter = " + filter);
        }
        ArrayList<DriverFilter> receiveFilterList = receiveFilterMap.get(driverName);
        if (receiveFilterList == null) {
            receiveFilterList = new ArrayList<DriverFilter>();
            receiveFilterMap.put(driverName, receiveFilterList);
        } else {
            if (receiveFilterList.contains(filter)) {
                logger.error("register filter error, filter already exits, driver name = " + driverName + ", filter = " + filter);
                return false;
            }
        }
        receiveFilterList.add(filter);
        if (logger.isInfoEnabled()) {
            logger.info("sort receive filter by priority, drivar name = " + driverName + ", filter count = " + receiveFilterList.size());
        }
        sortFilter(receiveFilterList);
        if (logger.isInfoEnabled()) {
            logger.info("register receive filter successed, driver name = " + driverName + ", filter = " + filter);
        }
        return true;
    }

    @Override
    public boolean registerSendFilter(DriverFilter filter, int priority) {
        if (props != null) {
            Object priorityProperty = props.get("send." + filter.getClass().getName());
            if (priorityProperty != null) {
                try {
                    priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace priority with property, filter = " + filter.getClass().getName() + ", from " + priority + ", to " + priorityProperty);
                } catch (Exception e) {
                    logger.warn("replace priority with property, filter = " + filter.getClass().getName(), e);
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("register send filter, filter = " + filter);
        }
        if (sendFilterList.contains(filter)) {
            logger.error("register send filter error, filter already exits, filter = " + filter);
            return false;
        } else {
            sendFilterList.add(filter);
            if (logger.isInfoEnabled()) {
                logger.info("sort send filter by priority, filter count = " + sendFilterList.size());
            }
            sortFilter(sendFilterList);
            if (logger.isInfoEnabled()) {
                logger.info("register send filter successed, filter = " + filter);
            }
            return true;
        }
    }

    @Override
    public boolean registerSendFilter(String driverName, DriverFilter filter, int priority) {
        if (props != null) {
            Object priorityProperty = props.get("send." + filter.getClass().getName());
            if (priorityProperty != null) {
                try {
                    priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace priority with property, filter = " + filter.getClass().getName() + ", from " + priority + ", to " + priorityProperty);
                } catch (Exception e) {
                    logger.warn("replace priority with property, filter = " + filter.getClass().getName(), e);
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("register send filter, driver name = " + driverName + ", filter = " + filter);
        }
        ArrayList<DriverFilter> sendFilterList = sendFilterMap.get(driverName);
        if (sendFilterList == null) {
            sendFilterList = new ArrayList<DriverFilter>();
            sendFilterMap.put(driverName, sendFilterList);
        } else {
            if (sendFilterList.contains(filter)) {
                logger.error("register filter error, filter already exits, driver name = " + driverName + ", filter = " + filter);
                return false;
            }
        }

        sendFilterList.add(filter);
        if (logger.isInfoEnabled()) {
            logger.info("sort receive filter by priority, drivar name = " + driverName + ", filter count = " + sendFilterList.size());
        }
        sortFilter(sendFilterList);
        if (logger.isInfoEnabled()) {
            logger.info("register receive filter successed, driver name = " + driverName + ", filter = " + filter);
        }
        return true;
    }

    @Override
    public void unregisterAllReceiveFilter() {
        if (logger.isInfoEnabled()) {
            logger.info("unregister all receive filter");
        }
        receiveFilterList.clear();
        receiveFilterMap.clear();
    }

    @Override
    public void unregisterAllSendFilter() {
        if (logger.isInfoEnabled()) {
            logger.info("unregister all send filter");
        }
        sendFilterList.clear();
        sendFilterMap.clear();
    }

    @Override
    public void unregisterReceiveFilter(DriverFilter filter) {
        if (logger.isInfoEnabled()) {
            logger.info("unregister receive filter, filter = " + filter);
        }
        receiveFilterList.remove(filter);
    }

    @Override
    public void unregisterReceiveFilter(String driverName, DriverFilter filter) {
        if (logger.isInfoEnabled()) {
            logger.info("unregister driver receive filter, driver name = " + driverName + ", filter = " + filter);
        }
        ArrayList<DriverFilter> receiveFilterList = receiveFilterMap.get(driverName);
        if (receiveFilterList != null) {
            receiveFilterList.remove(filter);
        }
    }

    @Override
    public void unregisterSendFilter(DriverFilter filter) {
        if (logger.isInfoEnabled()) {
            logger.info("unregister send filter, filter = " + filter);
        }
        sendFilterList.remove(filter);
    }

    @Override
    public void unregisterSendFilter(String driverName, DriverFilter filter) {
        if (logger.isInfoEnabled()) {
            logger.info("unregister driver send filter, driver name = " + driverName + ", filter = " + filter);
        }
        ArrayList<DriverFilter> sendFilterList = sendFilterMap.get(driverName);
        if (sendFilterList != null) {
            sendFilterList.remove(filter);
        }
    }

    @Override
    public StatusResult receive(Map data) {
        long startListener = 0l;
        StatusResult sr = new StatusResult();
        sr.setResult(StatusResult._SUCCESS);
        for (DriverFilter e : receiveFilterList) {
            try {
                if (logger.isDebugEnabled()) {
                    startListener = System.currentTimeMillis();
                }
                sr = e.doFilter(data);
                if (logger.isDebugEnabled()) {
                    long longtime = System.currentTimeMillis() - startListener;
                    if (longtime > 3)
                        logger.debug(">>>>>>>>>> Filter time-consuming:" + e.getClass().getName() + ":" + longtime);
                }
            } catch (Throwable ex) {
                String classsname = "";
                try {
                    classsname = e.getClass().getName();
                } catch (Throwable e2) {
                    logger.error("receive filter error, filter is null", e2);
                }
                logger.error("receive filter error[" + classsname + "]" + ex.getMessage(), ex);
                sr = new StatusResult();
                sr.setResult(StatusResult._ncriticalAbnormal);
                sr.setDescription("receive filter error[" + classsname + "]" + ex.getMessage());
                return sr;
            }
            if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                return sr;
            }
        }

        ArrayList<DriverFilter> receiveFilterList = receiveFilterMap.get(MapUtil.getString(data, DriverDataField.driverName));
        if (receiveFilterList != null) {
            for (DriverFilter e : receiveFilterList) {
                try {
                    if (logger.isDebugEnabled()) {
                        startListener = System.currentTimeMillis();
                    }
                    sr = e.doFilter(data);
                    if (logger.isDebugEnabled()) {
                        long longtime = System.currentTimeMillis() - startListener;
                        if (longtime > 3)
                            logger.debug(">>>>>>>>>> Filter time-consuming:" + e.getClass().getName() + ":" + longtime);
                    }
                } catch (Throwable ex) {
                    String classsname = "";
                    try {
                        classsname = e.getClass().getName();
                    } catch (Throwable e2) {
                        logger.error("receive filter error, filter is null", e2);

                    }
                    logger.error("receive filter error[" + classsname + "]" + ex.getMessage(), ex);
                    sr = new StatusResult();
                    sr.setResult(StatusResult._ncriticalAbnormal);
                    sr.setDescription("receive filter error[" + classsname + "]" + ex.getMessage());
                    return sr;
                }
                if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                    return sr;
                }
            }
        }
        return sr;
    }

    @Override
    public StatusResult send(Map data) {
        StatusResult sr = null;
        for (DriverFilter e : sendFilterList) {
            try {
                sr = e.doFilter(data);
            } catch (Throwable ex) {
                String classsname = "";
                try {
                    classsname = e.getClass().getName();
                } catch (Throwable e2) {
                    logger.error("send filter error, filter is null", e2);
                }
                logger.error("send filter error[" + classsname + "]" + ex.getMessage(), ex);
                sr = new StatusResult();
                sr.setResult(StatusResult._ncriticalAbnormal);
                data.put(DriverDataField.STATUS, sr);
                data.put(DriverDataField.DESCRIPTION, "send filter error[" + classsname + "]" + ex.getMessage());
                return sr;
            }
            if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                return sr;
            }
        }

        ArrayList<DriverFilter> sendFilterList = sendFilterMap.get(MapUtil.getString(data, DriverDataField.driverName));
        if (sendFilterList != null) {
            for (DriverFilter e : sendFilterList) {
                try {
                    sr = e.doFilter(data);
                } catch (Throwable ex) {
                    String classsname = "";
                    try {
                        classsname = e.getClass().getName();
                    } catch (Throwable e2) {
                        logger.error("send filter error, filter is null", e2);
                    }
                    logger.error("send filter error[" + classsname + "]" + ex.getMessage(), ex);
                    sr = new StatusResult();
                    sr.setResult(StatusResult._ncriticalAbnormal);
                    sr.setDescription("send filter error[" + classsname + "]" + ex.getMessage());
                    return sr;
                }
                if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                    return sr;
                }
            }
        }
        return sr;
    }

    public void updated(Dictionary<String, ?> props) {
        logger.info("update property");
        this.props = props;

        if (props == null) {
            return;
        }

        for (DriverFilter filterWarpper : receiveFilterList) {
            Object priorityProperty = props.get("receive." + filterWarpper.getClass().getName());
            if (priorityProperty != null) {
                try {
                    int priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace receive filter property, filter = " + filterWarpper.getClass().getName() + ", priority = " + priority);
                } catch (Exception e) {
                    logger.warn("replace receive filter property, filter = " + filterWarpper.getClass().getName(), e);
                }

            }
        }
        sortFilter(receiveFilterList);

        for (String driverName : receiveFilterMap.keySet()) {
            ArrayList<DriverFilter> receiveFilterList = receiveFilterMap.get(driverName);
            for (DriverFilter filterWarpper : receiveFilterList) {
                Object priorityProperty = props.get("receive." + filterWarpper.getClass().getName());
                if (priorityProperty != null) {
                    try {
                        int priority = Integer.parseInt((String) priorityProperty);
                        logger.info("replace receive filter property, driver = " + driverName + ", filter = " + filterWarpper.getClass().getName() + ", priority = " + priority);
                    } catch (Exception e) {
                        logger.warn("replace receive filter property, driver = " + driverName + ", filter = " + filterWarpper.getClass().getName(), e);
                    }

                }
            }
            sortFilter(receiveFilterList);
        }

        for (DriverFilter filterWarpper : sendFilterList) {
            Object priorityProperty = props.get("send." + filterWarpper.getClass().getName());
            if (priorityProperty != null) {
                try {
                    int priority = Integer.parseInt((String) priorityProperty);
                    logger.info("replace send property, filter = " + filterWarpper.getClass().getName() + ", priority = " + priority);
                } catch (Exception e) {
                    logger.warn("replace send property, filter = " + filterWarpper.getClass().getName(), e);
                }

            }
        }
        sortFilter(sendFilterList);

        for (String driverName : sendFilterMap.keySet()) {
            ArrayList<DriverFilter> sendFilterList = receiveFilterMap.get(driverName);
            for (DriverFilter filterWarpper : sendFilterList) {
                Object priorityProperty = props.get("send." + filterWarpper.getClass().getName());
                if (priorityProperty != null) {
                    try {
                        int priority = Integer.parseInt((String) priorityProperty);
                        logger.info("replace send property, driver = " + driverName + ", filter = " + filterWarpper.getClass().getName() + ", priority = " + priority);
                    } catch (Exception e) {
                        logger.warn("replace send property, driver = " + driverName + ", filter = " + filterWarpper.getClass().getName(), e);
                    }
                }
            }
            sortFilter(sendFilterList);
        }

    }

    @Override
    public void unregisterReceiveFilter(String driverName) {
        List<DriverFilter> receiveFilterList = receiveFilterMap.get(driverName);
        for (Iterator iterator = receiveFilterList.iterator(); iterator.hasNext();) {
            DriverFilter driverFilter = (DriverFilter) iterator.next();
            this.unregisterReceiveFilter(driverFilter);
        }
    }

    @Override
    public void unregisterSendFilter(String driverName) {
        List<DriverFilter> sendFilterList = sendFilterMap.get(driverName);
        for (Iterator iterator = sendFilterList.iterator(); iterator.hasNext();) {
            DriverFilter driverFilter = (DriverFilter) iterator.next();
            this.unregisterSendFilter(driverFilter);
        }
    }

    int priority = 10;

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getFilterName() {
        return "FilterName";
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
