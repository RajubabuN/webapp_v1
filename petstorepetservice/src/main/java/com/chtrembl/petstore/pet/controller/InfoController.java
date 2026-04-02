package com.chtrembl.petstore.pet.controller;

import com.chtrembl.petstore.pet.model.ContainerEnvironment;
import com.chtrembl.petstore.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/petstorepetservice/v2")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Info", description = "Pet Store Pet Service Information API")
public class InfoController {

    private final ContainerEnvironment containerEnvironment;
    private final PetService petService;

    @Operation(
            summary = "Health check",
            description = "Returns the health status of the service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(mediaType = "application/json"))
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
                "status", "UP",
                "service", "Pet Service",
                "version", containerEnvironment.getAppVersion(),
                "date", containerEnvironment.getAppDate(),
                "container", containerEnvironment.getContainerHostName(),
                "petsLoaded", String.valueOf(petService.getPetCount())
        );

        return ResponseEntity.ok(response);
    }
}