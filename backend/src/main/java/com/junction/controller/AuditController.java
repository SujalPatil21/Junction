package com.junction.controller;

import com.junction.dto.AuditEventDto;
import com.junction.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Immutable audit log of operational actions and interventions")
public class AuditController {

    private final AuditEventService auditEventService;

    public AuditController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get recent audit events (Organizer only)")
    public ResponseEntity<List<AuditEventDto>> getRecentAuditEvents() {
        return ResponseEntity.ok(auditEventService.getRecentEvents());
    }
}

