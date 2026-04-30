package com.chtrembl.petstoreapp.client;

import com.chtrembl.petstoreapp.config.FeignConfig;
import com.chtrembl.petstoreapp.model.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "order-service",
        url = "${petstore.service.order.url}",
        configuration = FeignConfig.class
)
public interface OrderServiceClient {

    @PostMapping("/petstoreorderservice/v2/store/order")
    Order createOrUpdateOrder(@RequestBody String orderJson);

    @GetMapping("/petstoreorderservice/v2/store/order/{orderId}")
    Order getOrder(@PathVariable("orderId") String orderId);

    @GetMapping("/petstoreorderservice/v2/health")
    String getHealth();
}