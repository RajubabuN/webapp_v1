package com.chtrembl.petstoreapp.security;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfiguration {

    private final ContainerEnvironment containerEnvironment;

    @Value("${petstore.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${azure.entra.external-id.tenant-domain:petshopdemo}")
    private String tenantDomain;

    @Value("${AZURE_CLIENT_ID:}")
    private String clientId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        if (!securityEnabled) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            containerEnvironment.setSecurityEnabled(false);
            log.warn("Security is DISABLED via petstore.security.enabled = false");
            return http.build();
        }

        // Check if External ID is properly configured
        boolean externalIdConfigured = tenantDomain != null && !tenantDomain.isEmpty() &&
                clientId != null && !clientId.isEmpty();

        if (!externalIdConfigured) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            containerEnvironment.setSecurityEnabled(false);
            log.warn("Security ENABLED but External ID not configured - fallback to DISABLED");
            return http.build();
        }

        // External ID OAuth2 configuration
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/content/**", "/.well-known/**"))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/*breed*").permitAll()
                        .requestMatchers("/*product*").permitAll()
                        .requestMatchers("/*cart*").permitAll()
                        .requestMatchers("/api/contactus").permitAll()
                        .requestMatchers("/login*").permitAll()
                        .requestMatchers("/content/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(this::handleLogoutSuccess)
                );

        containerEnvironment.setSecurityEnabled(true);
        log.info("Security is ENABLED using Microsoft Entra External ID OAuth2");

        return http.build();
    }

    /**
     * Custom logout success handler that redirects to External ID logout URL
     */
    private void handleLogoutSuccess(HttpServletRequest request,
                                     HttpServletResponse response,
                                     org.springframework.security.core.Authentication authentication)
            throws java.io.IOException {

        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ":" + request.getServerPort();

        if (tenantDomain != null && !tenantDomain.isEmpty()) {
            String logoutUrl = "https://" + tenantDomain + ".ciamlogin.com/" +
                    tenantDomain + ".onmicrosoft.com/oauth2/v2.0/logout" +
                    "?post_logout_redirect_uri=" + baseUrl;
            response.sendRedirect(logoutUrl);
        } else {
            log.warn("AZURE_TENANT_DOMAIN not configured, using default logout");
            response.sendRedirect("/");
        }
    }
}