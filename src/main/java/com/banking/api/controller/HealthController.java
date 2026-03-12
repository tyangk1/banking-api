package com.banking.api.controller;

import com.banking.api.model.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/health")
@Tag(name = "Health Check", description = "API health check endpoint")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns API health status")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> health = Map.of(
                "status", "UP",
                "service", "Banking API",
                "version", "1.0.0"
        );
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", health));
    }
}
