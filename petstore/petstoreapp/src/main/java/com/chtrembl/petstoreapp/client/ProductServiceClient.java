package com.chtrembl.petstoreapp.client;

import com.chtrembl.petstoreapp.config.FeignConfig;
import com.chtrembl.petstoreapp.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "product-service",
        url = "${petstore.service.product.url}",
        configuration = FeignConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/petstoreproductservice/v2/product/findByStatus")
    List<Product> getProductsByStatus(@RequestParam("status") String status);

    @GetMapping("/petstoreproductservice/v2/health")
    String getHealth();
}