package com.chtrembl.petstore.pet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petStoreOpenAPI() {
        Info info = new Info()
                .title("Pet Store Pet Service API")
                .version("v2")
                .description("This API exposes endpoints for managing Pet Store pets");

        return new OpenAPI()
                .info(info);
    }
}