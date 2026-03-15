package com.banking.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLimit extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyLimit = new BigDecimal("100000000.0000");

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal monthlyLimit = new BigDecimal("500000000.0000");

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal singleTransactionLimit = new BigDecimal("50000000.0000");

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentDailyUsed = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentMonthlyUsed = BigDecimal.ZERO;

    private LocalDateTime lastDailyReset;
    private LocalDateTime lastMonthlyReset;

    /**
     * Reset daily counters if the last reset was before today.
     */
    public void resetDailyIfNeeded() {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        if (lastDailyReset == null || lastDailyReset.isBefore(startOfToday)) {
            this.currentDailyUsed = BigDecimal.ZERO;
            this.lastDailyReset = LocalDateTime.now();
        }
    }

    /**
     * Reset monthly counters if the last reset was before the 1st of this month.
     */
    public void resetMonthlyIfNeeded() {
        LocalDateTime startOfMonth = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
        if (lastMonthlyReset == null || lastMonthlyReset.isBefore(startOfMonth)) {
            this.currentMonthlyUsed = BigDecimal.ZERO;
            this.lastMonthlyReset = LocalDateTime.now();
        }
    }

    public void recordUsage(BigDecimal amount) {
        resetDailyIfNeeded();
        resetMonthlyIfNeeded();
        this.currentDailyUsed = this.currentDailyUsed.add(amount);
        this.currentMonthlyUsed = this.currentMonthlyUsed.add(amount);
    }
}
