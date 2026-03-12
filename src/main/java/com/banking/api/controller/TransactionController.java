package com.banking.api.controller;

import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for fund transfers, deposits, and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfer money between two accounts")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        TransactionResponse response = transactionService.transfer(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Deposit funds", description = "Deposit money into an account (Manager/Admin only)")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit completed successfully", response));
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference number")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReference(
            @PathVariable String referenceNumber) {
        TransactionResponse response = transactionService.getByReferenceNumber(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transaction history", description = "Get paginated transaction history for an account")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable String accountId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
