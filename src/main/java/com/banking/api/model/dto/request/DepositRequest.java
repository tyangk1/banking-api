package com.banking.api.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to deposit money into an account")
public class DepositRequest {

    @NotBlank(message = "Account number is required")
    @Schema(description = "Account number to deposit into", example = "1000000001")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Deposit amount", example = "5000000")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Deposit description", example = "Salary deposit")
    private String description;
}
