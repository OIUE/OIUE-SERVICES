package org.oiue.service.event.etl.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.KettleLogLayout;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

public class JobExecutor implements Runnable {
	
	private String executionId;
	private JobExecutionConfiguration executionConfiguration;
	private JobMeta jobMeta = null;
	private Job job = null;
	private static final Class PKG = JobExecutor.class;
	private Repository repository;
	private DelegatingMetaStore metaStore;
	private Logger logger;
	
	private JobExecutor(JobExecutionConfiguration executionConfiguration, JobMeta jobMeta, Repository repository, DelegatingMetaStore metaStore, LogService logService) {
		this.executionId = UUID.randomUUID().toString().replaceAll("-", "");
		this.executionConfiguration = executionConfiguration;
		this.jobMeta = jobMeta;
		this.repository = repository;
		this.metaStore = metaStore;
		this.logger = logService.getLogger(this.getClass());
	}
	
	private static Hashtable<String, JobExecutor> executors = new Hashtable<String, JobExecutor>();
	
	public static synchronized JobExecutor initExecutor(JobExecutionConfiguration executionConfiguration, JobMeta jobMeta, Repository repository, DelegatingMetaStore metaStore, LogService logService) {
		JobExecutor jobExecutor = new JobExecutor(executionConfiguration, jobMeta, repository, metaStore, logService);
		executors.put(jobExecutor.getExecutionId(), jobExecutor);
		return jobExecutor;
	}
	
	public String getExecutionId() {
		return executionId;
	}
	
