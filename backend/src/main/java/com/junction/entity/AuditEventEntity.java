package com.junction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Immutable Audit Event Log Entity.
 * Table: audit_events
 */
@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "target_entity_type", nullable = false)
    private String targetEntityType;

    @Column(name = "target_entity_id", nullable = false)
    private String targetEntityId;

    @Column(name = "change_summary", nullable = false)
    private String changeSummary;

    @Column(name = "rationale")
    private String rationale;

    @Column(name = "previous_state_json", columnDefinition = "TEXT")
    private String previousStateJson;

    @Column(name = "new_state_json", columnDefinition = "TEXT")
    private String newStateJson;

    @Builder.Default
    @Column(name = "is_simulated_scenario", nullable = false)
    private Boolean isSimulatedScenario = true;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}

