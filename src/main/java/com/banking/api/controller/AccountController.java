package com.banking.api.controller;

import com.banking.api.model.dto.request.CreateAccountRequest;
import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.AccountService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Create a new bank account for the authenticated user")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AccountResponse response = accountService.createAccount(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get my accounts", description = "Get all accounts for the authenticated user")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/paged")
    @Operation(summary = "Get my accounts (paginated)")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getMyAccountsPaged(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccountResponse> accounts = accountService.getAccountsByUserId(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable String id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close an account", description = "Close a bank account (balance must be zero)")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AccountResponse response = accountService.closeAccount(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", response));
    }
}
