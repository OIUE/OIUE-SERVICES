package org.oiue.service.driver.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface DriverFilterService extends Serializable{
   int getPriority();
   void setPriority(int priority);
   String getFilterName();
    
   boolean registerReceiveFilter(DriverFilter filter, int priority);

   boolean registerReceiveFilter(String driverName, DriverFilter filter, int priority);

   void unregisterReceiveFilter(DriverFilter filter);

   void unregisterReceiveFilter(String driverName, DriverFilter filter);

   void unregisterReceiveFilter(String driverName);

   void unregisterAllReceiveFilter();

   boolean registerSendFilter(DriverFilter filter, int priority);

   boolean registerSendFilter(String driverName, DriverFilter filter, int priority);

   void unregisterSendFilter(DriverFilter filter);

   void unregisterSendFilter(String driverName, DriverFilter filter);

   void unregisterSendFilter(String driverName);

   void unregisterAllSendFilter();

   StatusResult receive(Map data);

   StatusResult send(Map data);
}
