package org.oiue.service.driver.listener.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.driver.api.DriverListenerService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unused" })
public class DriverListenerServiceImpl implements DriverListenerService {
	private static final long serialVersionUID = 1L;
	
	private List<DriverListener> receiveListenerList = new ArrayList<>();
	private HashMap<String, List<DriverListener>> receiveListenerMap = new HashMap<>();
	
	private Logger logger;
	private Dictionary props;
	
	public DriverListenerServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public String getListenerName() {
		return "DriverListener";
	}
	
	@Override
	public boolean registerListener(DriverListener listener) {
		receiveListenerList.add(listener);
		return false;
	}
	
	@Override
	public boolean registerListener(String driverName, DriverListener listener) {
		if (StringUtil.isEmptys(driverName)) {
			this.registerListener(listener);
		} else {
			List<DriverListener> receiveListenerList = receiveListenerMap.get(driverName);
			if (receiveListenerList == null) {
				receiveListenerList = new ArrayList<>();
				receiveListenerMap.put(driverName, receiveListenerList);
			}
			receiveListenerList.add(listener);
		}
		return false;
	}
	
	@Override
	public void unregisterListener(DriverListener listener) {
		receiveListenerList.remove(listener);
		for (Iterator iterator = receiveListenerMap.values().iterator(); iterator.hasNext();) {
			List driverListeners = (List) iterator.next();
			if (driverListeners != null) {
				driverListeners.remove(listener);
			}
		}
	}
	
	@Override
	public void unregisterListener(String listenerName) {
		
	}
	
	@Override
	public void unregisterAllListener() {
		receiveListenerList.clear();
		receiveListenerMap.clear();
	}
	
	@Override
	public void unregisterAllListener(String driverName) {
		List<DriverListener> receiveListenerList = receiveListenerMap.get(driverName);
		if (receiveListenerList != null) {
			for (Iterator iterator = receiveListenerList.iterator(); iterator.hasNext();) {
				iterator.remove();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public StatusResult receive(Map data) {
		StatusResult sr = null;
		if (data == null) {
			sr = new StatusResult();
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("data con not null");
			return sr;
		}
		String driverName = MapUtil.getString(data, DriverDataField.driverName);
		if (StringUtil.isEmptys(driverName)) {
			if (logger.isWarnEnabled())
				logger.warn("driverName is null:" + data);
		} else {
			List<DriverListener> receiveListenerList = receiveListenerMap.get(driverName);
			if (receiveListenerList != null) {
				for (Iterator iterator = receiveListenerList.iterator(); iterator.hasNext();) {
					DriverListener driverListener = (DriverListener) iterator.next();
					Thread ld = new listenerListData(driverListener, data);
					ld.setName(ld.getClass().getName());
					ld.start();
				}
			}
			
			for (Iterator iterator = this.receiveListenerList.iterator(); iterator.hasNext();) {
				DriverListener driverListener = (DriverListener) iterator.next();
				new listenerListData(driverListener, data).start();
			}
		}
		sr = new StatusResult();
		sr.setResult(StatusResult._SUCCESS);
		return sr;
	}
	
	class listenerListData extends Thread {
		DriverListener e;
		Map data;
		
		public listenerListData(DriverListener e, Map data) {
			this.e = e;
			this.data = data;
		}
		
		public void run() {
			
			long startListener = 0l;
			if (logger.isDebugEnabled()) {
				startListener = System.currentTimeMillis();
			}
			try {
				StatusResult sr = e.receive(data);
				if (sr.getResult() <= StatusResult._pleaseLogin)
					logger.error("[" + e.getClass().getName() + "]receive error:" + sr.getDescription());
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
	
	public void updated(Dictionary<String, ?> props) {
		this.props = props;
	}
}
