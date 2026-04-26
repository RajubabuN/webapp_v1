package com.chtrembl.petstore.order.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.chtrembl.petstore.order.model.Order;
import org.springframework.stereotype.Repository;

/**
 * CosmosDB repository for persistent Order storage.
 * Provides CRUD operations backed by Azure Cosmos DB (NoSQL API).
 * The in-memory ConcurrentMapCache is still used as a fast read-through layer.
 */
@Repository
public interface OrderRepository extends CosmosRepository<Order, String> {
}

