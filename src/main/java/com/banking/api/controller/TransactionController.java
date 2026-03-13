package com.banking.api.controller;

import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @GetMapping("/search")
    @Operation(summary = "Search transactions",
               description = "Advanced search with multiple filters: type, status, date range, amount range, keyword, category")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> searchTransactions(
            @Parameter(description = "Account ID to filter by") @RequestParam(required = false) String accountId,
            @Parameter(description = "Transaction type: TRANSFER, DEPOSIT") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Transaction status: COMPLETED, PENDING, FAILED") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Search keyword (description/reference)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Transaction category: SALARY, RENT, SHOPPING, etc.") @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TransactionResponse> results = transactionService.searchTransactions(
                accountId, type, status, fromDate, toDate, minAmount, maxAmount, keyword, category, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
