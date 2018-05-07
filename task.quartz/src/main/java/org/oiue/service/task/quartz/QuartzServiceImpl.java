package org.oiue.service.task.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.task.Task;
import org.oiue.service.task.TaskService;
import org.oiue.tools.string.StringUtil;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class QuartzServiceImpl implements TaskService {
	public static String QUARTZ_JOB = "QUARTZ_JOB";
	public static String QUARTZ_JOB_CONTEXT = "QUARTZ_JOB_CONTEXT";
	
	private static String QUARTZ_TRIGGER_GROUP = "QUARTZ_TRIGGER_GROUP";
	
	private Scheduler scheduler;
	private Logger logger;
	
	public QuartzServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
		
		logger.info("quartz service starting...");
		QuartzJob.logger = logService.getLogger(QuartzJob.class);
		
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		try {
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("start quartz scheduler error.", e);
		}
		
		logger.info("quartz service started.");
	}
	
	@Override
	public boolean registerCron(String name, String group, String cron, Task job, Map context) {
		if (logger.isDebugEnabled()) {
			logger.debug("register cron name = " + name + ", cron = " + cron + ", quartzJob = " + job + ", context = " + context);
		}
		if (StringUtil.isEmptys(group)) {
			group = QUARTZ_TRIGGER_GROUP;
		}
		JobDataMap newJobDataMap = new JobDataMap(context);
		newJobDataMap.put(QUARTZ_JOB, job);
		
		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(name, group).usingJobData(newJobDataMap).build();
		
		TriggerKey key = new TriggerKey(name, group);
		Trigger trigger = newTrigger().withIdentity(key).withSchedule(cronSchedule(cron)).build();
		
		try {
			// 添加job，以及其关联的trigger
			scheduler.scheduleJob(jobDetail, trigger);
			return true;
		} catch (Throwable e) {
			logger.error("register task is error:" + e.getMessage(), e);
			return false;
		}
	}
	
	@Override
	public boolean registerSimple(String name, String group, Date start, int interval, int repeat, Date end, Task job, Map context) {
		if (logger.isDebugEnabled()) {
			logger.debug("register simple name = " + name + ", start = " + start + ", interval = " + interval + ", repeat = " + repeat + ", quartzJob = " + job + ", context = " + context);
		}
		if (StringUtil.isEmptys(group)) {
			group = QUARTZ_TRIGGER_GROUP;
		}
		
		JobDataMap newJobDataMap = new JobDataMap(context);
		newJobDataMap.put(QUARTZ_JOB, job);
		
		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(name, group).usingJobData(newJobDataMap).build();
		
		TriggerKey key = new TriggerKey(name, group);
		SimpleTrigger trigger = newTrigger().withIdentity(key).startAt(start).endAt(end).withSchedule(simpleSchedule().withIntervalInSeconds(interval).withRepeatCount(repeat)).build();
		
		try {
			// 添加job，以及其关联的trigger
			scheduler.scheduleJob(jobDetail, trigger);
			return true;
		} catch (Throwable e) {
			logger.error("register task is error:" + e.getMessage(), e);
			return false;
		}
	}
	
	@Override
	public void unregister(String name, String group) {
		if (logger.isDebugEnabled()) {
			logger.debug("unregister quartz name = " + name + ",quartz group = " + group);
		}
		if (StringUtil.isEmptys(group)) {
			group = QUARTZ_TRIGGER_GROUP;
		}
		try {
			scheduler.unscheduleJob(new TriggerKey(name, group));
		} catch (SchedulerException e) {
			logger.error("unregister error", e);
		}
	}
	
	public void shutdown() {
		logger.info("quartz service stoping...");
		try {
			scheduler.shutdown(true);
		} catch (SchedulerException e) {
			logger.error("stop error", e);
		}
		logger.info("quartz service stoped");
	}
	
	@Override
	public boolean registerSimple(String name, Date start, int interval, int repeat, Date end, Task job, Map context) {
		return this.registerSimple(name, null, start, interval, repeat, end, job, context);
	}
	
	@Override
	public boolean registerCron(String name, String cron, Task job, Map context) {
		return this.registerCron(name, null, cron, job, context);
	}
	
	@Override
	public void unregister(String name) {
		this.unregister(name, null);
	}
	
}
