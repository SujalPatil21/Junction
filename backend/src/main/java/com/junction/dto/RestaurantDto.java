package com.junction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDto {
    private String id;
    private String name;
    private String zone;
    private String cuisine;
    private String address;
    private int totalCapacity;
    private int availableCovers;
    private int expectedReservations;
    private String priceRange;
    private Double lat;
    private Double lng;
    private boolean isActive;
}

