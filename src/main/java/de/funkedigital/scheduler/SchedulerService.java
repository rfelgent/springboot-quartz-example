package de.funkedigital.scheduler;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper of the Quartz {@link org.quartz.Scheduler} for programmatic administration of jobs and triggers.
 *
 * @author rfelgent
 */
@Service
public class SchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

    private static final String SCHEDULESERVICES_GROUP = "scheduleservices";

    @Autowired
    private Map<String, String> jobDefinitions;

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    protected void start() throws SchedulerException {
        LOGGER.info("The user might have changed job configuration ==> remove all triggers and job details");
        scheduler.clear();

        LOGGER.info("Creating cron jobs");

        for (Map.Entry<String, String> jobDefinition : jobDefinitions.entrySet()) {
            createJob(jobDefinition.getKey(), jobDefinition.getValue());
        }

        try {
            LOGGER.info("Starting scheduler");
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    protected void preDestroy() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to finished the jobs before shutdown");
        }
    }

    private JobDetail createJobDetail(String name) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(ScheduledJob.class);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        factoryBean.setName(name);
        factoryBean.setGroup(SCHEDULESERVICES_GROUP);
        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();
    }

    private Trigger createCronTrigger(JobDetail jobDetail, String cronExpression) {
        try {
            CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
            factoryBean.setJobDetail(jobDetail);
            factoryBean.setCronExpression(cronExpression);
            factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
            //Must set a unique name, otherwise quartz complains!
            factoryBean.setName(jobDetail.getKey().toString());
            factoryBean.setGroup(SCHEDULESERVICES_GROUP);
            factoryBean.setStartDelay(TimeUnit.SECONDS.toMillis(10));
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the cron trigger", e);
        }
    }

    private void createJob(String name, String cron) {
        JobDetail jobDetail = createJobDetail(name);
        try {
            scheduler.addJob(jobDetail, false);
            Trigger trigger = createCronTrigger(jobDetail, cron);
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
