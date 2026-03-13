package com.banking.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Spending analytics dashboard data")
public class SpendingAnalyticsResponse {

    @Schema(description = "Account ID")
    private String accountId;

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Total income over the period")
    private BigDecimal totalIncome;

    @Schema(description = "Total expense over the period")
    private BigDecimal totalExpense;

    @Schema(description = "Net change over the period")
    private BigDecimal netChange;

    @Schema(description = "Total number of transactions")
    private long totalTransactions;

    @Schema(description = "Average transaction amount")
    private BigDecimal averageTransactionAmount;

    @Schema(description = "Monthly breakdown")
    private List<MonthlySummary> monthlySummaries;

    @Schema(description = "Category breakdown")
    private List<CategorySummary> categoryBreakdown;

    @Schema(description = "Top beneficiaries by amount")
    private List<TopBeneficiarySummary> topBeneficiaries;
}
