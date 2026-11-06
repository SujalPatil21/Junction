package com.junction.controller;

import com.junction.dto.HospitalityDemandSignalDto;
import com.junction.service.HospitalityDemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospitality")
@Tag(name = "Hospitality", description = "Hotel & restaurant demand propagation")
public class HospitalityController {

    private final HospitalityDemandService hospitalityDemandService;

    public HospitalityController(HospitalityDemandService hospitalityDemandService) {
        this.hospitalityDemandService = hospitalityDemandService;
    }

    @GetMapping("/demand")
    @Operation(summary = "Calculate hospitality demand signals based on egress flow")
    public ResponseEntity<List<HospitalityDemandSignalDto>> getDemandSignals(
        @RequestParam(defaultValue = "120") int exitOutflowRate
    ) {
        return ResponseEntity.ok(hospitalityDemandService.calculateDemandSignals(exitOutflowRate));
    }
}

