package com.banking.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly transaction summary")
public class MonthlySummary {

    @Schema(description = "Year", example = "2024")
    private int year;

    @Schema(description = "Month (1-12)", example = "6")
    private int month;

    @Schema(description = "Month name", example = "June")
    private String monthName;

    @Schema(description = "Total income (deposits + incoming transfers)", example = "55000000")
    private BigDecimal totalIncome;

    @Schema(description = "Total expense (outgoing transfers)", example = "35000000")
    private BigDecimal totalExpense;

    @Schema(description = "Net change (income - expense)", example = "20000000")
    private BigDecimal netChange;

    @Schema(description = "Total transaction count", example = "15")
    private long transactionCount;
}
