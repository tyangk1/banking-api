package com.banking.api.model.entity;

import com.banking.api.model.enums.RecurringFrequency;
import com.banking.api.model.enums.RecurringStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @Column(name = "destination_account_number", nullable = false, length = 50)
    private String destinationAccountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(length = 500)
    private String description;

    // Schedule config
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurringFrequency frequency;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;  // 1-7 for WEEKLY

    @Column(name = "day_of_month")
    private Integer dayOfMonth; // 1-28 for MONTHLY

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Runtime state
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecurringStatus status = RecurringStatus.ACTIVE;

    @Column(name = "next_execution", nullable = false)
    private LocalDateTime nextExecution;

    @Column(name = "last_executed")
    private LocalDateTime lastExecuted;

    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private int executionCount = 0;

    @Column(name = "max_executions")
    private Integer maxExecutions;

    @Column(name = "last_error", length = 500)
    private String lastError;
}
