package org.oiue.service.driver.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oiue.service.driver.api.Driver;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverFilterService;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.driver.api.DriverListenerService;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class DriverServiceImpl implements DriverService, DriverListener {
    private Logger logger;
    private HashMap<String, Driver> driverMap = new HashMap<String, Driver>();

    private List<DriverListenerService> driverListenerSList = new ArrayList<>();
    private Map<String, List<DriverListenerService>> driverListenerSMap = new HashMap<>();
    private Map<String, DriverListenerService> driverListener = new HashMap<String, DriverListenerService>();

    private List<DriverFilterService> driverFilterSList = new ArrayList<>();
    private Map<String, List<DriverFilterService>> driverFilterSMap = new HashMap<>();
    private Map<String, DriverFilterService> driverFilter = new HashMap<String, DriverFilterService>();

    public DriverServiceImpl(LogService logService) {
        logger = logService.getLogger(this.getClass());
        if (logger.isInfoEnabled()) {
            logger.info("driver service created");
        }
    }

    @Override
    public Driver getDriver(String driverName) {
        return driverMap.get(driverName);
    }

    @Override
    public boolean registerDriver(String driverName, Driver driver) {
        if (driverMap.containsKey(driverName)) {
            logger.error("register driver error, driver name is existed, driver name = " + driverName);
            return false;
        } else {
            logger.info("register driver successed, driver name = " + driverName);
            driverMap.put(driverName, driver);
            driver.registered(this);
            return true;
        }
    }

    @Override
    public void unregisterDriver(String driverName) {
        logger.info("unregister driver, driver name = " + driverName);
        Driver driver = driverMap.remove(driverName);
        if (driver != null) {
            driver.unregistered();
        }
        List<DriverFilterService> dfs = driverFilterSMap.remove(driver);
        if (dfs != null)
            for (DriverFilterService filterName : dfs) {
                this.unregisterFilterService(filterName);
            }

        List<DriverListenerService> dls = driverListenerSMap.remove(driver);
        if (dls != null)
            for (DriverListenerService listenerName : dls) {
                this.unregisterListenerService(listenerName);
            }
    }

    @Override
    public StatusResult receive(Map data) {
        StatusResult sr = null;
        if (logger.isDebugEnabled()) {
            logger.debug("data:" + data);
        }
        long startListener = 0l;
        if (logger.isDebugEnabled()) {
            startListener = System.currentTimeMillis();
        }
        String driverName = MapUtil.getString(data, DriverDataField.driverName);

        if (driverFilterSList != null) {
            for (DriverFilterService driverFilterService : driverFilterSList) {
                try {
                    sr = driverFilterService.receive(data);
                    if (logger.isDebugEnabled()) {
                        long longtime = System.currentTimeMillis() - startListener;
                        if (longtime > 3)
                            logger.debug(">>>>>>>>>> Filter time-consuming:" + longtime);
                    }

                    if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                        return sr;
                    }
                } catch (Throwable e) {
                    logger.error("receive filter service error, cancel receive[driverFilterSList] :" + driverFilterService.getClass().getName()+e.getMessage(),e);
                }
            }
        }

        List<DriverFilterService> driverFilterList = driverFilterSMap.get(driverName);
        if (driverFilterList != null) {
            for (DriverFilterService driverFilterService : driverFilterList) {
                try {
                    sr = driverFilterService.receive(data);
                    if (logger.isDebugEnabled()) {
                        long longtime = System.currentTimeMillis() - startListener;
                        if (longtime > 3)
                            logger.debug(">>>>>>>>>> Filter time-consuming:" + longtime);
                    }

                    if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                        return sr;
                    }
                } catch (Throwable e) {
                    logger.error("receive filter service error, cancel receive[driverFilterSMap] :" + driverFilterService.getClass().getName()+e.getMessage(),e);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("filter over>" + data);
        }

        if (driverListenerSList != null) {
            for (DriverListenerService driverListenerService : driverListenerSList) {
                Thread ld = new listenerListData(driverListenerService, data);
                ld.setName(listenerListData.class.getName());
                ld.start();
            }
        }
        List<DriverListenerService> driverListenerList = driverListenerSMap.get(driverName);
        if (driverListenerList != null) {
            for (DriverListenerService driverListenerService : driverListenerList) {
                new listenerListData(driverListenerService, data).start();
            }
        }
        sr = new StatusResult();
        sr.setResult(StatusResult._SUCCESS);
        return sr;
    }

    class listenerListData extends Thread {
        DriverListenerService e;
        Map data;

        public listenerListData(DriverListenerService driverListenerService, Map data) {
            this.e = driverListenerService;
            this.data = data;
        }

        public void run() {

            long startListener = 0l;
            if (logger.isDebugEnabled()) {
                startListener = System.currentTimeMillis();
            }
            try {
                StatusResult sr = e.receive(data);
                if(sr.getResult()<=StatusResult._pleaseLogin)
                    logger.error("receive error["+sr.getDescription()+"]:e="+e+",data="+data);
            } catch (Throwable ex) {
                logger.error("driver name listener receive error:" + e.getClass().getName() + "|" + ex.getMessage(), ex);
            }
            if (logger.isDebugEnabled()) {
                long longtime = System.currentTimeMillis() - startListener;
                if (longtime > 3)
                    logger.debug(">>>>>>" + e.getClass().getName() + ":" + longtime);
            }
        }
    }

    @Override
    public void unregisterAllDriver() {
        logger.info("unregister all driver");
        for (Driver e : driverMap.values()) {
            e.unregistered();
        }
        driverMap.clear();
        driverListenerSList.clear();
        driverListenerSMap.clear();
        driverListener.clear();
        driverFilterSList.clear();
        driverFilterSMap.clear();
        driverFilter.clear();
    }

    @Override
    public StatusResult send(Map data) {

        StatusResult sr = null;
        if (logger.isDebugEnabled()) {
            logger.debug("send before filter data = " + data.toString());
        }

        long startListener = 0l;
        if (logger.isDebugEnabled()) {
            startListener = System.currentTimeMillis();
        }

        String driverName = MapUtil.getString(data, DriverDataField.driverName);
        if (driverFilterSList != null) {
            for (DriverFilterService driverFilterService : driverFilterSList) {
                try {
                    sr = driverFilterService.send(data);
                    if (logger.isDebugEnabled()) {
                        long longtime = System.currentTimeMillis() - startListener;
                        if (longtime > 3)
                            logger.debug(">>>>>>>>>> Filter time-consuming:" + longtime);
                    }

                    if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                        return sr;
                    }
                } catch (Throwable e) {
                    logger.error("send filter error, cancel receive[driverFilterSList] :" + driverFilterService.getClass().getName());
                }
            }
        }
        List<DriverFilterService> driverFilterList = driverFilterSMap.get(driverName);
        if (driverFilterList != null) {
            for (DriverFilterService driverFilterService : driverFilterList) {
                try {
                    sr = driverFilterService.send(data);
                    if (logger.isDebugEnabled()) {
                        long longtime = System.currentTimeMillis() - startListener;
                        if (longtime > 3)
                            logger.debug(">>>>>>>>>> Filter time-consuming:" + longtime);
                    }

                    if (sr.getResult() <= StatusResult._pleaseLogin || sr.getResult() == StatusResult._SUCCESS_OVER) {
                        return sr;
                    }
                } catch (Throwable e) {
                    logger.error("send filter error, cancel receive[driverFilterSMap] :" + driverFilterService.getClass().getName());
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("send after filter data = " + data.toString());
        }
        Driver driver = driverMap.get(driverName);

        if (driver == null) {
            sr = new StatusResult();
            logger.warn("send error, can't get driver from driver name, driver name = " + driverName);
            sr.setResult(StatusResult._ncriticalAbnormal);
            sr.setDescription("send error, can't get driver from driver name, driver name = " + driverName);
            return sr;
        }

        return driver.send(data);
    }

    @Override
    public int registerFilterService(DriverFilterService filter, String driverName) {
        driverFilter.put(filter.getFilterName(), filter);
        if (driverName == null) {
            driverFilterSList.add(filter);
            sortFilter(driverFilterSList);
        } else {
            List<DriverFilterService> driverFilterList = driverFilterSMap.get(driverName);
            if (driverFilterList == null) {
                driverFilterList = new ArrayList<>();
                driverFilterSMap.put(driverName, driverFilterList);
            }
            driverFilterList.add(filter);
            sortFilter(driverFilterList);
        }

        return 0;
    }

    private void sortFilter(List<DriverFilterService> filterList) {
        Collections.sort(filterList, new Comparator<DriverFilterService>() {
            @Override
            public int compare(DriverFilterService o1, DriverFilterService o2) {
                if (o1.getPriority() > o2.getPriority()) {
                    return 1;
                } else if (o1.getPriority() < o2.getPriority()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    @Override
    public void unregisterFilterService(String filterName) {
        this.unregisterFilterService(driverFilter.get(filterName));
    }

    @Override
    public boolean registerListenerService(DriverListenerService listener, String driverName) {
        driverListener.put(listener.getListenerName(), listener);
        if (driverName == null) {
            driverListenerSList.add(listener);
        } else {
            List<DriverListenerService> driverListenerList = driverListenerSMap.get(driverName);
            if (driverListenerList == null) {
                driverListenerList = new ArrayList<>();
                driverListenerSMap.put(driverName, driverListenerList);
            }
            driverListenerList.add(listener);
        }
        return true;
    }

    @Override
    public void unregisterListenerService(String listenerName) {
        this.unregisterListenerService(driverListener.get(listenerName));
    }

    public void updated(Dictionary<String, ?> props)  {
        logger.info("update property");
    }

    @Override
    public void unregisterFilterService(DriverFilterService filter) {
        try {
            for (List<DriverFilterService> dfsl : driverFilterSMap.values()) {
                try {
                    dfsl.remove(filter);
                } catch (Exception e) {
                    logger.error("remove filter service is error[0]:" + e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            logger.error("remove filter service is error[1]:" + e.getMessage(), e);
        }
        try {
            driverFilterSList.remove(filter);
        } catch (Exception e) {
            logger.error("remove filter service is error[2]:" + e.getMessage(), e);
        }
        try {
            driverFilter.remove(filter);
        } catch (Exception e) {
            logger.error("remove filter service is error[3]:" + e.getMessage(), e);
        }
    }

    @Override
    public void unregisterListenerService(DriverListenerService listener) {
        try {
            for (List<DriverListenerService> dlsl : driverListenerSMap.values()) {
                try {
                    dlsl.remove(listener);
                } catch (Exception e) {
                    logger.error("remove listener service is error[0]:" + e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            logger.error("remove listener service is error[1]:" + e.getMessage(), e);
        }
        try {
            driverListenerSList.remove(listener);
        } catch (Exception e) {
            logger.error("remove listener service is error[2]:" + e.getMessage(), e);
        }
        try {
            driverListener.remove(listener);
        } catch (Exception e) {
            logger.error("remove listener service is error[3]:" + e.getMessage(), e);
        }
    }
}
