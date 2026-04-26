package com.chtrembl.petstore.order.config;

/**
 * CosmosDB configuration is handled automatically by Spring Cloud Azure auto-configuration.
 * @EnableCosmosRepositories is placed on OrderServiceApplication to avoid bean name conflict
 * with the 'cosmosConfig' bean registered by CosmosDataConfiguration auto-configuration.
 */
public class CosmosConfig {
    // intentionally empty — no @Configuration annotation
}
