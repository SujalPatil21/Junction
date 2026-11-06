package com.junction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Junction Partner Restaurant Entity.
 * Table: restaurants
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id; // e.g., "R1", "R2"

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "zone", nullable = false)
    private String zone;

    @Column(name = "cuisine")
    private String cuisine;

    @Column(name = "address")
    private String address;

    @Builder.Default
    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity = 100;

    @Builder.Default
    @Column(name = "available_covers", nullable = false)
    private Integer availableCovers = 50;

    @Builder.Default
    @Column(name = "expected_reservations", nullable = false)
    private Integer expectedReservations = 0;

    @Builder.Default
    @Column(name = "price_range")
    private String priceRange = "₹₹";

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

