package com.junction.service;

import com.junction.common.exception.InvalidStateTransitionException;
import com.junction.dto.OperationalInterventionDto;
import com.junction.entity.OperationalInterventionEntity;
import com.junction.repository.OperationalInterventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationLifecycleServiceTest {

    @Mock
    private OperationalInterventionRepository interventionRepository;

    @Mock
    private AuditEventService auditEventService;

    private RecommendationLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        lifecycleService = new RecommendationLifecycleService(interventionRepository, auditEventService, 300000L);
    }

    @Test
    @DisplayName("Should successfully approve a PROPOSED intervention")
    void testApproveProposedIntervention() {
        OperationalInterventionEntity entity = OperationalInterventionEntity.builder()
            .id("REC_001")
            .type("ROUTE_DIVERSION")
            .title("Divert to Churchgate")
            .description("Diversion test")
            .targetZoneId("ZONE_WANKHEDE")
            .status("PROPOSED")
            .rationale("High pressure")
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(interventionRepository.findById("REC_001")).thenReturn(Optional.of(entity));
        when(interventionRepository.save(any())).thenReturn(entity);

        OperationalInterventionDto approved = lifecycleService.approve("REC_001", "lead_organizer", "Critical crowd surge");

        assertEquals("APPROVED", approved.getStatus());
        assertEquals("lead_organizer", approved.getApprovedByUserId());
        verify(auditEventService, times(1)).recordEvent(
            eq("lead_organizer"), eq("ORGANIZER"), eq("RECOMMENDATION_APPROVAL"),
            eq("OPERATIONAL_INTERVENTION"), eq("REC_001"), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Should block executing an unapproved PROPOSED intervention")
    void testBlockExecutingUnapprovedIntervention() {
        OperationalInterventionEntity entity = OperationalInterventionEntity.builder()
            .id("REC_002")
            .status("PROPOSED")
            .title("Diversion")
            .description("Desc")
            .targetZoneId("ZONE_WANKHEDE")
            .rationale("Rationale")
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(interventionRepository.findById("REC_002")).thenReturn(Optional.of(entity));

        assertThrows(InvalidStateTransitionException.class, () -> {
            lifecycleService.execute("REC_002", "operator_01");
        });

        verify(interventionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should execute an APPROVED intervention")
    void testExecuteApprovedIntervention() {
        OperationalInterventionEntity entity = OperationalInterventionEntity.builder()
            .id("REC_003")
            .status("APPROVED")
            .title("Diversion")
            .description("Desc")
            .targetZoneId("ZONE_WANKHEDE")
            .rationale("Rationale")
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(interventionRepository.findById("REC_003")).thenReturn(Optional.of(entity));
        when(interventionRepository.save(any())).thenReturn(entity);

        OperationalInterventionDto executed = lifecycleService.execute("REC_003", "operator_01");

        assertEquals("EXECUTED", executed.getStatus());
        verify(auditEventService, times(1)).recordEvent(
            eq("operator_01"), eq("ORGANIZER"), eq("OPERATIONAL_OVERRIDE"),
            eq("OPERATIONAL_INTERVENTION"), eq("REC_003"), any(), any(), any(), any()
        );
    }
}

