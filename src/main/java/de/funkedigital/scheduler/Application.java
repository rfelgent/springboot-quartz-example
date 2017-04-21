package de.funkedigital.scheduler;

import de.funkedigital.scheduler.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class Application extends SpringBootServletInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ApplicationContext ctx = new SpringApplicationBuilder().sources(AppConfig.class).run(args);
        if (args != null && args.length > 0) {
            LOGGER.info("Command line arguments: {}", String.join(",", args));
        } else {
            LOGGER.info("No command line arguments");
        }
        LOGGER.info("Active profiles: {}", Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
    }
}
