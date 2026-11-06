package com.junction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Operational Intervention / Recommendation Entity.
 * Table: operational_interventions
 */
@Entity
@Table(name = "operational_interventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalInterventionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "target_zone_id", nullable = false)
    private String targetZoneId;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "PROPOSED";

    @Builder.Default
    @Column(name = "urgency", nullable = false)
    private String urgency = "MEDIUM";

    @Builder.Default
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = true;

    @Column(name = "approval_role_required")
    private String approvalRoleRequired;

    @Column(name = "rationale", nullable = false)
    private String rationale;

    @Builder.Default
    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore = 0.8;

    @Builder.Default
    @Column(name = "expected_pressure_reduction_percent", nullable = false)
    private Integer expectedPressureReductionPercent = 0;

    @CreationTimestamp
    @Column(name = "proposed_at", nullable = false, updatable = false)
    private Instant proposedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by_user_id")
    private String approvedByUserId;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}

