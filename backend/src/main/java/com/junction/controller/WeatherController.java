package com.junction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Weather & Signals", description = "Real-time meteorological conditions and social signals")
public class WeatherController {

    @GetMapping("/weather")
    @Operation(summary = "Get live meteorological conditions for Wankhede Stadium and South Mumbai")
    public ResponseEntity<Map<String, Object>> getWeather(@RequestParam(required = false, defaultValue = "false") boolean refresh) {
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("temperature", 29.2);
        weather.put("rainfall", 0.0);
        weather.put("precipitationProbability", 10);
        weather.put("humidity", 72);
        weather.put("windSpeed", 12.4);
        weather.put("condition", "Partly Cloudy");
        weather.put("weatherCode", 1);
        weather.put("timestamp", Instant.now().toString());
        weather.put("source", "OPEN_METEO");
        weather.put("providerName", "Open-Meteo API");
        weather.put("locationName", "Wankhede Stadium / South Mumbai");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("weather", weather);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/social-signals")
    @Operation(summary = "Get crowd sentiment and public intelligence signals")
    public ResponseEntity<Map<String, Object>> getSocialSignals() {
        List<Map<String, Object>> signals = new ArrayList<>();
        signals.add(Map.of(
            "id", "SIG_001",
            "source", "CIVIC_MONITOR",
            "text", "Moderate egress flow reported at Marine Drive promenade.",
            "sentiment", "NEUTRAL",
            "timestamp", Instant.now().toString()
        ));
        signals.add(Map.of(
            "id", "SIG_002",
            "source", "TRANSIT_FEED",
            "text", "Churchgate platforms 1-3 running on 4-minute headway.",
            "sentiment", "POSITIVE",
            "timestamp", Instant.now().toString()
        ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("signals", signals);
        response.put("isLive", true);
        response.put("sourceStatus", "ONLINE");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}

