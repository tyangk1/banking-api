package com.banking.api.controller;

import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.SpendingAnalyticsResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Spending Analytics", description = "APIs for transaction analytics and spending insights")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spending/{accountId}")
    @Operation(summary = "Get spending analytics",
               description = "Dashboard data: monthly summaries, category breakdown, top beneficiaries")
    public ResponseEntity<ApiResponse<SpendingAnalyticsResponse>> getSpendingAnalytics(
            @PathVariable String accountId,
            @Parameter(description = "Start date (yyyy-MM-dd), default: 6 months ago")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (yyyy-MM-dd), default: today")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        SpendingAnalyticsResponse analytics = analyticsService.getSpendingAnalytics(
                accountId, principal.getId(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
