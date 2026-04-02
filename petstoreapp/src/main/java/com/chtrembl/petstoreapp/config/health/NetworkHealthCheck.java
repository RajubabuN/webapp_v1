package com.chtrembl.petstoreapp.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class NetworkHealthCheck {

    @EventListener(ApplicationReadyEvent.class)
    public void performHealthCheck() {
        try {
            InetAddress.getByName("www.google.com");
            log.info("External network connectivity: OK");
        } catch (UnknownHostException e) {
            log.warn("External network connectivity: FAILED - {}", e.getMessage());
        }
    }
}