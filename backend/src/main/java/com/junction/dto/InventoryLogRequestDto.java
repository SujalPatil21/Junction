package com.junction.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogRequestDto {
    @Min(0)
    @Builder.Default
    private int availableTables = 0;

    @Min(0)
    @Builder.Default
    private int availableCovers = 0;

    @Min(0)
    @Builder.Default
    private int expectedCovers = 0;

    @Min(0)
    @Builder.Default
    private int outOfOrderTables = 0;

    private String notes;
}

