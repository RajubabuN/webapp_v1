package com.chtrembl.petstoreapp.service;

import com.chtrembl.petstoreapp.client.PetServiceClient;
import com.chtrembl.petstoreapp.exception.PetServiceException;
import com.chtrembl.petstoreapp.model.Category;
import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.Pet;
import com.chtrembl.petstoreapp.model.User;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chtrembl.petstoreapp.config.Constants.CATEGORY;
import static com.chtrembl.petstoreapp.config.Constants.OPERATION;
import static com.chtrembl.petstoreapp.config.Constants.REQUEST_ID;
import static com.chtrembl.petstoreapp.config.Constants.TRACE_ID;
import static com.chtrembl.petstoreapp.model.Status.AVAILABLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetManagementService {

    private final User sessionUser;
    private final ContainerEnvironment containerEnvironment;
    private final PetServiceClient petServiceClient;

    public Collection<Pet> getPetsByCategory(String category) {
        List<Pet> pets;

        MDC.put(OPERATION, "getPets");
        MDC.put(CATEGORY, category);

        String requestId = MDC.get(REQUEST_ID);
        String traceId = MDC.get(TRACE_ID);

        log.info("Starting pet retrieval operation [RequestID: {}, TraceID: {}, Category: {}]",
                requestId, traceId, category);
        
        try {
            this.sessionUser.getTelemetryClient().trackEvent(
                    String.format("PetStoreApp user %s is requesting to retrieve pets from the PetStorePetService",
                            this.sessionUser.getName()),
                    this.sessionUser.getCustomEventProperties(), null);

            pets = petServiceClient.getPetsByStatus(AVAILABLE.getValue());
            this.sessionUser.setPets(pets);

            pets = pets.stream()
                    .filter(pet -> category.equals(pet.getCategory().getName()))
                    .toList();

            log.info("Successfully retrieved {} pets for category {} [RequestID: {}, TraceID: {}]",
                    pets.size(), category, requestId, traceId);

            return pets;
        } catch (FeignException fe) {
            log.error("Feign error retrieving pets [RequestID: {}, TraceID: {}, Category: {}, HTTP: {}, Message: {}]",
                    requestId, traceId, category, fe.status(), fe.getMessage(), fe);

            this.sessionUser.getTelemetryClient().trackException(fe);
            this.sessionUser.getTelemetryClient().trackEvent(
                    String.format("PetStoreApp %s received Feign error %s (HTTP %d), container host: %s",
                            this.sessionUser.getName(),
                            fe.getMessage(),
                            fe.status(),
                            this.containerEnvironment.getContainerHostName())
            );
            log.error("Failed to retrieve pets from PetStorePetService via Feign client", fe);
            throw new PetServiceException("Unable to retrieve pets from the PetStorePetService", fe);
        } catch (Exception e) {
            log.error("Unexpected error when retrieving pets [RequestID: {}, TraceID: {}, Category: {}]",
                    requestId, traceId, category, e);
            return createErrorPetCollection(e);
        } finally {
            MDC.remove(OPERATION);
            MDC.remove(CATEGORY);
        }
    }

    private Collection<Pet> createErrorPetCollection(Exception e) {
        List<Pet> pets = new ArrayList<>();
        Pet errorPet = new Pet();
        errorPet.setName("petstore.service.url:${PETSTOREPETSERVICE_URL} needs to be enabled for this service to work: "
                + e.getMessage());
        errorPet.setPhotoURL("");
        errorPet.setCategory(new Category());
        errorPet.setId((long) 0);
        pets.add(errorPet);
        return pets;
    }
}
