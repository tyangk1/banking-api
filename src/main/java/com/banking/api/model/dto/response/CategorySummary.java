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
@Schema(description = "Category spending summary")
public class CategorySummary {

    @Schema(description = "Category name", example = "SALARY")
    private String category;

    @Schema(description = "Total amount in this category", example = "25000000")
    private BigDecimal totalAmount;

    @Schema(description = "Number of transactions", example = "6")
    private long transactionCount;

    @Schema(description = "Percentage of total spending", example = "35.5")
    private double percentage;
}
