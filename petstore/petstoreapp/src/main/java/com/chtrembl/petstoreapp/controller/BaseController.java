package com.chtrembl.petstoreapp.controller;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.util.ExternalIdUtils;
import com.microsoft.applicationinsights.telemetry.PageViewTelemetry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.chtrembl.petstoreapp.config.Constants.AUTH_TYPE;
import static com.chtrembl.petstoreapp.config.Constants.CONTAINER_HOST;
import static com.chtrembl.petstoreapp.config.Constants.IS_AUTHENTICATED;
import static com.chtrembl.petstoreapp.config.Constants.SESSION_ID;
import static com.chtrembl.petstoreapp.config.Constants.USER_EMAIL;
import static com.chtrembl.petstoreapp.config.Constants.USER_NAME;

/**
 * Base controller providing common functionality for all domain controllers.
 * Handles session management, authentication setup, and shared model attributes.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public abstract class BaseController {

    private static final String CURRENT_USERS_HUB = "currentUsers";

    // Model attribute constants
    private static final String MODEL_APP_VERSION = "appVersion";
    private static final String MODEL_CART_SIZE = "cartSize";
    private static final String MODEL_CLAIMS = "claims";
    private static final String MODEL_CONTAINER_ENVIRONMENT = "containerEnvironment";
    private static final String MODEL_CURRENT_USERS_ON_SITE = "currentUsersOnSite";
    private static final String MODEL_EMAIL = "email";
    private static final String MODEL_GRANT_TYPE = "grant_type";
    private static final String MODEL_SESSION_ID = "sessionId";
    private static final String MODEL_USER = "user";
    private static final String MODEL_USER_LOGGED_IN = "userLoggedIn";
    private static final String MODEL_USER_NAME = "userName";

    protected final ContainerEnvironment containerEnvironment;
    protected final User sessionUser;
    protected final CacheManager currentUsersCacheManager;

    /**
     * Common model setup for all controllers.
     */
    @ModelAttribute
    public void setModel(HttpServletRequest request, Model model, @AuthenticationPrincipal OidcUser principal) {
        setupSessionUser(request, model, principal);
        setupAuthenticationDetails(model, principal);
        setupContainerInfo(model);
        setupCacheInfo(model);
    }

    /**
     * Setup session user information.
     */
    private void setupSessionUser(HttpServletRequest request, Model model, OidcUser principal) {
        CaffeineCache caffeineCache = (CaffeineCache) this.currentUsersCacheManager
                .getCache(CURRENT_USERS_HUB);

        if (sessionUser.getSessionId() == null) {
            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
            sessionUser.setSessionId(sessionId);
            caffeineCache.put(sessionUser.getSessionId(), sessionUser.getName());
        }

        if (principal != null) {
            String displayName = ExternalIdUtils.getDisplayName(principal);
            String email = ExternalIdUtils.getEmail(principal);

            if (displayName != null && !displayName.equals(sessionUser.getName())) {
                sessionUser.setName(displayName);
                log.debug("Updated session user name to: {}", displayName);
            }

            if (email != null && !email.equals(sessionUser.getEmail())) {
                sessionUser.setEmail(email);
                log.debug("Updated session user email to: {}", email);
            }
        }

        MDC.put(SESSION_ID, sessionUser.getSessionId());
        MDC.put(USER_NAME, sessionUser.getName());
        MDC.put(CONTAINER_HOST, this.containerEnvironment.getContainerHostName());

        caffeineCache.put(sessionUser.getSessionId(), sessionUser.getName());

        model.addAttribute(MODEL_USER_NAME, sessionUser.getName());
        model.addAttribute(MODEL_SESSION_ID, sessionUser.getSessionId());
        model.addAttribute(MODEL_CART_SIZE, sessionUser.getCartCount());
    }

    /**
     * Setup authentication details and user information.
     */
    private void setupAuthenticationDetails(Model model, OidcUser principal) {
        if (principal != null) {
            String displayName = ExternalIdUtils.getDisplayName(principal);
            String email = ExternalIdUtils.getEmail(principal);
            String userId = ExternalIdUtils.getUserId(principal);

            if (displayName != null) {
                sessionUser.setName(displayName);
                MDC.put(USER_NAME, displayName);
                model.addAttribute(MODEL_USER_NAME, displayName);
            }

            if (email != null) {
                sessionUser.setEmail(email);
                MDC.put(USER_EMAIL, email);
                model.addAttribute(MODEL_EMAIL, email);
                log.debug("User email set to: {}", email);
            } else {
                log.warn("Could not extract email for user: {}", sessionUser.getName());
            }

            MDC.put(AUTH_TYPE, "OAuth2-ExternalID");
            MDC.put(IS_AUTHENTICATED, "true");

            if (!sessionUser.isInitialTelemetryRecorded()) {
                sessionUser.getTelemetryClient().trackEvent(
                        String.format("PetStoreApp %s logged in, container host: %s",
                                sessionUser.getName(),
                                this.containerEnvironment.getContainerHostName()),
                        sessionUser.getCustomEventProperties(), null);

                sessionUser.setInitialTelemetryRecorded(true);
            }

            model.addAttribute(MODEL_CLAIMS, principal.getClaims());
            model.addAttribute(MODEL_USER, sessionUser.getName());
            model.addAttribute(MODEL_USER_LOGGED_IN, true);
            model.addAttribute(MODEL_GRANT_TYPE, principal.getAuthorities());
        } else {
            MDC.put(AUTH_TYPE, "Anonymous");
            MDC.put(IS_AUTHENTICATED, "false");
            model.addAttribute(MODEL_USER_LOGGED_IN, false);
        }
    }

    /**
     * Setup container environment information.
     */
    private void setupContainerInfo(Model model) {
        model.addAttribute(MODEL_CONTAINER_ENVIRONMENT, this.containerEnvironment);
        model.addAttribute(MODEL_APP_VERSION, this.containerEnvironment.getAppVersion());
    }

    /**
     * Setup cache information.
     */
    private void setupCacheInfo(Model model) {
        CaffeineCache caffeineCache = (CaffeineCache) this.currentUsersCacheManager
                .getCache(CURRENT_USERS_HUB);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                caffeineCache.getNativeCache();

        model.addAttribute(MODEL_CURRENT_USERS_ON_SITE, nativeCache.asMap().size());
    }

    /**
     * Track page view for telemetry.
     */
    protected void trackPageView(HttpServletRequest request, String pageName) {
        try {
            PageViewTelemetry pageViewTelemetry = new PageViewTelemetry();
            pageViewTelemetry.setUrl(new URI(request.getRequestURL().toString()));
            pageViewTelemetry.setName(pageName);
            sessionUser.getTelemetryClient().trackPageView(pageViewTelemetry);
        } catch (URISyntaxException e) {
            log.warn("Failed to track page view for {}: {}", pageName, e.getMessage());
        }
    }

    /**
     * Retrieve stack trace information for error handling.
     */
    protected String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        appendStackTrace(sb, throwable, "");
        return sb.toString();
    }

    /**
     * Recursively append stack trace information.
     */
    private void appendStackTrace(StringBuilder sb, Throwable throwable, String prefix) {
        if (throwable == null) return;

        sb.append(prefix).append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }

        for (Throwable suppressed : throwable.getSuppressed()) {
            appendStackTrace(sb, suppressed, "Suppressed: ");
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            appendStackTrace(sb, cause, "Caused by: ");
        }
    }
}