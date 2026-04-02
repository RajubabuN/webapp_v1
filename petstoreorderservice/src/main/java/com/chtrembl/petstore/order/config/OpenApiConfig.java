package com.chtrembl.petstore.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        Info info = new Info()
                .title("Pet Store Order Service API")
                .version("v2")
                .description("This API exposes endpoints for managing Pet Store orders");

        return new OpenAPI()
                .info(info);
    }
}