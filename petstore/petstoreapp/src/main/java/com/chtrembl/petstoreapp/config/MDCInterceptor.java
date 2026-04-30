package com.chtrembl.petstoreapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

import static com.chtrembl.petstoreapp.config.Constants.CLIENT_IP;
import static com.chtrembl.petstoreapp.config.Constants.EXCEPTION_MESSAGE;
import static com.chtrembl.petstoreapp.config.Constants.EXCEPTION_TYPE;
import static com.chtrembl.petstoreapp.config.Constants.HAS_EXCEPTION;
import static com.chtrembl.petstoreapp.config.Constants.HTTP_X_FORWARDED_FOR;
import static com.chtrembl.petstoreapp.config.Constants.PARENT_SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.PROXY_CLIENT_IP;
import static com.chtrembl.petstoreapp.config.Constants.REFERER;
import static com.chtrembl.petstoreapp.config.Constants.REFERER_HEADER;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_DURATION;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_METHOD;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_URI;
import static com.chtrembl.petstoreapp.config.Constants.RESPONSE_STATUS;
import static com.chtrembl.petstoreapp.config.Constants.SESSION_ID;
import static com.chtrembl.petstoreapp.config.Constants.SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.TRACE_ID;
import static com.chtrembl.petstoreapp.config.Constants.USER_AGENT;
import static com.chtrembl.petstoreapp.config.Constants.USER_AGENT_HEADER;
import static com.chtrembl.petstoreapp.config.Constants.WL_PROXY_CLIENT_IP;
import static com.chtrembl.petstoreapp.config.Constants.X_CORRELATION_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_FORWARDED_FOR;
import static com.chtrembl.petstoreapp.config.Constants.X_PARENT_SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_REAL_IP;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_DURATION;
import static com.chtrembl.petstoreapp.config.Constants.X_REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_RESPONSE_REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_RESPONSE_SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_RESPONSE_TRACE_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_SPAN_ID;
import static com.chtrembl.petstoreapp.config.Constants.X_TRACE_ID;

@Component
@Slf4j
public class MDCInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        MDC.put(REQUEST_URI, request.getRequestURI());
        MDC.put(REQUEST_METHOD, request.getMethod());

        String requestId = extractOrGenerateRequestId(request);
        MDC.put(REQUEST_ID, requestId);

        response.setHeader(X_REQUEST_ID, requestId);
        response.setHeader(X_CORRELATION_ID, requestId);

        handleDistributedTracing(request, response);

        addSessionInfo(request);
        addClientInfo(request);

        request.setAttribute("startTime", System.currentTimeMillis());

        log.debug("Starting request processing [RequestID: {}, URI: {}, Method: {}]",
                requestId, request.getRequestURI(), request.getMethod());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        try {
            MDC.put(RESPONSE_STATUS, String.valueOf(response.getStatus()));

            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                MDC.put(REQUEST_DURATION, String.valueOf(duration));

                response.setHeader(X_REQUEST_DURATION, String.valueOf(duration));
            }

            if (ex != null) {
                MDC.put(HAS_EXCEPTION, "true");
                MDC.put(EXCEPTION_TYPE, ex.getClass().getSimpleName());
                MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
                log.error("Request completed with exception [RequestID: {}]", MDC.get(REQUEST_ID), ex);
            } else {
                log.info("Request completed successfully [RequestID: {}, Status: {}, Duration: {}ms]",
                        MDC.get(REQUEST_ID), response.getStatus(), MDC.get(REQUEST_DURATION));
            }

            addTracingHeadersToResponse(response);

        } finally {
            MDC.clear();
        }
    }

    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(X_REQUEST_ID);
        if (StringUtils.hasText(requestId)) {
            log.debug("Using incoming request ID: {}", requestId);
            return requestId;
        }

        requestId = request.getHeader(X_CORRELATION_ID);
        if (StringUtils.hasText(requestId)) {
            log.debug("Using incoming correlation ID as request ID: {}", requestId);
            return requestId;
        }

        requestId = UUID.randomUUID().toString().substring(0, 8);
        log.debug("Generated new request ID: {}", requestId);
        return requestId;
    }

    private void handleDistributedTracing(HttpServletRequest request, HttpServletResponse response) {
        String traceId = request.getHeader(X_TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader(X_TRACE_ID, traceId);

        String parentSpanId = request.getHeader(X_SPAN_ID);
        if (StringUtils.hasText(parentSpanId)) {
            MDC.put(PARENT_SPAN_ID, parentSpanId);
        }

        String spanId = UUID.randomUUID().toString().substring(0, 16);
        MDC.put(SPAN_ID, spanId);
        response.setHeader(X_SPAN_ID, spanId);

        if (StringUtils.hasText(parentSpanId)) {
            response.setHeader(X_PARENT_SPAN_ID, parentSpanId);
        }

        log.debug("Distributed tracing setup [TraceID: {}, SpanID: {}, ParentSpanID: {}]",
                traceId, spanId, parentSpanId);
    }

    private void addSessionInfo(HttpServletRequest request) {
        try {
            String sessionId = request.getSession().getId();
            if (StringUtils.hasText(sessionId)) {
                MDC.put(SESSION_ID, sessionId);
            }
        } catch (Exception e) {
            log.debug("Could not extract session ID: {}", e.getMessage());
        }
    }

    private void addClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        if (StringUtils.hasText(userAgent)) {
            MDC.put(USER_AGENT, userAgent);
        }

        String clientIp = getClientIpAddress(request);
        if (StringUtils.hasText(clientIp)) {
            MDC.put(CLIENT_IP, clientIp);
        }

        String referer = request.getHeader(REFERER_HEADER);
        if (StringUtils.hasText(referer)) {
            MDC.put(REFERER, referer);
        }
    }

    private void addTracingHeadersToResponse(HttpServletResponse response) {
        String traceId = MDC.get(TRACE_ID);
        String spanId = MDC.get(SPAN_ID);
        String requestId = MDC.get(REQUEST_ID);

        if (StringUtils.hasText(traceId)) {
            response.setHeader(X_RESPONSE_TRACE_ID, traceId);
        }
        if (StringUtils.hasText(spanId)) {
            response.setHeader(X_RESPONSE_SPAN_ID, spanId);
        }
        if (StringUtils.hasText(requestId)) {
            response.setHeader(X_RESPONSE_REQUEST_ID, requestId);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                X_FORWARDED_FOR, X_REAL_IP, PROXY_CLIENT_IP,
                WL_PROXY_CLIENT_IP, HTTP_X_FORWARDED_FOR
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
