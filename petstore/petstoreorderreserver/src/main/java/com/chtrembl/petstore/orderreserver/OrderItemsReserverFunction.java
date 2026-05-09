package com.chtrembl.petstore.orderreserver;

import com.chtrembl.petstore.orderreserver.model.OrderMessage;
import com.chtrembl.petstore.orderreserver.service.BlobStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Azure Function: OrderItemsReserver
 *
 * Trigger  : Azure Service Bus queue ("order-queue")
 * On success: uploads order as {sessionId}.json to Blob Storage (overwrites on each cart update)
 * On failure: after 3 retry attempts, explicitly dead-letters the message so
 *             Azure Logic Apps can pick it up from the DLQ and notify the manager.
 *
 * Environment variables required:
 *   SERVICEBUS_CONNECTION_STRING  – connection string for the Service Bus namespace
 *   ORDER_QUEUE_NAME              – queue name (default: order-queue)
 *   BLOB_STORAGE_CONNECTION_STRING – storage account connection string
 *   BLOB_CONTAINER_NAME           – blob container name (default: order-snapshots)
 */
public class OrderItemsReserverFunction {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000L; // 1 s, doubles each attempt

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("OrderItemsReserver")
    public void run(
            @ServiceBusQueueTrigger(
                    name = "message",
                    queueName = "%ORDER_QUEUE_NAME%",
                    connection = "SERVICEBUS_CONNECTION_STRING"
            )
            String messageBody,

            // Metadata injected by the Service Bus binding
            @BindingName("MessageId") String messageId,
            @BindingName("ApplicationProperties") Map<String, Object> applicationProperties,

            ExecutionContext context
    ) {
        Logger log = context.getLogger();
        log.info("OrderItemsReserver triggered. MessageId=" + messageId);

        // ── 1. Parse message ────────────────────────────────────────────────────
        OrderMessage order;
        try {
            order = objectMapper.readValue(messageBody, OrderMessage.class);
        } catch (Exception e) {
            // Malformed JSON — throw immediately so Service Bus dead-letters via maxDeliveryCount
            log.severe("Cannot parse Service Bus message. Error: " + e.getMessage()
                    + " — throwing so SB dead-letters the message.");
            throw new RuntimeException("PARSE_ERROR: " + e.getMessage(), e);
        }

        // Session ID is the blob file name (same as order id / session id set by petstoreapp)
        String sessionId = getSessionId(order, applicationProperties);
        if (sessionId == null || sessionId.isBlank()) {
            log.severe("Session ID is missing in message — throwing so SB dead-letters.");
            throw new RuntimeException("MISSING_SESSION_ID: sessionId is null or blank in message " + messageId);
        }

        log.info("Processing order snapshot for session: " + sessionId);

        // ── 2. Build enriched JSON to store ─────────────────────────────────────
        String orderJson;
        try {
            // Add a server-side timestamp to the snapshot
            orderJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(Map.of(
                            "sessionId", sessionId,
                            "snapshotTimestamp", Instant.now().toString(),
                            "order", order
                    ));
        } catch (Exception e) {
            log.severe("Failed to serialize order to JSON: " + e.getMessage());
            throw new RuntimeException("SERIALIZATION_ERROR: " + e.getMessage(), e);
        }

        // ── 3. Upload to Blob Storage with up to 3 retries ──────────────────────
        BlobStorageService blobService = createBlobStorageService(log);
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                blobService.uploadOrderJson(sessionId, orderJson);
                log.info("Successfully uploaded order snapshot on attempt " + attempt
                        + " for session: " + sessionId);
                return; // ✅ success — Service Bus auto-completes the message
            } catch (Exception e) {
                lastException = e;
                log.warning("Blob upload attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS
                        + " failed for session " + sessionId + ": " + e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delay = RETRY_DELAY_MS * attempt; // 1 s, 2 s
                    log.info("Waiting " + delay + "ms before retry attempt " + (attempt + 1));
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // ── 4. All retries exhausted — throw so Service Bus dead-letters the message ────
        String failureReason = lastException != null ? lastException.getMessage() : "unknown error";
        log.severe("All " + MAX_RETRY_ATTEMPTS + " blob upload attempts failed for session "
                + sessionId + ". Throwing exception — Service Bus will dead-letter via maxDeliveryCount=1. Reason: "
                + failureReason);

        // Throwing causes the Functions runtime to abandon the message.
        // With maxDeliveryCount=1 set on the queue, Service Bus immediately moves it to DLQ.
        throw new RuntimeException(
                "BLOB_UPLOAD_FAILED_AFTER_" + MAX_RETRY_ATTEMPTS + "_RETRIES"
                        + " | Session: " + sessionId
                        + " | Error: " + failureReason,
                lastException);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────


    private BlobStorageService createBlobStorageService(Logger log) {
        String connStr = System.getenv("BLOB_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("BLOB_CONTAINER_NAME");
        if (containerName == null || containerName.isBlank()) {
            containerName = "order-snapshots";
        }
        return new BlobStorageService(connStr, containerName, log);
    }

    private String getSessionId(OrderMessage order, Map<String, Object> applicationProperties) {
        // Prefer the explicit application property set by petstoreapp publisher
        if (applicationProperties != null && applicationProperties.containsKey("sessionId")) {
            Object val = applicationProperties.get("sessionId");
            if (val != null && !val.toString().isBlank()) {
                return val.toString();
            }
        }
        // Fall back to the order's id field (which equals the session id)
        return order.getId();
    }
}

