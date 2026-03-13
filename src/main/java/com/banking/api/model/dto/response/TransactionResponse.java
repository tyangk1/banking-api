package com.banking.api.model.dto.response;

import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
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
@Schema(description = "Transaction response")
public class TransactionResponse {

    @Schema(description = "Transaction ID")
    private String id;

    @Schema(description = "Reference number", example = "TXN-20240101-ABC123")
    private String referenceNumber;

    @Schema(description = "Transaction type")
    private TransactionType type;

    @Schema(description = "Transaction amount", example = "1000000.0000")
    private BigDecimal amount;

    @Schema(description = "Transaction fee", example = "0.0000")
    private BigDecimal fee;

    @Schema(description = "Currency", example = "VND")
    private String currency;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Transaction status")
    private TransactionStatus status;

    @Schema(description = "Balance after transaction")
    private BigDecimal balanceAfterTransaction;

    @Schema(description = "Source account number")
    private String sourceAccountNumber;

    @Schema(description = "Destination account number")
    private String destinationAccountNumber;

    @Schema(description = "Transaction category", example = "SALARY")
    private String category;

    @Schema(description = "Transaction date")
    private LocalDateTime createdAt;
}
