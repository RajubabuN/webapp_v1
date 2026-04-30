package com.chtrembl.petstore.product.controller;

import com.chtrembl.petstore.product.model.Product;
import com.chtrembl.petstore.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/petstoreproductservice/v2")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Product", description = "Pet Store Product API")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Find products by status",
            description = "Returns a list of products filtered by their status (available, pending, sold)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/product/findByStatus")
    public ResponseEntity<List<Product>> findProductsByStatus(
            @Parameter(description = "Status values that need to be considered for filter",
                    required = true,
                    example = "available")
            @RequestParam(value = "status", required = true) List<String> status) {

        log.info("Received GET request to /petstoreproductservice/v2/product/findByStatus with status: {}", status);

        try {
            List<Product> products = productService.findProductsByStatus(status);
            log.info("Successfully found {} products with status: {}", products.size(), status);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error occurred while finding products by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Find product by ID",
            description = "Returns a single product by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID of product to return", required = true, example = "1")
            @PathVariable("productId") Long productId) {

        log.info("Received GET request to /petstoreproductservice/v2/product/{}", productId);

        return productService.findProductById(productId)
                .map(product -> {
                    log.info("Successfully found product: id={}, name='{}'", product.getId(), product.getName());
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    log.warn("Product with id {} not found", productId);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(
            summary = "Get all products",
            description = "Returns a list of all available products"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All products retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/product/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Received GET request to /petstoreproductservice/v2/product/all");

        try {
            List<Product> products = productService.getAllProducts();
            log.info("Successfully retrieved all products, count: {}", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error occurred while retrieving all products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}