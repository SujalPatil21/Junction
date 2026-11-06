package com.junction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequestDto {
    @NotNull(message = "Start location is required")
    private GeoLocationDto start;

    @NotNull(message = "End location is required")
    private GeoLocationDto end;
}

