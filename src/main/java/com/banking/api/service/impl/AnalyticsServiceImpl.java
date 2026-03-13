package com.banking.api.service.impl;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.response.*;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.Transaction;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.TransactionRepository;
import com.banking.api.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public SpendingAnalyticsResponse getSpendingAnalytics(String accountId, String userId,
                                                          LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Validate ownership
        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view analytics for your own accounts");
        }

        // Default date range: last 6 months
        if (fromDate == null) {
            fromDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(LocalTime.MAX);

        // Fetch all transactions for this account in date range
        List<Transaction> transactions = transactionRepository.findAll(
                com.banking.api.repository.specification.TransactionSpecification.withFilters(
                        accountId, null, null, fromDate, toDate, null, null, null, null));

        log.info("Analytics for account {} — {} transactions from {} to {}", accountId, transactions.size(), fromDate, toDate);

        // Compute totals
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if (isIncome(tx, accountId)) {
                totalIncome = totalIncome.add(tx.getAmount());
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
            }
        }

        BigDecimal netChange = totalIncome.subtract(totalExpense);
        BigDecimal avgAmount = transactions.isEmpty() ? BigDecimal.ZERO :
                totalIncome.add(totalExpense).divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);

        // Monthly breakdown
        List<MonthlySummary> monthlySummaries = buildMonthlySummaries(transactions, accountId);

        // Category breakdown
        List<CategorySummary> categoryBreakdown = buildCategoryBreakdown(transactions, accountId);

        // Top beneficiaries (by outgoing transfer amount)
        List<TopBeneficiarySummary> topBeneficiaries = buildTopBeneficiaries(transactions, accountId);

        return SpendingAnalyticsResponse.builder()
                .accountId(accountId)
                .accountNumber(account.getAccountNumber())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netChange(netChange)
                .totalTransactions(transactions.size())
                .averageTransactionAmount(avgAmount)
                .monthlySummaries(monthlySummaries)
                .categoryBreakdown(categoryBreakdown)
                .topBeneficiaries(topBeneficiaries)
                .build();
    }

    // ==================== Private Helpers ====================

    private boolean isIncome(Transaction tx, String accountId) {
        // Income = deposit to this account, or incoming transfer
        if (tx.getType() == TransactionType.DEPOSIT) {
            return tx.getDestinationAccount() != null && tx.getDestinationAccount().getId().equals(accountId);
        }
        // For transfers, income is when this account is the destination
        return tx.getDestinationAccount() != null && tx.getDestinationAccount().getId().equals(accountId);
    }

    private List<MonthlySummary> buildMonthlySummaries(List<Transaction> transactions, String accountId) {
        // Group by year-month
        Map<String, List<Transaction>> byMonth = transactions.stream()
                .filter(tx -> tx.getCreatedAt() != null)
                .collect(Collectors.groupingBy(tx -> tx.getCreatedAt().getYear() + "-" + tx.getCreatedAt().getMonthValue(),
                        LinkedHashMap::new, Collectors.toList()));

        List<MonthlySummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byMonth.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (Transaction tx : entry.getValue()) {
                if (isIncome(tx, accountId)) {
                    income = income.add(tx.getAmount());
                } else {
                    expense = expense.add(tx.getAmount());
                }
            }

            summaries.add(MonthlySummary.builder()
                    .year(year)
                    .month(month)
                    .monthName(Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .totalIncome(income)
                    .totalExpense(expense)
                    .netChange(income.subtract(expense))
                    .transactionCount(entry.getValue().size())
                    .build());
        }

        // Sort by year-month
        summaries.sort(Comparator.comparingInt(MonthlySummary::getYear)
                .thenComparingInt(MonthlySummary::getMonth));
        return summaries;
    }

    private List<CategorySummary> buildCategoryBreakdown(List<Transaction> transactions, String accountId) {
        // Only count outgoing/expense transactions for category breakdown
        Map<String, List<Transaction>> byCategory = transactions.stream()
                .filter(tx -> !isIncome(tx, accountId))
                .collect(Collectors.groupingBy(tx -> tx.getCategory() != null ? tx.getCategory() : "OTHER"));

        BigDecimal totalExpense = byCategory.values().stream()
                .flatMap(List::stream)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategorySummary> categories = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byCategory.entrySet()) {
            BigDecimal categoryTotal = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? categoryTotal.multiply(BigDecimal.valueOf(100))
                        .divide(totalExpense, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            categories.add(CategorySummary.builder()
                    .category(entry.getKey())
                    .totalAmount(categoryTotal)
                    .transactionCount(entry.getValue().size())
                    .percentage(percentage)
                    .build());
        }

        // Sort by amount descending
        categories.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return categories;
    }

    private List<TopBeneficiarySummary> buildTopBeneficiaries(List<Transaction> transactions, String accountId) {
        // Only outgoing transfers
        Map<String, List<Transaction>> byDestination = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.TRANSFER)
                .filter(tx -> tx.getSourceAccount() != null && tx.getSourceAccount().getId().equals(accountId))
                .filter(tx -> tx.getDestinationAccount() != null)
                .collect(Collectors.groupingBy(tx -> tx.getDestinationAccount().getAccountNumber()));

        List<TopBeneficiarySummary> beneficiaries = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byDestination.entrySet()) {
            BigDecimal totalAmount = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            beneficiaries.add(TopBeneficiarySummary.builder()
                    .accountNumber(entry.getKey())
                    .totalAmount(totalAmount)
                    .transferCount(entry.getValue().size())
                    .build());
        }

        // Sort by total amount descending, limit top 5
        beneficiaries.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return beneficiaries.stream().limit(5).collect(Collectors.toList());
    }
}
