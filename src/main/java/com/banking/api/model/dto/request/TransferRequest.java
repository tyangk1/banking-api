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
@Schema(description = "Request to transfer funds between accounts")
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    @Schema(description = "Source account number", example = "1000000001")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    @Schema(description = "Destination account number", example = "1000000002")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Transfer amount", example = "1000000")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Transfer description", example = "Monthly rent payment")
    private String description;
}
