package com.junction.controller;

import com.junction.dto.DeviceDefinitionDto;
import com.junction.service.DeviceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Hardware device registry and sensor telemetry")
public class DeviceController {

    private final DeviceRegistryService deviceRegistry;

    public DeviceController(DeviceRegistryService deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    @GetMapping
    @Operation(summary = "Get all registered devices")
    public ResponseEntity<List<DeviceDefinitionDto>> getAllDevices(
        @RequestParam(required = false) String zoneId,
        @RequestParam(required = false) String type
    ) {
        if (zoneId != null && !zoneId.isBlank()) {
            return ResponseEntity.ok(deviceRegistry.getByZone(zoneId));
        }
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(deviceRegistry.getByType(type));
        }
        return ResponseEntity.ok(deviceRegistry.getAll());
    }
}

