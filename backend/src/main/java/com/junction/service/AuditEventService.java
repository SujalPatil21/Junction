package com.junction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junction.dto.AuditEventDto;
import com.junction.entity.AuditEventEntity;
import com.junction.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditEventService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordEvent(
        String actorId,
        String actorRole,
        String category,
        String targetEntityType,
        String targetEntityId,
        String changeSummary,
        String rationale,
        Object previousState,
        Object newState
    ) {
        String prevJson = null;
        String newJson = null;
        try {
            if (previousState != null) prevJson = objectMapper.writeValueAsString(previousState);
            if (newState != null) newJson = objectMapper.writeValueAsString(newState);
        } catch (Exception ignored) {}

        AuditEventEntity entity = AuditEventEntity.builder()
            .id("AUDIT_" + UUID.randomUUID())
            .actorId(actorId != null ? actorId : "SYSTEM")
            .actorRole(actorRole != null ? actorRole : "ORGANIZER")
            .category(category)
            .targetEntityType(targetEntityType)
            .targetEntityId(targetEntityId)
            .changeSummary(changeSummary)
            .rationale(rationale)
            .previousStateJson(prevJson)
            .newStateJson(newJson)
            .isSimulatedScenario(false)
            .timestamp(Instant.now())
            .build();

        auditEventRepository.save(entity);
    }

    public List<AuditEventDto> getRecentEvents() {
        return auditEventRepository.findTop100ByOrderByTimestampDesc().stream()
            .map(e -> AuditEventDto.builder()
                .id(e.getId())
                .actorId(e.getActorId())
                .actorRole(e.getActorRole())
                .category(e.getCategory())
                .targetEntityType(e.getTargetEntityType())
                .targetEntityId(e.getTargetEntityId())
                .changeSummary(e.getChangeSummary())
                .rationale(e.getRationale())
                .previousStateJson(e.getPreviousStateJson())
                .newStateJson(e.getNewStateJson())
                .isSimulatedScenario(e.getIsSimulatedScenario())
                .timestamp(e.getTimestamp() != null ? e.getTimestamp().toString() : null)
                .build())
            .toList();
    }
}

