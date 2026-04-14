package com.chtrembl.petstoreapp.config;

import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.model.WebRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.chtrembl.petstoreapp.config.Constants.AUTH_TYPE;
import static com.chtrembl.petstoreapp.config.Constants.CACHE_CONTROL;
import static com.chtrembl.petstoreapp.config.Constants.CONTAINER_HOST;
import static com.chtrembl.petstoreapp.config.Constants.IS_AUTHENTICATED;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_METHOD;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_URI;
import static com.chtrembl.petstoreapp.config.Constants.SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.TRACE_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_AUTHENTICATED;
import static com.chtrembl.petstoreapp.config.Constants.X_AUTH_TYPE;
import static com.chtrembl.petstoreapp.config.Constants.X_CORRELATION_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_HTTP_SESSION_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_PARENT_SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_METHOD;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_TIMESTAMP;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_URI;
import static com.chtrembl.petstoreapp.config.Constants.X_SESSION_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_SESSION_ID_LOWERCASE;
import static com.chtrembl.petstoreapp.config.Constants.X_SOURCE_CONTAINER;
import static com.chtrembl.petstoreapp.config.Constants.X_SOURCE_SERVICE;
import static com.chtrembl.petstoreapp.config.Constants.X_SOURCE_VERSION;
import static com.chtrembl.petstoreapp.config.Constants.X_TARGET_SERVICE;
import static com.chtrembl.petstoreapp.config.Constants.X_TRACE_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_USER_EMAIL;
import static com.chtrembl.petstoreapp.config.Constants.X_USER_NAME;

@Configuration
@EnableFeignClients(basePackages = "com.chtrembl.petstoreapp.client")
@RequiredArgsConstructor
@Slf4j
public class FeignConfig {

