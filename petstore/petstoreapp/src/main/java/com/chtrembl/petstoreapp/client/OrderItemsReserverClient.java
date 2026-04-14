package com.chtrembl.petstoreapp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that calls the OrderItemsReserver Azure Function.
 * Posts order JSON to the function on every cart update.
 * The function saves it as a blob (order-{sessionId}.json) in Azure Blob Storage.
 *
 * Configure via environment variable:
 *   ORDERITEMSRESERVER_URL — full function URL including the function key code param
 *   e.g. https://orderitemsreserver22.azurewebsites.net/api/reserveOrderItems?code=YOUR_KEY
 *   For local testing: http://localhost:7071/api/reserveOrderItems
 */
@Component
@Slf4j
public class OrderItemsReserverClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${petstore.service.orderitemsreserver.url:}")
    private String orderItemsReserverUrl;

    /**
     * Sends the order JSON to the OrderItemsReserver Azure Function.
     * This is a best-effort call: if the function is unavailable, the error is
     * logged but the cart update in PetStoreApp is NOT rolled back.
     *
     * @param orderJson serialized order JSON (contains session ID, products, email)
     */
    public void reserveOrderItems(String orderJson) {
        if (orderItemsReserverUrl == null || orderItemsReserverUrl.isBlank()) {
            log.warn("OrderItemsReserver URL is not configured (ORDERITEMSRESERVER_URL). Skipping blob reservation.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(orderJson, headers);

            String response = restTemplate.postForObject(orderItemsReserverUrl, request, String.class);
            log.info("OrderItemsReserver responded: {}", response);

        } catch (Exception e) {
            // Non-fatal: log and continue — cart update should not fail due to blob upload issues
            log.error("Failed to call OrderItemsReserver at {}: {}", orderItemsReserverUrl, e.getMessage());
        }
    }
}
