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
 * Controller for pet breeds management.
 * Handles pet browsing, breed listing, and breed details.
 */
@Controller
@Slf4j
public class PetController extends BaseController {

    private static final String MODEL_ERROR = "error";
    private static final String MODEL_PET = "pet";
    private static final String MODEL_PETS = "pets";
    private static final String MODEL_STACKTRACE = "stacktrace";

    private static final String VIEW_HOME = "home";
    private static final String VIEW_BREEDS = "breeds";
    private static final String VIEW_BREED_DETAILS = "breeddetails";

    private final PetStoreFacadeService petStoreService;

    public PetController(ContainerEnvironment containerEnvironment,
                         User sessionUser,
                         CacheManager currentUsersCacheManager,
                         PetStoreFacadeService petStoreService) {
        super(containerEnvironment, sessionUser, currentUsersCacheManager);
        this.petStoreService = petStoreService;
    }

    /**
     * Display pets by category (dogs, cats, fish).
     * Supports multiple breed endpoints for different pet types.
     */
    @GetMapping(value = {"/dogbreeds", "/catbreeds", "/fishbreeds"})
    public String breeds(Model model,
                         OAuth2AuthenticationToken token,
                         HttpServletRequest request,
                         @RequestParam(name = "category") String category) throws URISyntaxException {

        sessionUser.getTelemetryClient().trackMetric("TestMetric", System.currentTimeMillis() % 100);

        trackPageView(request, category.toLowerCase() + "breeds");

        sessionUser.getTelemetryClient().trackMetric("PageViews_" + category, 1);

        if (!isValidCategory(category)) {
            log.warn("Invalid category requested: {}", category);
            sessionUser.getTelemetryClient().trackMetric("InvalidCategoryRequests", 1);
            return VIEW_HOME;
        }

        log.debug("PetStoreApp /{} breeds requested for category: {}",
                category.toLowerCase(), category);

        try {
            long startTime = System.currentTimeMillis();

            final Collection<Pet> pets = this.petStoreService.getPets(category);
            model.addAttribute(MODEL_PETS, pets);

            long duration = System.currentTimeMillis() - startTime;

            sessionUser.getTelemetryClient().trackMetric("PetLoadDuration_" + category, duration);
            sessionUser.getTelemetryClient().trackMetric("PetsFound_" + category, pets != null ? pets.size() : 0);
            sessionUser.getTelemetryClient().trackMetric("TotalPetsLoaded", pets != null ? pets.size() : 0);

            log.info("Successfully loaded {} pets for category: {}",
                    pets != null ? pets.size() : 0, category);

        } catch (Exception ex) {
            log.error("Error loading pets from service for category {}: ", category, ex);
            sessionUser.getTelemetryClient().trackMetric("PetLoadErrors", 1);
            sessionUser.getTelemetryClient().trackMetric("PetLoadErrors_" + category, 1);

            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't load pet breeds.");
            model.addAttribute(MODEL_STACKTRACE, getStackTrace(ex));
        }

        return VIEW_BREEDS;
    }

    /**
     * Display detailed information about a specific breed.
     * Shows breed details and available product categories.
     */
    @GetMapping(value = "/breeddetails")
    public String breedDetails(Model model,
                               OAuth2AuthenticationToken token,
                               HttpServletRequest request,
                               @RequestParam(name = "category") String category,
                               @RequestParam(name = "id") int id) throws URISyntaxException {

        if (!isValidCategory(category)) {
            log.warn("Invalid category requested for breed details: {}", category);
            return VIEW_HOME;
        }

        log.debug("PetStoreApp /breeddetails requested for category: {}, id: {}", category, id);

        trackPageView(request, "breeddetails");

        try {
            if (sessionUser.getPets() == null) {
                log.debug("Pets not loaded in session, loading for category: {}", category);
                this.petStoreService.getPets(category);
            }

            if (sessionUser.getPets() == null || sessionUser.getPets().isEmpty()) {
                throw new IllegalArgumentException("No pets available in session");
            }

            if (id < 1 || id > sessionUser.getPets().size()) {
                throw new IllegalArgumentException("Invalid pet ID: " + id);
            }

            Pet pet = sessionUser.getPets().get(id - 1);

            log.debug("PetStoreApp /breeddetails requested for {}, routing to breeddetails view...",
                    pet.getName());

            model.addAttribute(MODEL_PET, pet);

        } catch (Exception ex) {
            log.error("Error loading pet details for category {}, id {}: ", category, id, ex);
            model.addAttribute(MODEL_ERROR, "Sorry, we couldn't load pet details.");
            model.addAttribute(MODEL_STACKTRACE, getStackTrace(ex));
        }

        return VIEW_BREED_DETAILS;
    }

    /**
     * Validate if the provided category is supported.
     */
    private boolean isValidCategory(String category) {
        return "Dog".equals(category) || "Cat".equals(category) || "Fish".equals(category);
    }
}