    private final WebRequest webRequest;
    private final User sessionUser;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new EnhancedRequestInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new PetstoreErrorDecoder();
    }

    @Bean
    public feign.Request.Options feignOptions() {
        return new feign.Request.Options(
                5000, TimeUnit.MILLISECONDS,  // connect timeout
                5000, TimeUnit.MILLISECONDS,  // read timeout
                true  // follow redirects
        );
    }

    private static class PetstoreErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            String requestId = extractHeaderValue(response, "X-Request-ID");
            String sessionId = extractHeaderValue(response, "X-Session-ID");
            String responseTraceId = extractHeaderValue(response, "X-Response-Trace-ID");
            String currentTraceId = MDC.get("traceId");

            if (response.status() == 404) {
                log.info("Feign client resource not found on {} [RequestID: {}, SessionID: {}, TraceID: {}, ResponseTraceID: {}]: {} - {}",
                        methodKey, requestId, sessionId, currentTraceId, responseTraceId,
                        response.status(), response.reason());
            } else {
                log.error("Feign client error on {} [RequestID: {}, SessionID: {}, TraceID: {}, ResponseTraceID: {}]: {} - {}",
                        methodKey, requestId, sessionId, currentTraceId, responseTraceId,
                        response.status(), response.reason());
            }

            String errorMessage = String.format(
                    "Service call failed for %s [RequestID: %s, SessionID: %s, TraceID: %s] with status %d",
                    methodKey, requestId, sessionId, currentTraceId, response.status());

            return switch (response.status()) {
                case 404 -> new feign.FeignException.NotFound(
                        "Resource not found for " + methodKey,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
                case 400 -> new feign.FeignException.BadRequest(
                        "Bad request for " + methodKey,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
                case 429 -> new feign.FeignException.TooManyRequests(
                        "Rate limit exceeded for " + methodKey,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
                case 500 -> new feign.FeignException.InternalServerError(
                        "Internal server error for " + methodKey,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
                case 503 -> new feign.FeignException.ServiceUnavailable(
                        "Service unavailable for " + methodKey,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
                default -> new feign.FeignException.BadRequest(
                        errorMessage,
                        response.request(),
                        extractResponseBody(response),
                        response.headers()
                );
            };
        }

        private byte[] extractResponseBody(feign.Response response) {
            try {
                if (response.body() != null) {
                    return response.body().toString().getBytes();
                }
            } catch (Exception e) {
                log.warn("Failed to extract response body: {}", e.getMessage());
            }
            return new byte[0];
        }

        private String extractHeaderValue(feign.Response response, String headerName) {
            try {
                return response.headers().getOrDefault(headerName, java.util.Collections.emptyList())
                        .stream()
                        .findFirst()
                        .orElse("unknown");
            } catch (Exception e) {
                return "unknown";
            }
        }
    }

    private class EnhancedRequestInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            webRequest.getHeaders().forEach((key, values) -> {
                values.forEach(value -> template.header(key, value));
            });

            template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            template.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            template.header(CACHE_CONTROL, "no-cache");

            addSessionHeaders(template);
            addCorrelationHeaders(template);
            addUserContextHeaders(template);
            addServiceHeaders(template);

            template.header(X_REQUEST_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            String requestId = MDC.get(REQUEST_ID);
            String traceId = MDC.get(TRACE_ID);
            String targetService = template.feignTarget().name();

            log.info("Outgoing Feign request: {} {} [RequestID: {}, TraceID: {}, Target: {}]",
                    template.method(), template.url(), requestId, traceId, targetService);
            
            log.debug("Applied headers to Feign request: {} {}",
                    template.method(), template.url());
            log.debug("All headers: {}", template.headers());
        }

        private void addSessionHeaders(RequestTemplate template) {
            if (sessionUser != null && StringUtils.hasText(sessionUser.getSessionId())) {
                template.header(X_SESSION_ID, sessionUser.getSessionId());
                template.header(X_SESSION_ID_LOWERCASE, sessionUser.getSessionId());
                log.debug("Added session ID header: {}", sessionUser.getSessionId());
            }

            try {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                String sessionId = attributes.getRequest().getSession().getId();
                if (StringUtils.hasText(sessionId)) {
                    template.header(X_HTTP_SESSION_ID, sessionId);
                }
            } catch (Exception e) {
                log.debug("Could not extract HTTP session ID: {}", e.getMessage());
            }
        }

        private void addCorrelationHeaders(RequestTemplate template) {
            String requestId = MDC.get(REQUEST_ID);
            if (StringUtils.hasText(requestId)) {
                template.header(X_REQUEST_ID, requestId);
                template.header(X_CORRELATION_ID, requestId);
            } else {
                String newRequestId = UUID.randomUUID().toString().substring(0, 8);
                template.header(X_REQUEST_ID, newRequestId);
                template.header(X_CORRELATION_ID, newRequestId);
                log.debug("Generated new request ID: {}", newRequestId);
            }

            String traceId = MDC.get(TRACE_ID);
            if (StringUtils.hasText(traceId)) {
                template.header(X_TRACE_ID, traceId);
            }

            String spanId = MDC.get(SPAN_ID);
            if (StringUtils.hasText(spanId)) {
                template.header(X_PARENT_SPAN_ID, spanId);
            }
        }

        private void addUserContextHeaders(RequestTemplate template) {
            if (sessionUser != null) {
                if (StringUtils.hasText(sessionUser.getName())) {
                    template.header(X_USER_NAME, sessionUser.getName());
                }

                if (StringUtils.hasText(sessionUser.getEmail())) {
                    template.header(X_USER_EMAIL, sessionUser.getEmail());
                }

                String authType = MDC.get(AUTH_TYPE);
                if (StringUtils.hasText(authType)) {
                    template.header(X_AUTH_TYPE, authType);
                }

                String isAuthenticated = MDC.get(IS_AUTHENTICATED);
                if (StringUtils.hasText(isAuthenticated)) {
                    template.header(X_AUTHENTICATED, isAuthenticated);
                }
            }
        }

        private void addServiceHeaders(RequestTemplate template) {
            template.header(X_SOURCE_SERVICE, "petstoreapp");
            template.header(X_SOURCE_VERSION, getAppVersion());

            String targetService = template.feignTarget().name();
            template.header(X_TARGET_SERVICE, targetService);

            template.header(X_REQUEST_URI, MDC.get(REQUEST_URI));
            template.header(X_REQUEST_METHOD, MDC.get(REQUEST_METHOD));

            String containerHost = MDC.get(CONTAINER_HOST);
            if (StringUtils.hasText(containerHost)) {
                template.header(X_SOURCE_CONTAINER, containerHost);
            }
        }

        private String getAppVersion() {
            String version = System.getProperty("app.version");
            if (StringUtils.hasText(version)) {
                return version;
            }

            Package pkg = this.getClass().getPackage();
            if (pkg != null && pkg.getImplementationVersion() != null) {
                return pkg.getImplementationVersion();
            }

            return "unknown";
        }
    }
}
