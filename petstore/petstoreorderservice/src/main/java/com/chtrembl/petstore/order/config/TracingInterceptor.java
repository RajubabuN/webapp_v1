package com.chtrembl.petstore.order.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Interceptor for automatic addition of tracing headers to all RestTemplate calls.
 * Propagates session, request, trace, and span IDs from MDC to downstream services.
 */
@Slf4j
public class TracingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        long startTime = System.currentTimeMillis();

        addTracingHeaders(request);

        try {
            ClientHttpResponse response = execution.execute(request, body);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("RestTemplate call completed: {} {} - {} ({} ms)",
                    request.getMethod(), request.getURI(),
                    response.getStatusCode(), duration);

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("RestTemplate call failed: {} {} - {} ({} ms)",
                    request.getMethod(), request.getURI(),
                    e.getMessage(), duration);
            throw e;
        }
    }

    /**
     * Adds tracing headers from MDC to the outgoing request
     */
    private void addTracingHeaders(HttpRequest request) {
        // Get data from MDC
        String requestId = MDC.get(Constants.REQUEST_ID);
        String sessionId = MDC.get(Constants.SESSION_ID);
        String traceId = MDC.get(Constants.TRACE_ID);
        String spanId = MDC.get(Constants.SPAN_ID);

        // Request ID
        if (StringUtils.hasText(requestId)) {
            request.getHeaders().add(Constants.X_REQUEST_ID, requestId);
            request.getHeaders().add(Constants.X_CORRELATION_ID, requestId);
        } else {
            // Generate new if missing
            String newRequestId = UUID.randomUUID().toString().substring(0, 8);
            request.getHeaders().add(Constants.X_REQUEST_ID, newRequestId);
            request.getHeaders().add(Constants.X_CORRELATION_ID, newRequestId);
            log.debug("Generated new request ID for RestTemplate call: {}", newRequestId);
        }

        // Session ID
        if (StringUtils.hasText(sessionId)) {
            request.getHeaders().add(Constants.X_SESSION_ID, sessionId);
            request.getHeaders().add(Constants.X_SESSION_ID_LOWERCASE, sessionId);
            log.debug("Forwarding session ID: {} to {}", sessionId, request.getURI());
        } else {
            log.debug("No session ID found in MDC for call to: {}", request.getURI());
        }

        // Trace ID
        if (StringUtils.hasText(traceId)) {
            request.getHeaders().add(Constants.X_TRACE_ID, traceId);
        } else {
            // Generate new trace ID
            String newTraceId = UUID.randomUUID().toString().replaceAll("-", "");
            request.getHeaders().add(Constants.X_TRACE_ID, newTraceId);
            log.debug("Generated new trace ID for RestTemplate call: {}", newTraceId);
        }

        // Parent Span ID (current span becomes parent for new service)
        if (StringUtils.hasText(spanId)) {
            request.getHeaders().add(Constants.X_PARENT_SPAN_ID, spanId);
        }

        // Service metadata
        request.getHeaders().add(Constants.X_SOURCE_SERVICE, "order-service");
        request.getHeaders().add(Constants.X_REQUEST_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        // Determine target service by URL
        String targetService = extractTargetService(request.getURI().toString());
        request.getHeaders().add(Constants.X_TARGET_SERVICE, targetService);

        log.debug("Added tracing headers for RestTemplate call: requestId={}, sessionId={}, traceId={}, target={}",
                requestId, sessionId, traceId, targetService);
    }

    /**
     * Determines target service name by analyzing the URL
     */
    private String extractTargetService(String url) {
        if (url.contains("petstoreproductservice")) {
            return "product-service";
        } else if (url.contains("petstorepetservice")) {
            return "pet-service";
        } else if (url.contains("petstoreorderservice")) {
            return "order-service";
        }
        return "unknown-service";
    }
}