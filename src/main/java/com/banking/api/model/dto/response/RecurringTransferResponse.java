package com.banking.api.model.dto.response;

import com.banking.api.model.enums.RecurringFrequency;
import com.banking.api.model.enums.RecurringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransferResponse {

    private String id;
    private String sourceAccountId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;

    private RecurringFrequency frequency;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private LocalDate startDate;
    private LocalDate endDate;

    private RecurringStatus status;
    private LocalDateTime nextExecution;
    private LocalDateTime lastExecuted;
    private int executionCount;
    private Integer maxExecutions;
    private String lastError;

    private LocalDateTime createdAt;
}
