package de.funkedigital.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @link https://github.com/quartz-scheduler/quartz/issues/117
 */
@DisallowConcurrentExecution
public class ScheduledJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        LOGGER.info("Handle schedule event for job key {}", jobKey.toString());

        try {
            //the long running task
            LOGGER.info("Starting long running task");
            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            LOGGER.info("Finished long running task");
        } catch (Exception e) {
            LOGGER.error("Error", e);
            throw new JobExecutionException(e);
        }
    }
}
