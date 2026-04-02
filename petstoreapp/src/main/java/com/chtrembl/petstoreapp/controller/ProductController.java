package com.chtrembl.petstoreapp.controller;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.Pet;
import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.service.PetStoreFacadeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Controller for product catalog management.
 * Handles product listing and product-related operations.
 */
@Controller
@Slf4j
public class ProductController extends BaseController {

    private static final String MODEL_ERROR = "error";
    private static final String MODEL_PRODUCTS = "products";
    private static final String MODEL_STACKTRACE = "stacktrace";

    private static final String VIEW_HOME = "home";
    private static final String VIEW_PRODUCTS = "products";

    private final PetStoreFacadeService petStoreService;

    public ProductController(ContainerEnvironment containerEnvironment,
                             User sessionUser,
                             CacheManager currentUsersCacheManager,
                             PetStoreFacadeService petStoreService) {
        super(containerEnvironment, sessionUser, currentUsersCacheManager);
        this.petStoreService = petStoreService;
    }

    /**
     * Display products for a specific pet breed and category.
     * Shows available products (toys, food) for the selected pet.
     */
    @GetMapping(value = "/products")
    public String products(Model model,
                           OAuth2AuthenticationToken token,
                           HttpServletRequest request,
                           @RequestParam(name = "category") String category,
                           @RequestParam(name = "id") int id) throws URISyntaxException {

        // Validate product category
        if (!isValidProductCategory(category)) {
            log.warn("Invalid product category requested: {}", category);
            return VIEW_HOME;
        }

        log.debug("PetStoreApp /products requested for category: {}, pet id: {}", category, id);

        trackPageView(request, "products");

        try {
            // Ensure pets are loaded in session
            if (sessionUser.getPets() == null || sessionUser.getPets().isEmpty()) {
                log.warn("No pets available in session for products request");
                throw new IllegalStateException("Pet information not available. Please select a pet breed first.");
            }

            // Validate pet ID
            if (id < 1 || id > sessionUser.getPets().size()) {
                throw new IllegalArgumentException("Invalid pet ID: " + id);
            }

            Pet pet = sessionUser.getPets().get(id - 1);

            log.debug("PetStoreApp /products requested for category: {}, pet: {}", category, pet.getName());

            // Build product category search term
            String productCategory = pet.getCategory().getName() + " " + category;

            // Retrieve products for the specified category and pet
            Collection<?> products = this.petStoreService.getProducts(productCategory, pet.getTags());

            model.addAttribute(MODEL_PRODUCTS, products);

            log.info("Successfully loaded {} products for category: {}, pet: {}",
                    products != null ? products.size() : 0, category, pet.getName());

        } catch (Exception ex) {
            log.error("Error loading products for category {}, id {}: ", category, id, ex);
            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't load products.");
            model.addAttribute(MODEL_STACKTRACE, getStackTrace(ex));
        }

        return VIEW_PRODUCTS;
    }

    /**
     * Validate if the provided product category is supported.
     */
    private boolean isValidProductCategory(String category) {
        return "Toy".equals(category) || "Food".equals(category);
    }
}