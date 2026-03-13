package com.banking.api.controller;

import com.banking.api.model.dto.request.CreateBeneficiaryRequest;
import com.banking.api.model.dto.request.UpdateBeneficiaryRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.BeneficiaryResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.BeneficiaryService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiary Management", description = "APIs for managing saved transfer recipients")
@SecurityRequirement(name = "bearerAuth")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @Operation(summary = "Add a new beneficiary", description = "Save a new transfer recipient for quick access")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> createBeneficiary(
            @Valid @RequestBody CreateBeneficiaryRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        BeneficiaryResponse response = beneficiaryService.createBeneficiary(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary added successfully", response));
    }

    @GetMapping
    @Operation(summary = "List beneficiaries", description = "Get all beneficiaries with pagination")
    public ResponseEntity<ApiResponse<Page<BeneficiaryResponse>>> getBeneficiaries(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20, sort = "nickname", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<BeneficiaryResponse> page = beneficiaryService.getBeneficiaries(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get favorite beneficiaries", description = "Get all beneficiaries marked as favorite")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getFavorites(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<BeneficiaryResponse> favorites = beneficiaryService.getFavorites(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/search")
    @Operation(summary = "Search beneficiaries", description = "Search by nickname, account holder name, or account number")
    public ResponseEntity<ApiResponse<Page<BeneficiaryResponse>>> searchBeneficiaries(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Search keyword") @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BeneficiaryResponse> results = beneficiaryService.searchBeneficiaries(principal.getId(), q, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get beneficiary by ID")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getBeneficiary(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update beneficiary", description = "Update nickname or favorite status")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiary(
            @PathVariable String id,
            @Valid @RequestBody UpdateBeneficiaryRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete beneficiary")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        beneficiaryService.deleteBeneficiary(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully"));
    }
}
