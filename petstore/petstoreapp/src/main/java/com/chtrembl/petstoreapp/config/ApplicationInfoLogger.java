package com.chtrembl.petstoreapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Component for logging application startup information
 */
@Component
@Slf4j
public class ApplicationInfoLogger implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        logApplicationStartupInfo();
    }

    private void logApplicationStartupInfo() {
        log.info("Starting Pet Store Application...");
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Spring Boot Version: {}", getSpringBootVersion());
        log.info("Working Directory: {}", System.getProperty("user.dir"));
        log.info("Available Processors: {}", Runtime.getRuntime().availableProcessors());
        log.info("Max Memory: {} MB", Runtime.getRuntime().maxMemory() / (1024 * 1024));
    }

    private String getSpringBootVersion() {
        try {
            Package springBootPackage = SpringApplication.class.getPackage();
            return springBootPackage.getImplementationVersion() != null
                    ? springBootPackage.getImplementationVersion()
                    : "Unknown";
        } catch (Exception e) {
            log.debug("Could not determine Spring Boot version", e);
            return "Unknown";
        }
    }
}