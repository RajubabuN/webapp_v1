package com.chtrembl.petstore.orderitemsreserver;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.chtrembl.petstore.orderitemsreserver.model.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * OrderItemsReserver Azure Function.
 *
 * Accepts an HTTP POST with the order JSON from PetStoreApp.
 * Uploads (or overwrites) a JSON blob named "order-{sessionId}.json"
 * in Azure Blob Storage so that every cart update is persisted.
 *
 * Required Application Settings in Azure Portal:
 *   BLOB_CONNECTION_STRING  — Storage Account connection string
 *   BLOB_CONTAINER_NAME     — Blob container name (default: orders)
 */
public class OrderItemsReserverFunction {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_CONTAINER = "orders";

    @FunctionName("reserveOrderItems")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "reserveOrderItems"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("OrderItemsReserver function triggered.");

        // --- 1. Validate request body ---
        String body = request.getBody().orElse(null);
        if (body == null || body.isBlank()) {
            logger.warning("Empty request body received.");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Request body is required\"}")
                    .build();
        }

        // --- 2. Parse the order JSON to get the session ID ---
        OrderRequest orderRequest;
        try {
            orderRequest = OBJECT_MAPPER.readValue(body, OrderRequest.class);
        } catch (Exception e) {
            logger.warning("Failed to parse order JSON: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Invalid JSON body: " + e.getMessage() + "\"}")
                    .build();
        }

        String sessionId = orderRequest.getId();
        if (sessionId == null || sessionId.isBlank()) {
            logger.warning("Order JSON is missing the 'id' (sessionId) field.");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Order 'id' (sessionId) is required\"}")
                    .build();
        }

        logger.info("Processing order for session: " + sessionId
                + ", products: " + (orderRequest.getProducts() != null ? orderRequest.getProducts().size() : 0));

        // --- 3. Upload to Blob Storage ---
        try {
            String connectionString = System.getenv("BLOB_CONNECTION_STRING");
            if (connectionString == null || connectionString.isBlank()) {
                throw new IllegalStateException("BLOB_CONNECTION_STRING environment variable is not configured.");
            }

            String containerName = System.getenv("BLOB_CONTAINER_NAME");
            if (containerName == null || containerName.isBlank()) {
                containerName = DEFAULT_CONTAINER;
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
                logger.info("Created blob container: " + containerName);
            }

            // File name uses session ID so it is overwritten on every cart update
            String blobName = "order-" + sessionId + ".json";
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            byte[] data = body.getBytes(StandardCharsets.UTF_8);
            // overwrite = true ensures the file is replaced on every update
            blobClient.upload(new ByteArrayInputStream(data), data.length, true);

            logger.info("Successfully uploaded blob: " + blobName + " (" + data.length + " bytes)");

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"message\":\"Order reserved\",\"blob\":\"" + blobName + "\"}")
                    .build();

        } catch (IllegalStateException e) {
            logger.severe("Configuration error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            logger.severe("Failed to upload order to blob storage: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Failed to reserve order: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
