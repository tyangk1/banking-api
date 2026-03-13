package com.banking.api.repository.specification;

import com.banking.api.model.entity.Transaction;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic Transaction queries.
 * Supports combining multiple filters: type, status, date range,
 * amount range, account, keyword, and category.
 */
public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> withFilters(
            String accountId,
            TransactionType type,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword,
            String category) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by account (source OR destination)
            if (accountId != null && !accountId.isBlank()) {
                predicates.add(cb.or(
                        cb.equal(root.get("sourceAccount").get("id"), accountId),
                        cb.equal(root.get("destinationAccount").get("id"), accountId)
                ));
            }

            // Filter by type
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by date range
            if (fromDate != null) {
                LocalDateTime fromDateTime = fromDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }
            if (toDate != null) {
                LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            // Filter by amount range
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            // Filter by keyword (description search)
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("referenceNumber")), pattern)
                ));
            }

            // Filter by category
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category.toUpperCase()));
            }

            // Order by createdAt desc by default
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
