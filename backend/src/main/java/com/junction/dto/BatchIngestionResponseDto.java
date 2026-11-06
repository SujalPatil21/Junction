package com.junction.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchIngestionResponseDto {
    private int acceptedCount;
    private int quarantinedCount;
    private int deduplicatedCount;
    private List<NormalizedObservationDto> accepted;
    private List<Map<String, Object>> quarantined;
}

