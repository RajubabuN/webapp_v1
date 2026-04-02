package com.chtrembl.petstoreapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

/**
 * Configuration for application startup tasks and final status logging
 */
@Configuration
@Slf4j
@Order(100)
public class ApplicationStartupConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Pet Store Application started successfully!");
        log.info("Application is ready to accept requests");

        logEnvironmentInfo();
    }

    private void logEnvironmentInfo() {
        String profile = System.getProperty("spring.profiles.active", "default");
        String port = System.getProperty("server.port", "8080");

        log.info("Active Profile: {}", profile);
        log.info("Server Port: {}", port);

        // Log service URLs if available
        String petServiceUrl = System.getenv("PETSTOREPETSERVICE_URL");
        String productServiceUrl = System.getenv("PETSTOREPRODUCTSERVICE_URL");
        String orderServiceUrl = System.getenv("PETSTOREORDERSERVICE_URL");

        if (petServiceUrl != null) {
            log.info("Pet Service URL: {}", petServiceUrl);
        }
        if (productServiceUrl != null) {
            log.info("Product Service URL: {}", productServiceUrl);
        }
        if (orderServiceUrl != null) {
            log.info("Order Service URL: {}", orderServiceUrl);
        }
    }
}