package org.oiue.service.task;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface TaskService extends Serializable {
	
	boolean registerSimple(String name, String group, Date start, int interval, int repeat, Date end, Task job, Map context);
	
	boolean registerSimple(String name, Date start, int interval, int repeat, Date end, Task job, Map context);
	
	boolean registerCron(String name, String group, String cron, Task job, Map context);
	
	boolean registerCron(String name, String cron, Task job, Map context);
	
	void unregister(String name, String group);
	
	void unregister(String name);
	
}
