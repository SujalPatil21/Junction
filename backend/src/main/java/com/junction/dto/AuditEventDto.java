package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventDto {
    private String id;
    private String actorId;
    private String actorRole;
    private String category;
    private String targetEntityType;
    private String targetEntityId;
    private String changeSummary;
    private String rationale;
    private String previousStateJson;
    private String newStateJson;
    private boolean isSimulatedScenario;
    private String timestamp;
}

