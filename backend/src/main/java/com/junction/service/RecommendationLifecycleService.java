package com.junction.service;

import com.junction.common.exception.InvalidStateTransitionException;
import com.junction.common.exception.ResourceNotFoundException;
import com.junction.dto.OperationalInterventionDto;
import com.junction.dto.ZoneStateDto;
import com.junction.entity.OperationalInterventionEntity;
import com.junction.repository.OperationalInterventionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecommendationLifecycleService {

    private final OperationalInterventionRepository interventionRepository;
    private final AuditEventService auditEventService;
    private final long cooldownMs;

    // In-memory cooldown tracking: targetZoneId -> lastGeneratedTimestamp
    private final Map<String, Long> lastGeneratedTimestamps = new ConcurrentHashMap<>();

    public RecommendationLifecycleService(
        OperationalInterventionRepository interventionRepository,
        AuditEventService auditEventService,
        @Value("${junction.recommendation.cooldown-ms:300000}") long cooldownMs
    ) {
        this.interventionRepository = interventionRepository;
        this.auditEventService = auditEventService;
        this.cooldownMs = cooldownMs;
    }

    @Transactional
    public List<OperationalInterventionDto> generateInterventions(List<ZoneStateDto> zones) {
        List<OperationalInterventionDto> generated = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (ZoneStateDto zone : zones) {
            if (zone.getPressure() >= 80) {
                Long lastTime = lastGeneratedTimestamps.get(zone.getId());
                if (lastTime != null && (now - lastTime) < cooldownMs) {
                    // Suppressed due to 5-minute cooldown
                    continue;
                }

                String id = "REC_" + zone.getId() + "_" + now;
                String title = "Divert Pedestrian Egress from " + zone.getName();
                String desc = "High pressure detected (" + zone.getPressure() + "%). Recommend activating alternate routing and transit gateholds.";

                OperationalInterventionEntity entity = OperationalInterventionEntity.builder()
                    .id(id)
                    .type("ROUTE_DIVERSION")
                    .title(title)
                    .description(desc)
                    .targetZoneId(zone.getId())
                    .status("PROPOSED")
                    .urgency(zone.getPressure() >= 90 ? "CRITICAL" : "HIGH")
                    .requiresApproval(true)
                    .approvalRoleRequired("ORGANIZER")
                    .rationale("Zone pressure reached " + zone.getPressure() + "%, exceeding critical threshold.")
                    .confidenceScore(zone.getConfidence())
                    .expectedPressureReductionPercent(18)
                    .proposedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

                interventionRepository.save(entity);
                lastGeneratedTimestamps.put(zone.getId(), now);
                generated.add(toDto(entity));
            }
        }

        return generated;
    }

    public List<OperationalInterventionDto> getAllInterventions() {
        return interventionRepository.findTop50ByOrderByProposedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    public Optional<OperationalInterventionDto> getInterventionById(String id) {
        return interventionRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public OperationalInterventionDto approve(String id, String userId, String rationale) {
        OperationalInterventionEntity entity = interventionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", id));

        if (!"PROPOSED".equals(entity.getStatus())) {
            throw new InvalidStateTransitionException("Intervention", entity.getStatus(), "APPROVED");
        }

        entity.setStatus("APPROVED");
        entity.setApprovedAt(Instant.now());
        entity.setApprovedByUserId(userId);
        interventionRepository.save(entity);

        auditEventService.recordEvent(
            userId,
            "ORGANIZER",
            "RECOMMENDATION_APPROVAL",
            "OPERATIONAL_INTERVENTION",
            id,
            "Approved intervention: " + entity.getTitle(),
            rationale,
            Map.of("status", "PROPOSED"),
            Map.of("status", "APPROVED", "approvedBy", userId)
        );

        return toDto(entity);
    }

    @Transactional
    public OperationalInterventionDto reject(String id, String userId, String reason) {
        OperationalInterventionEntity entity = interventionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", id));

        if (!"PROPOSED".equals(entity.getStatus())) {
            throw new InvalidStateTransitionException("Intervention", entity.getStatus(), "REJECTED");
        }

        entity.setStatus("REJECTED");
        entity.setRejectionReason(reason);
        interventionRepository.save(entity);

        auditEventService.recordEvent(
            userId,
            "ORGANIZER",
            "RECOMMENDATION_REJECTION",
            "OPERATIONAL_INTERVENTION",
            id,
            "Rejected intervention: " + entity.getTitle(),
            reason,
            Map.of("status", "PROPOSED"),
            Map.of("status", "REJECTED", "reason", reason)
        );

        return toDto(entity);
    }

    @Transactional
    public OperationalInterventionDto execute(String id, String userId) {
        OperationalInterventionEntity entity = interventionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", id));

        if ("PROPOSED".equals(entity.getStatus())) {
            throw new InvalidStateTransitionException("Cannot execute an unapproved PROPOSED intervention. Operator approval is strictly required.");
        }

        if (!"APPROVED".equals(entity.getStatus())) {
            throw new InvalidStateTransitionException("Intervention", entity.getStatus(), "EXECUTED");
        }

        entity.setStatus("EXECUTED");
        interventionRepository.save(entity);

        auditEventService.recordEvent(
            userId,
            "ORGANIZER",
            "OPERATIONAL_OVERRIDE",
            "OPERATIONAL_INTERVENTION",
            id,
            "Executed intervention: " + entity.getTitle(),
            "Dispatched operational commands to field systems.",
            Map.of("status", "APPROVED"),
            Map.of("status", "EXECUTED")
        );

        return toDto(entity);
    }

    private OperationalInterventionDto toDto(OperationalInterventionEntity e) {
        return OperationalInterventionDto.builder()
            .id(e.getId())
            .type(e.getType())
            .title(e.getTitle())
            .description(e.getDescription())
            .targetZoneId(e.getTargetZoneId())
            .status(e.getStatus())
            .urgency(e.getUrgency())
            .requiresApproval(e.getRequiresApproval())
            .approvalRoleRequired(e.getApprovalRoleRequired())
            .rationale(e.getRationale())
            .confidenceScore(e.getConfidenceScore())
            .expectedPressureReductionPercent(e.getExpectedPressureReductionPercent())
            .proposedAt(e.getProposedAt() != null ? e.getProposedAt().toString() : null)
            .expiresAt(e.getExpiresAt() != null ? e.getExpiresAt().toString() : null)
            .approvedAt(e.getApprovedAt() != null ? e.getApprovedAt().toString() : null)
            .approvedByUserId(e.getApprovedByUserId())
            .rejectionReason(e.getRejectionReason())
            .build();
    }
}

