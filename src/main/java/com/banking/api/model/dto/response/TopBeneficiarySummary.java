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
@Schema(description = "Top beneficiary summary")
public class TopBeneficiarySummary {

    @Schema(description = "Destination account number", example = "1000000010")
    private String accountNumber;

    @Schema(description = "Total amount transferred", example = "30000000")
    private BigDecimal totalAmount;

    @Schema(description = "Number of transfers", example = "6")
    private long transferCount;
}
