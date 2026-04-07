package com.chtrembl.petstoreapp.config.health;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component("petService")
@RequiredArgsConstructor
@Slf4j
public class PetServiceHealthIndicator implements HealthIndicator {

    private final ContainerEnvironment containerEnvironment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Health health() {
        try {
            String baseUrl = containerEnvironment.getPetStorePetServiceURL();
            if (baseUrl == null || baseUrl.isEmpty()) {
                return Health.down()
                        .withDetail("reason", "Pet service URL not configured")
                        .withDetail("url", "not set")
                        .build();
            }

            // Call custom /health endpoint
            String response = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri("/petstorepetservice/v2/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            JsonNode healthData = objectMapper.readTree(response);
            String status = healthData.get("status").asText();
            String version = healthData.has("version") ? healthData.get("version").asText() : "unknown";
            String date = healthData.has("date") ? healthData.get("date").asText() : "unknown";
            String container = healthData.has("container") ? healthData.get("container").asText() : "unknown";

            if ("UP".equalsIgnoreCase(status)) {
                return Health.up()
                        .withDetail("url", baseUrl)
                        .withDetail("status", "Service responding")
                        .withDetail("version", version)
                        .withDetail("appDate", date)
                        .withDetail("container", container)
                        .build();
            } else {
                return Health.down()
                        .withDetail("url", baseUrl)
                        .withDetail("reason", "Service status: " + status)
                        .withDetail("version", version)
                        .withDetail("appDate", date)
                        .withDetail("container", container)
                        .build();
            }

        } catch (WebClientResponseException e) {
            log.warn("Pet service health check failed with HTTP {}: {}", e.getStatusCode(), e.getMessage());
            return Health.down()
                    .withDetail("url", containerEnvironment.getPetStorePetServiceURL())
                    .withDetail("error", "HTTP " + e.getStatusCode().value() + ": " + e.getStatusText())
                    .withDetail("responseBody", e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.warn("Pet service health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("url", containerEnvironment.getPetStorePetServiceURL())
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}
