package com.junction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateDto {
    @NotBlank(message = "ID is required (e.g. R4)")
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Zone is required (e.g. ZONE_A)")
    private String zone;

    private String cuisine;
    private String address;

    @Builder.Default
    private Integer totalCapacity = 100;

    @Builder.Default
    private Integer availableCovers = 50;

    @Builder.Default
    private Integer expectedReservations = 0;

    @Builder.Default
    private String priceRange = "₹₹";

    private Double lat;
    private Double lng;
}

