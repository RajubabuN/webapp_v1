package com.chtrembl.petstore.order.controller;

import com.chtrembl.petstore.order.model.ContainerEnvironment;
import com.chtrembl.petstore.order.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/petstoreorderservice/v2")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Info", description = "Pet Store Order Service Information API")
public class InfoController {

    private final ContainerEnvironment containerEnvironment;
    private final CacheService cacheService;

    @Operation(
            summary = "Health check",
            description = "Returns the health status of the service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(mediaType = "application/json"))
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
                "status", "UP",
                "service", "order-service",
                "version", containerEnvironment.getAppVersion(),
                "date", containerEnvironment.getAppDate(),
                "container", containerEnvironment.getContainerHostName()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Service information",
            description = "Returns detailed information about the order service"
    )
    @ApiResponse(responseCode = "200", description = "Service information retrieved successfully",
            content = @Content(mediaType = "application/json"))
    @GetMapping("/store/info")
    public ResponseEntity<Map<String, String>> serviceInfo() {
        log.info("Incoming GET request to /petstoreorderservice/v2/store/info");

        Map<String, String> response = Map.of(
                "service", "order service",
                "version", containerEnvironment.getAppVersion(),
                "container", containerEnvironment.getContainerHostName(),
                "ordersCacheSize", String.valueOf(cacheService.getOrdersCacheSize())
        );

        return ResponseEntity.ok(response);
    }
}