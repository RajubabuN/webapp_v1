package com.chtrembl.petstoreapp.client;

import com.chtrembl.petstoreapp.config.FeignConfig;
import com.chtrembl.petstoreapp.model.Pet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "pet-service",
        url = "${petstore.service.pet.url}",
        configuration = FeignConfig.class
)
public interface PetServiceClient {

    @GetMapping("/petstorepetservice/v2/pet/findByStatus")
    List<Pet> getPetsByStatus(@RequestParam("status") String status);

    @GetMapping("/petstorepetservice/v2/health")
    String getHealth();
}