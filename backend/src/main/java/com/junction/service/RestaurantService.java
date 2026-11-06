package com.junction.service;

import com.junction.common.exception.ResourceNotFoundException;
import com.junction.dto.InventoryLogRequestDto;
import com.junction.dto.RestaurantCreateDto;
import com.junction.dto.RestaurantDto;
import com.junction.dto.RestaurantUpdateDto;
import com.junction.entity.ProfileEntity;
import com.junction.entity.RestaurantEntity;
import com.junction.entity.RestaurantInventoryEntity;
import com.junction.repository.ProfileRepository;
import com.junction.repository.RestaurantInventoryRepository;
import com.junction.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantInventoryRepository inventoryRepository;
    private final ProfileRepository profileRepository;

    public RestaurantService(
        RestaurantRepository restaurantRepository,
        RestaurantInventoryRepository inventoryRepository,
        ProfileRepository profileRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.inventoryRepository = inventoryRepository;
        this.profileRepository = profileRepository;
    }

    public List<RestaurantDto> getAllRestaurants(String zone) {
        List<RestaurantEntity> entities = (zone != null && !zone.isBlank())
            ? restaurantRepository.findByZoneAndIsActiveTrue(zone)
            : restaurantRepository.findByIsActiveTrue();

        return entities.stream().map(this::toDto).toList();
    }

    public RestaurantDto getRestaurantById(String id) {
        RestaurantEntity entity = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
        return toDto(entity);
    }

    @Transactional
    public RestaurantDto createRestaurant(RestaurantCreateDto dto) {
        RestaurantEntity entity = RestaurantEntity.builder()
            .id(dto.getId())
            .name(dto.getName())
            .zone(dto.getZone())
            .cuisine(dto.getCuisine())
            .address(dto.getAddress())
            .totalCapacity(dto.getTotalCapacity() != null ? dto.getTotalCapacity() : 100)
            .availableCovers(dto.getAvailableCovers() != null ? dto.getAvailableCovers() : 50)
            .expectedReservations(dto.getExpectedReservations() != null ? dto.getExpectedReservations() : 0)
            .priceRange(dto.getPriceRange() != null ? dto.getPriceRange() : "₹₹")
            .lat(dto.getLat())
            .lng(dto.getLng())
            .isActive(true)
            .build();

        restaurantRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public RestaurantDto updateRestaurant(String id, RestaurantUpdateDto dto) {
        RestaurantEntity entity = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getZone() != null) entity.setZone(dto.getZone());
        if (dto.getCuisine() != null) entity.setCuisine(dto.getCuisine());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getTotalCapacity() != null) entity.setTotalCapacity(dto.getTotalCapacity());
        if (dto.getAvailableCovers() != null) entity.setAvailableCovers(dto.getAvailableCovers());
        if (dto.getExpectedReservations() != null) entity.setExpectedReservations(dto.getExpectedReservations());
        if (dto.getPriceRange() != null) entity.setPriceRange(dto.getPriceRange());
        if (dto.getLat() != null) entity.setLat(dto.getLat());
        if (dto.getLng() != null) entity.setLng(dto.getLng());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());

        restaurantRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public RestaurantDto recordInventory(String restaurantId, String reporterClerkUserId, InventoryLogRequestDto dto) {
        RestaurantEntity restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));

        ProfileEntity reporter = null;
        if (reporterClerkUserId != null) {
            reporter = profileRepository.findByClerkUserId(reporterClerkUserId).orElse(null);
        }

        RestaurantInventoryEntity inv = RestaurantInventoryEntity.builder()
            .restaurant(restaurant)
            .reportedByProfile(reporter)
            .availableTables(dto.getAvailableTables())
            .availableCovers(dto.getAvailableCovers())
            .expectedCovers(dto.getExpectedCovers())
            .outOfOrderTables(dto.getOutOfOrderTables())
            .notes(dto.getNotes())
            .recordedAt(Instant.now())
            .build();

        inventoryRepository.save(inv);

        // Update live restaurant covers
        restaurant.setAvailableCovers(dto.getAvailableCovers());
        restaurantRepository.save(restaurant);

        return toDto(restaurant);
    }

    private RestaurantDto toDto(RestaurantEntity e) {
        return RestaurantDto.builder()
            .id(e.getId())
            .name(e.getName())
            .zone(e.getZone())
            .cuisine(e.getCuisine())
            .address(e.getAddress())
            .totalCapacity(e.getTotalCapacity())
            .availableCovers(e.getAvailableCovers())
            .expectedReservations(e.getExpectedReservations())
            .priceRange(e.getPriceRange())
            .lat(e.getLat())
            .lng(e.getLng())
            .isActive(e.getIsActive())
            .build();
    }
}

