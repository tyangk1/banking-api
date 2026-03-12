package com.banking.api.model.dto.request;

import com.banking.api.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new bank account")
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    @Schema(description = "Account display name", example = "My Savings Account")
    private String accountName;

    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account", example = "SAVINGS")
    private AccountType accountType;

    @Schema(description = "Currency code (default: VND)", example = "VND")
    @Builder.Default
    private String currency = "VND";
}
