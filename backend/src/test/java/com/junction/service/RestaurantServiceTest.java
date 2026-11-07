package com.junction.service;

import com.junction.common.exception.ResourceNotFoundException;
import com.junction.dto.InventoryLogRequestDto;
import com.junction.dto.RestaurantDto;
import com.junction.entity.ProfileEntity;
import com.junction.entity.RestaurantEntity;
import com.junction.entity.UserRole;
import com.junction.repository.ProfileRepository;
import com.junction.repository.RestaurantInventoryRepository;
import com.junction.repository.RestaurantMembershipRepository;
import com.junction.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantInventoryRepository inventoryRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RestaurantMembershipRepository membershipRepository;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(
            restaurantRepository,
            inventoryRepository,
            profileRepository,
            membershipRepository
        );
    }

    @Test
    @DisplayName("Organizer can record inventory on any restaurant without specific membership")
    void testOrganizerRecordsInventory() {
        String restaurantId = "R1";
        String clerkId = "clerk_organizer_001";
        RestaurantEntity restaurant = RestaurantEntity.builder().id("R1").name("Trishna").availableCovers(50).build();
        ProfileEntity organizer = ProfileEntity.builder().id(UUID.randomUUID()).role(UserRole.ORGANIZER).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(organizer));

        InventoryLogRequestDto dto = InventoryLogRequestDto.builder()
            .availableTables(5)
            .availableCovers(30)
            .expectedCovers(10)
            .build();

        RestaurantDto result = restaurantService.recordInventory(restaurantId, clerkId, dto);
        assertThat(result).isNotNull();
        assertThat(result.getAvailableCovers()).isEqualTo(30);
    }

    @Test
    @DisplayName("Restaurant partner with valid membership can update assigned restaurant")
    void testPartnerWithMembershipCanUpdate() {
        String restaurantId = "R1";
        String clerkId = "clerk_partner_001";
        UUID profileId = UUID.randomUUID();
        RestaurantEntity restaurant = RestaurantEntity.builder().id("R1").name("Trishna").availableCovers(50).build();
        ProfileEntity partner = ProfileEntity.builder().id(profileId).role(UserRole.RESTAURANT_PARTNER).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(partner));
        when(membershipRepository.existsByProfileIdAndRestaurantId(profileId, restaurantId)).thenReturn(true);

        InventoryLogRequestDto dto = InventoryLogRequestDto.builder()
            .availableTables(8)
            .availableCovers(25)
            .build();

        RestaurantDto result = restaurantService.recordInventory(restaurantId, clerkId, dto);
        assertThat(result).isNotNull();
        assertThat(result.getAvailableCovers()).isEqualTo(25);
    }

    @Test
    @DisplayName("Restaurant partner WITHOUT membership is denied access with AccessDeniedException")
    void testPartnerWithoutMembershipIsDenied() {
        String restaurantId = "R2";
        String clerkId = "clerk_partner_001";
        UUID profileId = UUID.randomUUID();
        RestaurantEntity restaurant = RestaurantEntity.builder().id("R2").name("Khyber").availableCovers(40).build();
        ProfileEntity partner = ProfileEntity.builder().id(profileId).role(UserRole.RESTAURANT_PARTNER).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(partner));
        when(membershipRepository.existsByProfileIdAndRestaurantId(profileId, restaurantId)).thenReturn(false);

        InventoryLogRequestDto dto = InventoryLogRequestDto.builder().availableCovers(20).build();

        assertThatThrownBy(() -> restaurantService.recordInventory(restaurantId, clerkId, dto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("You do not have permission to manage restaurant R2");
    }

    @Test
    @DisplayName("Non-existent restaurant throws ResourceNotFoundException")
    void testNonExistentRestaurantThrowsNotFound() {
        when(restaurantRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        InventoryLogRequestDto dto = InventoryLogRequestDto.builder().build();

        assertThatThrownBy(() -> restaurantService.recordInventory("UNKNOWN", "clerk_001", dto))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
