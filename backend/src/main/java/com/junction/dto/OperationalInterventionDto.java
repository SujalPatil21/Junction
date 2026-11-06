package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalInterventionDto {
    private String id;
    private String type;
    private String title;
    private String description;
    private String targetZoneId;
    private String status; // PROPOSED, APPROVED, REJECTED, EXECUTED
    private String urgency;
    private boolean requiresApproval;
    private String approvalRoleRequired;
    private String rationale;
    private double confidenceScore;
    private int expectedPressureReductionPercent;
    private String proposedAt;
    private String expiresAt;
    private String approvedAt;
    private String approvedByUserId;
    private String rejectionReason;
}

