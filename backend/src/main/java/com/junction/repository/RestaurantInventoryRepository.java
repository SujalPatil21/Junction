package com.junction.repository;

import com.junction.entity.RestaurantInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantInventoryRepository extends JpaRepository<RestaurantInventoryEntity, UUID> {
    List<RestaurantInventoryEntity> findByRestaurantIdOrderByRecordedAtDesc(String restaurantId);
    Optional<RestaurantInventoryEntity> findFirstByRestaurantIdOrderByRecordedAtDesc(String restaurantId);
}

