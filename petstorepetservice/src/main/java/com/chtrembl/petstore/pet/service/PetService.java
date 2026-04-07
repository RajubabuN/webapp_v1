package com.chtrembl.petstore.pet.service;

import com.chtrembl.petstore.pet.model.DataPreload;
import com.chtrembl.petstore.pet.model.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PetService {

    private final DataPreload dataPreload;

    public List<Pet> findPetsByStatus(List<String> status) {
        log.info("Finding pets with status: {}", status);

        return dataPreload.getPets().stream()
                .filter(pet -> status.contains(pet.getStatus().getValue()))
                .toList();
    }

    public Optional<Pet> findPetById(Long petId) {
        log.info("Finding pet with id: {}", petId);

        return dataPreload.getPets().stream()
                .filter(pet -> pet.getId().equals(petId))
                .findFirst();
    }

    public List<Pet> getAllPets() {
        log.info("Getting all pets");
        return dataPreload.getPets();
    }

    public int getPetCount() {
        return dataPreload.getPets().size();
    }
}