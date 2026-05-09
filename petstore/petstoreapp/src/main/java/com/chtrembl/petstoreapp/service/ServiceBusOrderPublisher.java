package com.chtrembl.petstoreapp.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.chtrembl.petstoreapp.model.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Publishes order update events to Azure Service Bus.
 * The OrderItemsReserver Azure Function subscribes to the same queue
 * and uploads a JSON snapshot of the order to Blob Storage.
 *
 * If the Service Bus connection string is not configured (e.g. local dev without SB),
 * publishing is skipped gracefully — the rest of the order flow is unaffected.
 */
@Service
@Slf4j
public class ServiceBusOrderPublisher {

    @Value("${petstore.servicebus.connection-string:}")
    private String connectionString;

    @Value("${petstore.servicebus.queue-name:order-queue}")
    private String queueName;

    private ServiceBusSenderClient senderClient;
    private final ObjectMapper objectMapper;

    public ServiceBusOrderPublisher() {
        this.objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(connectionString)) {
            try {
                senderClient = new ServiceBusClientBuilder()
                        .connectionString(connectionString)
                        .sender()
                        .queueName(queueName)
                        .buildClient();
                log.info("ServiceBusOrderPublisher initialized for queue: {}", queueName);
            } catch (Exception e) {
                log.warn("Failed to initialize Service Bus sender client. Order publishing will be skipped. Error: {}", e.getMessage());
                senderClient = null;
            }
        } else {
            log.info("SERVICEBUS_CONNECTION_STRING not set — order publishing to Service Bus is disabled (local dev mode)");
        }
    }

    /**
     * Publish an order update message to the Service Bus queue.
     * Called every time a customer updates their shopping cart.
     *
     * @param order   the current state of the order (after update)
     * @param sessionId the session ID, used as blob file name in the reserver function
     */
    public void publishOrderUpdate(Order order, String sessionId) {
        if (senderClient == null) {
            log.debug("Service Bus sender not available — skipping order publish for session: {}", sessionId);
            return;
        }

        try {
            String messageBody = objectMapper.writeValueAsString(order);
            ServiceBusMessage message = new ServiceBusMessage(messageBody)
                    .setMessageId(sessionId)                        // idempotency key
                    .setSessionId(sessionId)                        // used for session-based routing if needed
                    .setSubject("order-update")
                    .setContentType("application/json");

            // Add session ID as custom property so the function can read it directly
            message.getApplicationProperties().put("sessionId", sessionId);

            senderClient.sendMessage(message);
            log.info("Published order update to Service Bus queue '{}' for session: {}", queueName, sessionId);
        } catch (Exception e) {
            // Publishing failure must NOT break the main cart update flow
            log.error("Failed to publish order to Service Bus for session {}. Order flow continues normally. Error: {}",
                    sessionId, e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (senderClient != null) {
            try {
                senderClient.close();
                log.info("ServiceBusOrderPublisher sender client closed");
            } catch (Exception e) {
                log.warn("Error closing Service Bus sender: {}", e.getMessage());
            }
        }
    }
}

