package com.banking.api.model.dto.request;

import com.banking.api.model.enums.RecurringFrequency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringTransferRequest {

    @NotBlank(message = "Source account ID is required")
    private String sourceAccountId;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Frequency is required")
    private RecurringFrequency frequency;

    @Min(value = 1, message = "Day of week must be 1-7")
    @Max(value = 7, message = "Day of week must be 1-7")
    private Integer dayOfWeek;  // For WEEKLY

    @Min(value = 1, message = "Day of month must be 1-28")
    @Max(value = 28, message = "Day of month must be 1-28")
    private Integer dayOfMonth; // For MONTHLY

    private LocalDate startDate; // Default: today

    private LocalDate endDate;   // Optional

    private Integer maxExecutions; // Optional
}
