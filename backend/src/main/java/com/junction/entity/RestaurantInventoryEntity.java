package com.junction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Restaurant Live Inventory / Telemetry Entity.
 * Table: restaurant_inventory
 */
@Entity
@Table(name = "restaurant_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_restaurant"))
    private RestaurantEntity restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_profile_id", foreignKey = @ForeignKey(name = "fk_inventory_profile"))
    private ProfileEntity reportedByProfile;

    @Builder.Default
    @Column(name = "available_tables", nullable = false)
    private Integer availableTables = 0;

    @Builder.Default
    @Column(name = "available_covers", nullable = false)
    private Integer availableCovers = 0;

    @Builder.Default
    @Column(name = "expected_covers", nullable = false)
    private Integer expectedCovers = 0;

    @Builder.Default
    @Column(name = "out_of_order_tables", nullable = false)
    private Integer outOfOrderTables = 0;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;
}

