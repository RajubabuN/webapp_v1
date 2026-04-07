package com.chtrembl.petstore.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@Slf4j
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(new TracingInterceptor()));
        log.info("RestTemplate configured with TracingInterceptor for automatic header propagation");
        return restTemplate;
    }

    @Bean
    public TracingInterceptor tracingInterceptor() {
        return new TracingInterceptor();
    }
}