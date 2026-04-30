package com.chtrembl.petstoreapp.controller;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.Order;
import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.service.PetStoreFacadeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Controller for shopping cart and order management.
 * Handles cart display, item updates, and order completion.
 */
@Controller
@Slf4j
public class ShoppingCartController extends BaseController {

    private static final String MODEL_CART_SIZE = "cartSize";
    private static final String MODEL_EMAIL = "email";
    private static final String MODEL_ERROR = "error";
    private static final String MODEL_ORDER = "order";
    private static final String MODEL_STACKTRACE = "stacktrace";
    private static final String MODEL_USER_LOGGED_IN = "userLoggedIn";

    private static final String VIEW_CART = "cart";
    private static final String VIEW_REDIRECT_CART = "redirect:cart";

    private final PetStoreFacadeService petStoreService;

    public ShoppingCartController(ContainerEnvironment containerEnvironment,
                                  User sessionUser,
                                  CacheManager currentUsersCacheManager,
                                  PetStoreFacadeService petStoreService) {
        super(containerEnvironment, sessionUser, currentUsersCacheManager);
        this.petStoreService = petStoreService;
    }

    /**
     * Display shopping cart contents.
     * Shows current cart items, order summary, and checkout options.
     */
    @GetMapping(value = "/cart")
    public String cart(Model model, OAuth2AuthenticationToken token, HttpServletRequest request) {

        log.debug("PetStoreApp /cart requested by user: {}", sessionUser.getName());

        trackPageView(request, "cart");

        try {
            // Retrieve current order for the session
            Order order = this.petStoreService.retrieveOrder(sessionUser.getSessionId());
            model.addAttribute(MODEL_ORDER, order);

            // Calculate and update cart size
            int cartSize = 0;
            if (order != null && order.getProducts() != null && !order.isComplete()) {
                cartSize = order.getProducts().size();
            }
            sessionUser.setCartCount(cartSize);
            model.addAttribute(MODEL_CART_SIZE, sessionUser.getCartCount());

            // Add user authentication status
            if (token != null) {
                model.addAttribute(MODEL_USER_LOGGED_IN, true);
                model.addAttribute(MODEL_EMAIL, sessionUser.getEmail());
                log.debug("Authenticated user accessing cart: {}", sessionUser.getEmail());
            } else {
                log.debug("Anonymous user accessing cart");
            }

            log.info("Cart loaded for user: {}, items: {}, order status: {}",
                    sessionUser.getName(), cartSize,
                    order != null ? (order.isComplete() ? "complete" : "active") : "empty");

        } catch (Exception ex) {
            log.error("Error loading cart for user {}: ", sessionUser.getName(), ex);
            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't load your cart.");
            model.addAttribute(MODEL_STACKTRACE, getStackTrace(ex));
        }

        return VIEW_CART;
    }

    /**
     * Update cart contents.
     * Handles adding, removing, and modifying cart items.
     */
    @PostMapping(value = "/updatecart")
    public String updateCart(Model model,
                             OAuth2AuthenticationToken token,
                             HttpServletRequest request,
                             @RequestParam Map<String, String> params) {

        String productIdStr = params.get("productId");
        String operator = params.get("operator");

        log.debug("PetStoreApp /updatecart requested by user: {}, productId: {}, operator: {}",
                sessionUser.getName(), productIdStr, operator);

        try {
            // Validate and parse product ID
            if (StringUtils.isEmpty(productIdStr)) {
                throw new IllegalArgumentException("Product ID is required");
            }

            long productId = Long.parseLong(productIdStr);

            // Determine cart count based on operator
            int cartCount = 1; // Default: add one item

            if (StringUtils.isNotEmpty(operator)) {
                switch (operator) {
                    case "minus":
                        cartCount = -1;
                        log.debug("Decreasing quantity for product: {}", productId);
                        break;
                    case "remove":
                        cartCount = -999; // Special value for complete removal
                        log.debug("Removing product from cart: {}", productId);
                        break;
                    case "plus":
                        cartCount = 1;
                        log.debug("Increasing quantity for product: {}", productId);
                        break;
                    default:
                        log.debug("Adding product to cart: {}", productId);
                        break;
                }
            }

            // Update the order
            this.petStoreService.updateOrder(productId, cartCount, false);

            log.info("Cart updated successfully for user: {}, product: {}, operation: {}",
                    sessionUser.getName(), productId, operator != null ? operator : "add");

        } catch (NumberFormatException ex) {
            log.error("Invalid product ID format: {}", productIdStr, ex);
            model.addAttribute(MODEL_ERROR, "Invalid product ID provided.");
        } catch (Exception ex) {
            log.error("Error updating cart for user {}: ", sessionUser.getName(), ex);
            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't update your cart.");
        }

        return VIEW_REDIRECT_CART;
    }

    /**
     * Complete the current order.
     * Finalizes the order for authenticated users.
     */
    @PostMapping(value = "/completecart")
    public String completeCart(Model model,
                               OAuth2AuthenticationToken token,
                               HttpServletRequest request) {

        log.debug("PetStoreApp /completecart requested by user: {}", sessionUser.getName());

        try {
            // Only allow order completion for authenticated users
            if (token != null) {
                this.petStoreService.updateOrder(0, 0, true);

                log.info("Order completed successfully for user: {}", sessionUser.getName());
            } else {
                log.warn("Anonymous user attempted to complete order");
                model.addAttribute(MODEL_ERROR, "You must be logged in to complete an order.");
            }

        } catch (Exception ex) {
            log.error("Error completing order for user {}: ", sessionUser.getName(), ex);
            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't complete your order.");
        }

        return VIEW_REDIRECT_CART;
    }
}