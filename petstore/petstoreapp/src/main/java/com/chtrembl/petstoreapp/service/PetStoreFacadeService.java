package com.chtrembl.petstoreapp.service;

import com.chtrembl.petstoreapp.model.Order;
import com.chtrembl.petstoreapp.model.Pet;
import com.chtrembl.petstoreapp.model.Product;
import com.chtrembl.petstoreapp.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetStoreFacadeService {

    private final PetManagementService petManagementService;
    private final ProductManagementService productManagementService;
    private final OrderManagementService orderManagementService;

    public Collection<Pet> getPets(String category) {
        return petManagementService.getPetsByCategory(category);
    }

    public Collection<Product> getProducts(String category, List<Tag> tags) {
        return productManagementService.getProductsByCategory(category, tags);
    }

    public void updateOrder(long productId, int quantity, boolean completeOrder) {
        orderManagementService.updateOrder(productId, quantity, completeOrder);
    }

    public Order retrieveOrder(String orderId) {
        return orderManagementService.retrieveOrder(orderId);
    }
}