package com.junction.repository;

import com.junction.entity.RestaurantMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantMembershipRepository extends JpaRepository<RestaurantMembershipEntity, UUID> {
    List<RestaurantMembershipEntity> findByProfileId(UUID profileId);
    List<RestaurantMembershipEntity> findByRestaurantId(String restaurantId);
    Optional<RestaurantMembershipEntity> findByProfileIdAndRestaurantId(UUID profileId, String restaurantId);
    boolean existsByProfileIdAndRestaurantId(UUID profileId, String restaurantId);
}

