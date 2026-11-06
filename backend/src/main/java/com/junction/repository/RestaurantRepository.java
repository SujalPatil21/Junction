package com.junction.repository;

import com.junction.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, String> {
    List<RestaurantEntity> findByZone(String zone);
    List<RestaurantEntity> findByIsActiveTrue();
    List<RestaurantEntity> findByZoneAndIsActiveTrue(String zone);
}

