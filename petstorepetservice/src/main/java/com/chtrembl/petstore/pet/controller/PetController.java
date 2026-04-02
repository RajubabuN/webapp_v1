package com.chtrembl.petstore.pet.controller;

import com.chtrembl.petstore.pet.model.Pet;
import com.chtrembl.petstore.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/petstorepetservice/v2")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Pet", description = "Pet Store Pet API")
public class PetController {

	private final PetService petService;

	@Operation(
			summary = "Find pets by status",
			description = "Returns a list of pets filtered by their status (available, pending, sold)"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Pets found successfully",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Pet.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
	})
	@GetMapping("/pet/findByStatus")
	public ResponseEntity<List<Pet>> findPetsByStatus(
			@Parameter(description = "Status values that need to be considered for filter",
					required = true,
					example = "available")
			@RequestParam(value = "status", required = true) List<String> status) {

		log.info("Received GET request to /petstorepetservice/v2/pet/findByStatus with status: {}", status);

		try {
			List<Pet> pets = petService.findPetsByStatus(status);
			log.info("Successfully found {} pets with status: {}", pets.size(), status);
			return ResponseEntity.ok(pets);
		} catch (Exception e) {
			log.error("Error occurred while finding pets by status {}: {}", status, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(
			summary = "Find pet by ID",
			description = "Returns a single pet by its ID"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Pet found successfully",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Pet.class))),
			@ApiResponse(responseCode = "404", description = "Pet not found", content = @Content)
	})
	@GetMapping("/pet/{petId}")
	public ResponseEntity<Pet> getPetById(
			@Parameter(description = "ID of pet to return", required = true, example = "1")
			@PathVariable("petId") Long petId) {

		log.info("Received GET request to /petstorepetservice/v2/pet/{}", petId);

		return petService.findPetById(petId)
				.map(pet -> {
					log.info("Successfully found pet: id={}, name='{}'", pet.getId(), pet.getName());
					return ResponseEntity.ok(pet);
				})
				.orElseGet(() -> {
					log.warn("Pet with id {} not found", petId);
					return ResponseEntity.notFound().build();
				});
	}

	@Operation(
			summary = "Get all pets",
			description = "Returns a list of all available pets"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All pets retrieved successfully",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Pet.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
	})
	@GetMapping("/pet/all")
	public ResponseEntity<List<Pet>> getAllPets() {
		log.info("Received GET request to /petstorepetservice/v2/pet/all");

		try {
			List<Pet> pets = petService.getAllPets();
			log.info("Successfully retrieved all pets, count: {}", pets.size());
			return ResponseEntity.ok(pets);
		} catch (Exception e) {
			log.error("Error occurred while retrieving all pets: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}