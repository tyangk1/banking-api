package com.banking.api.controller;

import com.banking.api.model.dto.request.CreateRecurringTransferRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.RecurringTransferResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.RecurringTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/recurring-transfers")
@RequiredArgsConstructor
@Tag(name = "Recurring Transfers", description = "APIs for scheduled automatic transfers")
@SecurityRequirement(name = "bearerAuth")
public class RecurringTransferController {

    private final RecurringTransferService recurringService;

    @PostMapping
    @Operation(summary = "Create a recurring transfer schedule")
    public ResponseEntity<ApiResponse<RecurringTransferResponse>> create(
            @Valid @RequestBody CreateRecurringTransferRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        RecurringTransferResponse response = recurringService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List all recurring transfers for current user")
    public ResponseEntity<ApiResponse<List<RecurringTransferResponse>>> listMine(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<RecurringTransferResponse> list = recurringService.getByUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recurring transfer details")
    public ResponseEntity<ApiResponse<RecurringTransferResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        RecurringTransferResponse response = recurringService.getById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/pause")
    @Operation(summary = "Pause an active recurring transfer")
    public ResponseEntity<ApiResponse<RecurringTransferResponse>> pause(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        RecurringTransferResponse response = recurringService.pause(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/resume")
    @Operation(summary = "Resume a paused recurring transfer")
    public ResponseEntity<ApiResponse<RecurringTransferResponse>> resume(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        RecurringTransferResponse response = recurringService.resume(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a recurring transfer")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        recurringService.cancel(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
