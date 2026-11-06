package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CascadeAnalysisDto {
    private String rootIncidentId;
    private String rootResourceId;
    private String timestamp;
    private List<List<CascadeNodeProjectionDto>> affectedPathways;
    private String summary;
    private double confidence;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CascadeNodeProjectionDto {
        private String nodeId;
        private String resourceId;
        private String label;
        private int currentPressure;
        private int projectedPressure;
        private String status;
        private int leadTimeMinutes;
        private double influenceWeight;
        private boolean mitigationAvailable;
    }
}

