package com.junction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerInventoryRequestDto {
    @NotBlank
    private String restaurantId;
    private int availableTables;
    private int availableCovers;
    private int expectedCovers;
    private int outOfOrderTables;
    private String notes;
}
