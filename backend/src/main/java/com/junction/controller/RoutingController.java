package com.junction.controller;

import com.junction.dto.GeoLocationDto;
import com.junction.dto.RouteRequestDto;
import com.junction.dto.RouteResultDto;
import com.junction.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routing")
@Tag(name = "Routing", description = "OSM vector road network routing and congestion calculations")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping
    @Operation(summary = "Calculate road network route between coordinates")
    public ResponseEntity<RouteResultDto> getRoute(
        @RequestParam double startLat,
        @RequestParam double startLng,
        @RequestParam double endLat,
        @RequestParam double endLng
    ) {
        return ResponseEntity.ok(routingService.calculateRoute(
            new GeoLocationDto(startLat, startLng),
            new GeoLocationDto(endLat, endLng)
        ));
    }

    @PostMapping
    @Operation(summary = "Calculate road network route between coordinates (body)")
    public ResponseEntity<RouteResultDto> calculateRoute(@Valid @RequestBody RouteRequestDto request) {
        return ResponseEntity.ok(routingService.calculateRoute(request.getStart(), request.getEnd()));
    }
}

