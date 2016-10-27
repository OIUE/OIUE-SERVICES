package org.oiue.service.task.quartz;

import java.util.HashMap;
import java.util.Map;

import org.oiue.service.log.Logger;
import org.oiue.service.task.Task;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob implements Job {
    static Logger logger;
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map newJobDataMap = context.getMergedJobDataMap();
            if(logger.isDebugEnabled()){
                logger.debug("running job:"+newJobDataMap);
            }
            Map data = new HashMap<>(newJobDataMap);
            Task task = (Task) newJobDataMap.remove(QuartzServiceImpl.QUARTZ_JOB);
            task.execute(newJobDataMap);
        } catch (Throwable e) {
            logger.error("running task throw error:" + e.getMessage(), e);
        }
    }
}