	private boolean finished = false;
	private long errCount = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		try {
			for (String varName : executionConfiguration.getVariables().keySet()) {
				String varValue = executionConfiguration.getVariables().get(varName);
				jobMeta.setVariable(varName, varValue);
			}
			
			for (String paramName : executionConfiguration.getParams().keySet()) {
				String paramValue = executionConfiguration.getParams().get(paramName);
				jobMeta.setParameterValue(paramName, paramValue);
			}
			
			if (executionConfiguration.isExecutingLocally()) {
				SimpleLoggingObject spoonLoggingObject = new SimpleLoggingObject("SPOON", LoggingObjectType.SPOON, null);
				spoonLoggingObject.setContainerObjectId(executionId);
				spoonLoggingObject.setLogLevel(executionConfiguration.getLogLevel());
				job = new Job(repository, jobMeta, spoonLoggingObject);
				
				job.setLogLevel(executionConfiguration.getLogLevel());
				job.shareVariablesWith(jobMeta);
				job.setInteractive(true);
				job.setGatheringMetrics(executionConfiguration.isGatheringMetrics());
				job.setArguments(executionConfiguration.getArgumentStrings());
				
				job.getExtensionDataMap().putAll(executionConfiguration.getExtensionOptions());
				
				// If there is an alternative start job entry, pass it to the job
				//
				if (!Const.isEmpty(executionConfiguration.getStartCopyName())) {
					JobEntryCopy startJobEntryCopy = jobMeta.findJobEntry(executionConfiguration.getStartCopyName(), executionConfiguration.getStartCopyNr(), false);
					job.setStartJobEntryCopy(startJobEntryCopy);
				}
				
				// Set the named parameters
				Map<String, String> paramMap = executionConfiguration.getParams();
				Set<String> keys = paramMap.keySet();
				for (String key : keys) {
					job.getJobMeta().setParameterValue(key, Const.NVL(paramMap.get(key), ""));
				}
				job.getJobMeta().activateParameters();
				
				job.start();
				
				while (!job.isFinished()) {
					Thread.sleep(500);
				}
				
				errCount = job.getErrors();
			} else if (executionConfiguration.isExecutingRemotely()) {
				carteObjectId = Job.sendToSlaveServer(jobMeta, executionConfiguration, repository, metaStore);
				SlaveServer remoteSlaveServer = executionConfiguration.getRemoteServer();
				
				boolean running = true;
				while (running) {
					SlaveServerJobStatus jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), carteObjectId, 0);
					running = jobStatus.isRunning();
					if (!running)
						errCount = jobStatus.getResult().getNrErrors();
					Thread.sleep(500);
				}
			}
			
		} catch (Exception e) {
			logger.error("执行失败！", e);
		} finally {
			finished = true;
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public long getErrCount() {
		return errCount;
	}
	
	private String carteObjectId = null;
	
	public int previousNrItems;
	
	@SuppressWarnings("deprecation")
	public List getJobMeasure() {
		List jsonArray = new ArrayList<>();
		if (executionConfiguration.isExecutingLocally()) {
			JobTracker jobTracker = job.getJobTracker();
			int nrItems = jobTracker.getTotalNumberOfItems();
			if (nrItems != previousNrItems) {
				// Re-populate this...
				String jobName = jobTracker.getJobName();
				
				if (Const.isEmpty(jobName)) {
					if (!Const.isEmpty(jobTracker.getJobFilename())) {
						jobName = jobTracker.getJobFilename();
					} else {
						jobName = BaseMessages.getString(PKG, "JobLog.Tree.StringToDisplayWhenJobHasNoName");
					}
				}
				
				Map jsonObject = new HashMap();
				jsonObject.put("name", jobName);
				jsonObject.put("expanded", true);
				
				List children = new ArrayList<>();
				for (int i = 0; i < jobTracker.nrJobTrackers(); i++) {
					Map jsonObject2 = addTrackerToTree(jobTracker.getJobTracker(i));
					if (jsonObject2 != null)
						children.add(jsonObject2);
				}
				jsonObject.put("children", children);
				jsonArray.add(jsonObject);
				
				previousNrItems = nrItems;
			}
		}
		return jsonArray;
	}
	
	@SuppressWarnings("deprecation")
	private Map addTrackerToTree(JobTracker jobTracker) {
		Map jsonObject = new HashMap<>();
		if (jobTracker != null) {
			if (jobTracker.nrJobTrackers() > 0) {
				// This is a sub-job: display the name at the top of the list...
				jsonObject.put("name", BaseMessages.getString(PKG, "JobLog.Tree.JobPrefix") + jobTracker.getJobName());
				jsonObject.put("expanded", true);
				List children = new ArrayList<>();
				// then populate the sub-job entries ...
				for (int i = 0; i < jobTracker.nrJobTrackers(); i++) {
					Map jsonObject2 = addTrackerToTree(jobTracker.getJobTracker(i));
					if (jsonObject2 != null)
						children.add(jsonObject2);
				}
				jsonObject.put("children", children);
			} else {
				JobEntryResult result = jobTracker.getJobEntryResult();
				if (result != null) {
					String jobEntryName = result.getJobEntryName();
					if (!Const.isEmpty(jobEntryName)) {
						jsonObject.put("name", jobEntryName);
						jsonObject.put("fileName", Const.NVL(result.getJobEntryFilename(), ""));
					} else {
						jsonObject.put("name", BaseMessages.getString(PKG, "JobLog.Tree.JobPrefix2") + jobTracker.getJobName());
					}
					String comment = result.getComment();
					if (comment != null) {
						jsonObject.put("comment", comment);
					}
					Result res = result.getResult();
					if (res != null) {
						jsonObject.put("result", res.getResult() ? BaseMessages.getString(PKG, "JobLog.Tree.Success") : BaseMessages.getString(PKG, "JobLog.Tree.Failure"));
						jsonObject.put("number", Long.toString(res.getEntryNr()));
					}
					String reason = result.getReason();
					if (reason != null) {
						jsonObject.put("reason", reason);
					}
					Date logDate = result.getLogDate();
					if (logDate != null) {
						jsonObject.put("logDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(logDate));
					}
					jsonObject.put("leaf", true);
				} else
					return null;
			}
		} else
			return null;
		return jsonObject;
	}
	
	public String getExecutionLog() throws Exception {
		if (executionConfiguration.isExecutingLocally()) {
			StringBuffer sb = new StringBuffer();
			KettleLogLayout logLayout = new KettleLogLayout(true);
			List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren(job.getLogChannelId());
			List<KettleLoggingEvent> logLines = KettleLogStore.getLogBufferFromTo(childIds, true, -1, KettleLogStore.getLastBufferLineNr());
			for (int i = 0; i < logLines.size(); i++) {
				KettleLoggingEvent event = logLines.get(i);
				String line = logLayout.format(event).trim();
				sb.append(line).append("\n");
			}
			return sb.toString();
		} else {
			SlaveServer remoteSlaveServer = executionConfiguration.getRemoteServer();
			SlaveServerJobStatus jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), carteObjectId, 0);
			return jobStatus.getLoggingString();
		}
		
	}
	
}