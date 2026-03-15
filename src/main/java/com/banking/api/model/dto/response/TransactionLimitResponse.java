package com.banking.api.model.dto.response;

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
@Schema(description = "Transaction limit response")
public class TransactionLimitResponse {

    @Schema(description = "Limit ID")
    private String id;

    @Schema(description = "Account ID")
    private String accountId;

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Daily transfer limit")
    private BigDecimal dailyLimit;

    @Schema(description = "Monthly transfer limit")
    private BigDecimal monthlyLimit;

    @Schema(description = "Single transaction limit")
    private BigDecimal singleTransactionLimit;

    @Schema(description = "Amount used today")
    private BigDecimal currentDailyUsed;

    @Schema(description = "Amount used this month")
    private BigDecimal currentMonthlyUsed;

    @Schema(description = "Daily remaining")
    private BigDecimal dailyRemaining;

    @Schema(description = "Monthly remaining")
    private BigDecimal monthlyRemaining;
}
