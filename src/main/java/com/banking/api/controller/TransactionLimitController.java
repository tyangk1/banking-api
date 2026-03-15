package com.banking.api.controller;

import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.ApprovalRequestResponse;
import com.banking.api.model.dto.response.TransactionLimitResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.TransactionLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/limits")
@RequiredArgsConstructor
@Tag(name = "Transaction Limits & Approvals", description = "APIs for managing transfer limits and approval workflow")
@SecurityRequirement(name = "bearerAuth")
public class TransactionLimitController {

    private final TransactionLimitService transactionLimitService;

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account limits", description = "View daily/monthly/single transfer limits and current usage")
    public ResponseEntity<ApiResponse<TransactionLimitResponse>> getAccountLimits(
            @PathVariable String accountId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        TransactionLimitResponse response = transactionLimitService.getLimits(accountId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/approvals/my")
    @Operation(summary = "My approval requests", description = "List my pending/approved/rejected transfer requests")
    public ResponseEntity<ApiResponse<Page<ApprovalRequestResponse>>> getMyApprovals(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApprovalRequestResponse> page = transactionLimitService.getMyApprovalRequests(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/approvals/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Pending approvals (Manager/Admin)", description = "List all pending transfer approval requests")
    public ResponseEntity<ApiResponse<Page<ApprovalRequestResponse>>> getPendingApprovals(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApprovalRequestResponse> page = transactionLimitService.getPendingApprovals(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping("/approvals/{requestId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Approve transfer request (Manager/Admin)")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> approveRequest(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ApprovalRequestResponse response = transactionLimitService.approveRequest(requestId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Transfer request approved", response));
    }

    @PostMapping("/approvals/{requestId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Reject transfer request (Manager/Admin)")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> rejectRequest(
            @PathVariable String requestId,
            @Parameter(description = "Reason for rejection") @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ApprovalRequestResponse response = transactionLimitService.rejectRequest(requestId, principal.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success("Transfer request rejected", response));
    }
}
