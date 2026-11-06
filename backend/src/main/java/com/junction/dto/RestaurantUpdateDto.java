package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantUpdateDto {
    private String name;
    private String zone;
    private String cuisine;
    private String address;
    private Integer totalCapacity;
    private Integer availableCovers;
    private Integer expectedReservations;
    private String priceRange;
    private Double lat;
    private Double lng;
    private Boolean isActive;
}

