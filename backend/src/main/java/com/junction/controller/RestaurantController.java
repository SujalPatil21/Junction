package com.junction.controller;

import com.junction.dto.InventoryLogRequestDto;
import com.junction.dto.RestaurantCreateDto;
import com.junction.dto.RestaurantDto;
import com.junction.dto.RestaurantUpdateDto;
import com.junction.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Partner restaurant directory & live capacity telemetry")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    @Operation(summary = "Get all active restaurants (optionally filtered by zone)")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(
        @RequestParam(required = false) String zone
    ) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(zone));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable String id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Register new restaurant partner (Organizer only)")
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody RestaurantCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Update restaurant details (Organizer only)")
    public ResponseEntity<RestaurantDto> updateRestaurant(
        @PathVariable String id,
        @RequestBody RestaurantUpdateDto dto
    ) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, dto));
    }

    @PostMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'RESTAURANT_PARTNER')")
    @Operation(summary = "Record live inventory/table availability (Organizer or Restaurant Partner)")
    public ResponseEntity<RestaurantDto> recordInventory(
        @PathVariable String id,
        @Valid @RequestBody InventoryLogRequestDto dto,
        Authentication auth
    ) {
        String clerkUserId = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(restaurantService.recordInventory(id, clerkUserId, dto));
    }
}

