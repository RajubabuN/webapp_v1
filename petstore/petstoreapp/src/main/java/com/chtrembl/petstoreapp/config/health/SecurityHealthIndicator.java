package com.chtrembl.petstoreapp.config.health;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "management.health.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class SecurityHealthIndicator implements HealthIndicator {

    private final ContainerEnvironment containerEnvironment;

    @Override
    public Health health() {
        boolean securityEnabled = containerEnvironment.isSecurityEnabled();
        String azureB2cBaseUri = System.getenv("PETSTOREAPP_B2C_BASE_URI");
        String azureB2cClientId = System.getenv("PETSTOREAPP_B2C_CLIENT_ID");
        String azureB2cEnabled = System.getenv("PETSTOREAPP_B2C_ENABLED");

        if (!securityEnabled) {
            return Health.up()
                    .withDetail("security", "Disabled")
                    .withDetail("azureB2C", "Not applicable")
                    .withDetail("reason", "Security disabled in configuration")
                    .build();
        }

        boolean b2cConfigured = azureB2cBaseUri != null && !azureB2cBaseUri.isEmpty() &&
                azureB2cClientId != null && !azureB2cClientId.isEmpty();
        boolean b2cEnabled = "true".equalsIgnoreCase(azureB2cEnabled);

        if (securityEnabled && b2cConfigured && b2cEnabled) {
            return Health.up()
                    .withDetail("security", "Enabled")
                    .withDetail("azureB2C", "Configured and enabled")
                    .withDetail("baseUri", azureB2cBaseUri)
                    .withDetail("hasClientId", true)
                    .build();
        } else if (securityEnabled && !b2cConfigured) {
            return Health.down()
                    .withDetail("security", "Enabled")
                    .withDetail("azureB2C", "Not configured")
                    .withDetail("reason", "Security enabled but Azure B2C not properly configured")
                    .withDetail("hasBaseUri", azureB2cBaseUri != null && !azureB2cBaseUri.isEmpty())
                    .withDetail("hasClientId", azureB2cClientId != null && !azureB2cClientId.isEmpty())
                    .build();
        } else {
            return Health.up()
                    .withDetail("security", "Enabled")
                    .withDetail("azureB2C", "Disabled")
                    .withDetail("reason", "Azure B2C disabled via configuration")
                    .build();
        }
    }
}