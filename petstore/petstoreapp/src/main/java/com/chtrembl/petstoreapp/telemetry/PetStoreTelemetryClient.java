package com.chtrembl.petstoreapp.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.PageViewTelemetry;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Custom TelemetryClient that sends data to Application Insights.
 * This version actually sends telemetry data instead of just logging.
 */
@Component
public class PetStoreTelemetryClient {

    private static final Logger logger = LoggerFactory.getLogger(PetStoreTelemetryClient.class);
    private static final Logger telemetryLogger = LoggerFactory.getLogger("ApplicationInsightsTelemetry");

    private final TelemetryClient telemetryClient;

    public PetStoreTelemetryClient() {
        this.telemetryClient = new TelemetryClient();
    }

    public void track(Object telemetry) {
        telemetryLogger.info("Custom telemetry tracked: {}", telemetry);
        logger.debug("Telemetry object processed: {}", telemetry.getClass().getSimpleName());

        if (telemetry instanceof String string) {
            telemetryClient.trackTrace(string);
        }
    }

    public void trackDependency(String dependencyName, String commandName, Object duration, boolean success) {
        telemetryLogger.info("Dependency: {} - {} (Success: {})", dependencyName, commandName, success);
        logger.debug("Dependency call tracked: {} -> {}", dependencyName, commandName);

        long durationMs = duration instanceof Long ? (Long) duration : 0L;
        telemetryClient.trackDependency(dependencyName, commandName, new Duration(durationMs), success);
    }

    public void trackEvent(String name, Map<String, String> properties, Map<String, Double> metrics) {
        if (properties != null) {
            properties.forEach(MDC::put);
        }

        telemetryLogger.info("Event: {} with properties: {} and metrics: {}", name, properties, metrics);
        logger.info("Custom event tracked: {}", name);

        telemetryClient.trackEvent(name, properties, metrics);

        if (properties != null) {
            properties.keySet().forEach(MDC::remove);
        }
    }

    public void trackEvent(String name) {
        telemetryLogger.info("Event: {}", name);
        logger.info("Simple event tracked: {}", name);

        telemetryClient.trackEvent(name);
    }

    public void trackException(Exception exception, Map<String, String> properties, Map<String, Double> metrics) {
        if (properties != null) {
            properties.forEach(MDC::put);
        }

        telemetryLogger.error("Exception tracked with properties: {} and metrics: {}", properties, metrics, exception);
        logger.error("Exception tracked: {}", exception.getMessage(), exception);

        telemetryClient.trackException(exception, properties, metrics);

        if (properties != null) {
            properties.keySet().forEach(MDC::remove);
        }
    }

    public void trackException(Exception exception) {
        telemetryLogger.error("Exception tracked", exception);
        logger.error("Simple exception tracked: {}", exception.getMessage(), exception);

        telemetryClient.trackException(exception);
    }

    public void trackHttpRequest(String name, Date timestamp, long duration, String responseCode, boolean success) {
        telemetryLogger.info("HTTP Request: {} - {} ms (Response: {}, Success: {})", name, duration, responseCode, success);
        logger.debug("HTTP request logged: {} took {} ms", name, duration);

        telemetryClient.trackHttpRequest(name, timestamp, duration, responseCode, success);
    }

    public void trackMetric(String name, double value, int sampleCount, double min, double max, Map<String, String> properties) {
        if (properties != null) {
            properties.forEach(MDC::put);
        }

        telemetryLogger.info("Metric: {} = {} (samples: {}, min: {}, max: {}) with properties: {}",
                name, value, sampleCount, min, max, properties);
        logger.debug("Metric tracked: {} = {}", name, value);

        telemetryClient.trackMetric(name, value);

        if (properties != null) {
            properties.keySet().forEach(MDC::remove);
        }
    }

    public void trackMetric(String name, double value) {
        telemetryLogger.info("Metric: {} = {}", name, value);
        logger.debug("Simple metric tracked: {} = {}", name, value);

        telemetryClient.trackMetric(name, value);
    }

    public void trackPageView(Object pageViewTelemetry) {
        telemetryLogger.info("Page view tracked: {}", pageViewTelemetry);
        logger.debug("Page view processed: {}", pageViewTelemetry);

        if (pageViewTelemetry instanceof PageViewTelemetry pageViewTel) {
            telemetryClient.track(pageViewTel);
        }
    }

    public void trackPageView(String name) {
        telemetryLogger.info("Page view: {}", name);
        logger.info("Page view tracked: {}", name);

        telemetryClient.trackPageView(name);
    }

    public void trackTrace(String message, Object severityLevel, Map<String, String> properties) {
        if (properties != null) {
            properties.forEach(MDC::put);
        }

        telemetryLogger.info("Trace [{}]: {} with properties: {}", severityLevel, message, properties);
        logger.debug("Trace logged with level: {}", severityLevel);

        if (severityLevel instanceof SeverityLevel severity) {
            telemetryClient.trackTrace(message, severity, properties);
        } else {
            telemetryClient.trackTrace(message, SeverityLevel.Information, properties);
        }

        if (properties != null) {
            properties.keySet().forEach(MDC::remove);
        }
    }

    public void trackTrace(String message, Object severityLevel) {
        telemetryLogger.info("Trace [{}]: {}", severityLevel, message);
        logger.debug("Simple trace: {}", message);

        if (severityLevel instanceof SeverityLevel severity) {
            telemetryClient.trackTrace(message, severity);
        } else {
            telemetryClient.trackTrace(message, SeverityLevel.Information);
        }
    }

    public void trackTrace(String message) {
        telemetryLogger.info("Trace: {}", message);
        logger.info("Trace message: {}", message);

        telemetryClient.trackTrace(message);
    }

    public void flush() {
        telemetryLogger.info("Telemetry flush requested");
        logger.debug("Telemetry flush operation called");

        telemetryClient.flush();
    }
}
