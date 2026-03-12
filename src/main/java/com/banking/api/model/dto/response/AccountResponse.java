package com.banking.api.model.dto.response;

import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account response")
public class AccountResponse {

    @Schema(description = "Account ID")
    private String id;

    @Schema(description = "Account number", example = "1000000001")
    private String accountNumber;

    @Schema(description = "Account display name", example = "My Savings Account")
    private String accountName;

    @Schema(description = "Account type")
    private AccountType accountType;

    @Schema(description = "Current balance", example = "10000000.0000")
    private BigDecimal balance;

    @Schema(description = "Currency code", example = "VND")
    private String currency;

    @Schema(description = "Account status")
    private AccountStatus status;

    @Schema(description = "Account owner name")
    private String ownerName;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}
