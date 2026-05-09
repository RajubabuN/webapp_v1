package com.chtrembl.petstore.orderreserver.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.util.logging.Logger;

/**
 * Handles uploading order JSON snapshots to Azure Blob Storage.
 * Each order is stored as  {sessionId}.json  in the configured container.
 * Uploading with overwrite=true ensures the file is always the latest cart state.
 */
public class BlobStorageService {

    private final String connectionString;
    private final String containerName;
    private final Logger logger;

    public BlobStorageService(String connectionString, String containerName, Logger logger) {
        this.connectionString = connectionString;
        this.containerName = containerName;
        this.logger = logger;
    }

    /**
     * Upload (or overwrite) a JSON file for the given session's order.
     *
     * @param sessionId  used as blob name: {sessionId}.json
     * @param jsonContent the full order JSON string
     * @throws Exception if upload fails (caller handles retries)
     */
    public void uploadOrderJson(String sessionId, String jsonContent) throws Exception {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Create container if it does not exist yet
        if (!containerClient.exists()) {
            containerClient.create();
            logger.info("Created blob container: " + containerName);
        }

        String blobName = sessionId + ".json";
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // overwrite = true so the file is always replaced with the latest cart state
        blobClient.upload(BinaryData.fromString(jsonContent), true);

        logger.info("Uploaded order snapshot to blob: " + blobName
                + " in container: " + containerName
                + " (size: " + jsonContent.length() + " bytes)");
    }
}

