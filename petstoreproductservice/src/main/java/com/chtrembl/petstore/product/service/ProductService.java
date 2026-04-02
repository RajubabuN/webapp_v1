package com.chtrembl.petstore.product.service;

import com.chtrembl.petstore.product.model.DataPreload;
import com.chtrembl.petstore.product.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final DataPreload dataPreload;

    public List<Product> findProductsByStatus(List<String> status) {
        log.info("Finding products with status: {}", status);

        return dataPreload.getProducts().stream()
                .filter(product -> status.contains(product.getStatus().getValue()))
                .toList();
    }

    public Optional<Product> findProductById(Long productId) {
        log.info("Finding product with id: {}", productId);

        return dataPreload.getProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst();
    }

    public List<Product> getAllProducts() {
        log.info("Getting all products");
        return dataPreload.getProducts();
    }

    public int getProductCount() {
        return dataPreload.getProducts().size();
    }
}