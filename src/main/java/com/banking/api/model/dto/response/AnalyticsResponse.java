package com.banking.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Analytics dashboard response with aggregated transaction data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    /** Overall summary */
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalTransactions;

    /** Monthly income vs expenses (for line/bar chart) */
    private List<MonthlySummary> monthlySummary;

    /** Category breakdown (for pie/doughnut chart) */
    private List<CategoryBreakdown> categoryBreakdown;

    /** Daily transaction volume (for area chart) */
    private List<DailyVolume> dailyVolume;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummary {
        private String month;        // "2026-03"
        private BigDecimal income;
        private BigDecimal expenses;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyVolume {
        private String date;         // "2026-03-15"
        private long count;
        private BigDecimal amount;
    }
}
