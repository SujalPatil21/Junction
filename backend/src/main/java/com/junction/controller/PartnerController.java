package com.junction.controller;

import com.junction.dto.InventoryLogRequestDto;
import com.junction.dto.PartnerInventoryRequestDto;
import com.junction.dto.RestaurantDto;
import com.junction.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partner")
@Tag(name = "Partner", description = "Hospitality Partner Scoped Operations")
public class PartnerController {

    private final RestaurantService restaurantService;

    public PartnerController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'RESTAURANT_PARTNER')")
    @Operation(summary = "Submit live restaurant inventory (scoped partner endpoint)")
    public ResponseEntity<Map<String, Object>> updateInventory(
        @Valid @RequestBody PartnerInventoryRequestDto dto,
        Authentication auth
    ) {
        String clerkUserId = auth != null ? auth.getName() : null;
        InventoryLogRequestDto logDto = InventoryLogRequestDto.builder()
            .availableTables(dto.getAvailableTables())
            .availableCovers(dto.getAvailableCovers())
            .expectedCovers(dto.getExpectedCovers())
            .outOfOrderTables(dto.getOutOfOrderTables())
            .notes(dto.getNotes())
            .build();

        RestaurantDto updated = restaurantService.recordInventory(dto.getRestaurantId(), clerkUserId, logDto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "restaurant", updated
        ));
    }
}
