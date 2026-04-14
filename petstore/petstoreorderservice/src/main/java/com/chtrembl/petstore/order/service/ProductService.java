package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petstore.service.product.url:http://localhost:8082}")
    private String productServiceUrl;

    @Cacheable("products")
    public List<Product> getAvailableProducts() {
        log.info("Retrieving products from: {}/petstoreproductservice/v2/product/findByStatus?status=available",
                productServiceUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    String.format("%s/petstoreproductservice/v2/product/findByStatus?status=available", productServiceUrl),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            List<Product> products = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });

            log.info("Successfully retrieved {} products from product service", products.size());
            return products;

        } catch (Exception e) {
            log.error("Error retrieving products from product service: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }
}
