package de.funkedigital.scheduler;

import de.funkedigital.scheduler.spring.QuartzBeanJobFactory;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@SpringBootApplication
public class AppConfig {

    @Bean("jobDefinitions")
    public Map<String,String> jobDefinitions() {
        //the job definitions could be retrieved via *.xml, *.yml, database or any other persistent store!
        Map<String, String> jobs = new HashMap<String, String>();
        jobs.put("meier", "0/20 * * * * ?");
        jobs.put("hans", "0/10 * * * * ?");
        jobs.put("max", "0/5 * * * * ?");
        jobs.put("homer", "0/30 * * * * ?");
        jobs.put("wurst", "0/12 * * * * ?");
        return jobs;
    }

    @Configuration
    protected static class QuartzSchedulerConfig {

        @Autowired
        private DataSource dataSource;

        @Bean
        public JobFactory jobFactory(ApplicationContext applicationContext) {
            QuartzBeanJobFactory jobFactory = new QuartzBeanJobFactory();
            jobFactory.setApplicationContext(applicationContext);
            return jobFactory;
        }

        //ensures Quartz tables before SchedulerFactory is initialised
        @DependsOn("liquibase")
        @Bean
        public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
            SchedulerFactoryBean factory = new SchedulerFactoryBean();
            // this allows to update triggers in DB when updating settings in config file
            factory.setOverwriteExistingJobs(true);
            factory.setJobFactory(jobFactory);
            factory.setDataSource(dataSource);
            factory.setQuartzProperties(quartzProperties());
            //SchedulerService is taking care of start/stop!
            factory.setAutoStartup(false);
            return factory;
        }

        @Bean
        public Properties quartzProperties() {
            try {
                PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
                propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
                propertiesFactoryBean.afterPropertiesSet();
                return propertiesFactoryBean.getObject();
            } catch (IOException ioe) {
                throw new IllegalStateException("loading quartz.properties failed", ioe);
            }
        }
    }
}
