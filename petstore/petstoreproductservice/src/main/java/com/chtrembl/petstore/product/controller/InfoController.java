package com.chtrembl.petstore.product.controller;

import com.chtrembl.petstore.product.model.ContainerEnvironment;
import com.chtrembl.petstore.product.service.ProductService;
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
@RequestMapping("/petstoreproductservice/v2")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Info", description = "Pet Store Product Service Information API")
public class InfoController {

    private final ContainerEnvironment containerEnvironment;
    private final ProductService productService;

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
                "service", "Product Service",
                "version", containerEnvironment.getAppVersion(),
                "date", containerEnvironment.getAppDate(),
                "container", containerEnvironment.getContainerHostName(),
                "productsLoaded", String.valueOf(productService.getProductCount())
        );

        return ResponseEntity.ok(response);
    }
}