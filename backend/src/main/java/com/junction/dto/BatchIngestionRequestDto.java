package com.junction.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchIngestionRequestDto {
    @NotEmpty(message = "Observations array must not be empty")
    private List<NormalizedObservationDto> observations;
}

