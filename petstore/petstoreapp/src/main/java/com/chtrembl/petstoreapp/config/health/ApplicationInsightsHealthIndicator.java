package com.chtrembl.petstoreapp.config.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("applicationInsights")
@ConditionalOnProperty(prefix = "management.health.applicationinsights", name = "enabled", havingValue = "true", matchIfMissing = true)
class ApplicationInsightsHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        String connectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        String enabled = System.getenv("APPLICATIONINSIGHTS_ENABLED");

        if ("false".equalsIgnoreCase(enabled)) {
            return Health.up()
                    .withDetail("status", "Disabled")
                    .withDetail("reason", "Disabled via environment variable")
                    .build();
        }

        if (connectionString == null || connectionString.isEmpty()) {
            return Health.down()
                    .withDetail("status", "Not configured")
                    .withDetail("reason", "Connection string not set")
                    .build();
        }

        return Health.up()
                .withDetail("status", "Configured")
                .withDetail("hasConnectionString", true)
                .build();
    }
}