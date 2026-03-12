package com.banking.api.scheduler;

import com.banking.api.model.entity.Account;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.AccountType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for banking operations:
 * 
 *  1. Daily interest calculation for SAVINGS accounts (midnight)
 *  2. Daily transaction summary report (6 AM)
 *  3. Dormant account detection (weekly, Sunday midnight)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankingScheduler {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Annual interest rate for savings accounts (5.5%)
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.055");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    /**
     * Calculate daily interest for all ACTIVE SAVINGS accounts.
     * Runs every day at midnight (00:00).
     * Formula: dailyInterest = balance × (annualRate / 365)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void calculateDailyInterest() {
        log.info("🏦 [SCHEDULER] Starting daily interest calculation...");
        long startTime = System.currentTimeMillis();

        List<Account> savingsAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType() == AccountType.SAVINGS)
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .filter(a -> a.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        int processedCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Account account : savingsAccounts) {
            BigDecimal dailyInterest = account.getBalance()
                    .multiply(ANNUAL_INTEREST_RATE)
                    .divide(DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);

            account.setBalance(account.getBalance().add(dailyInterest));
            accountRepository.save(account);

            totalInterest = totalInterest.add(dailyInterest);
            processedCount++;

            log.debug("  Interest applied: account={}, balance={}, interest={}",
                    account.getAccountNumber(), account.getBalance(), dailyInterest);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("🏦 [SCHEDULER] Interest calculation complete: {} accounts processed, total interest={}, elapsed={}ms",
                processedCount, totalInterest, elapsed);
    }

    /**
     * Generate daily transaction summary report.
     * Runs every day at 6:00 AM.
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void generateDailyReport() {
        log.info("📊 [SCHEDULER] Generating daily transaction report...");

        LocalDateTime startOfDay = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atStartOfDay();

        long totalTransactions = transactionRepository.count();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .count();

        BigDecimal totalBalance = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("📊 [DAILY REPORT] date={}", LocalDate.now().minusDays(1));
        log.info("  Total accounts: {} (active: {})", totalAccounts, activeAccounts);
        log.info("  Total balance across all accounts: {}", totalBalance);
        log.info("  Total transactions in system: {}", totalTransactions);
        log.info("📊 [DAILY REPORT] Complete");
    }

    /**
     * Detect dormant accounts (no transactions in 90 days).
     * Runs every Sunday at midnight.
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void detectDormantAccounts() {
        log.info("😴 [SCHEDULER] Checking for dormant accounts...");

        LocalDateTime dormantThreshold = LocalDateTime.now().minusDays(90);
        int dormantCount = 0;

        List<Account> activeAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .toList();

        for (Account account : activeAccounts) {
            // Check if account has any recent transactions
            boolean hasRecentActivity = account.getUpdatedAt() != null
                    && account.getUpdatedAt().isAfter(dormantThreshold);

            if (!hasRecentActivity && account.getCreatedAt().isBefore(dormantThreshold)) {
                account.setStatus(AccountStatus.DORMANT);
                accountRepository.save(account);
                dormantCount++;
                log.warn("  Account {} marked as DORMANT (last activity before {})",
                        account.getAccountNumber(), dormantThreshold);
            }
        }

        log.info("😴 [SCHEDULER] Dormant check complete: {} accounts marked dormant", dormantCount);
    }
}
