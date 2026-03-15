package com.banking.api.repository;

import com.banking.api.model.entity.Transaction;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Page<Transaction> findBySourceAccountId(String accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId)")
    Page<Transaction> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
            "AND t.type = :type")
    Page<Transaction> findByAccountIdAndType(@Param("accountId") String accountId,
                                              @Param("type") TransactionType type, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
            "AND t.createdAt BETWEEN :from AND :to")
    Page<Transaction> findByAccountIdAndDateRange(@Param("accountId") String accountId,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to,
                                                   Pageable pageable);

    long countBySourceAccountIdAndStatus(String accountId, TransactionStatus status);

    // ============ Analytics Queries ============

    /** Monthly summary: total amount grouped by type and month */
    @Query("SELECT FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM') as month, t.type, SUM(t.amount), COUNT(t) " +
           "FROM Transaction t WHERE (t.sourceAccount.id IN :accountIds OR t.destinationAccount.id IN :accountIds) " +
           "AND t.status = 'COMPLETED' AND t.createdAt >= :since " +
           "GROUP BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM'), t.type " +
           "ORDER BY month")
    java.util.List<Object[]> getMonthlySummary(@Param("accountIds") java.util.List<String> accountIds,
                                               @Param("since") LocalDateTime since);

    /** Category breakdown: total amount per category */
    @Query("SELECT t.category, SUM(t.amount), COUNT(t) " +
           "FROM Transaction t WHERE t.sourceAccount.id IN :accountIds " +
           "AND t.status = 'COMPLETED' AND t.createdAt >= :since " +
           "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    java.util.List<Object[]> getCategoryBreakdown(@Param("accountIds") java.util.List<String> accountIds,
                                                   @Param("since") LocalDateTime since);

    /** Daily volume for last N days */
    @Query("SELECT FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM-DD') as day, COUNT(t), SUM(t.amount) " +
           "FROM Transaction t WHERE (t.sourceAccount.id IN :accountIds OR t.destinationAccount.id IN :accountIds) " +
           "AND t.status = 'COMPLETED' AND t.createdAt >= :since " +
           "GROUP BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM-DD') ORDER BY day")
    java.util.List<Object[]> getDailyVolume(@Param("accountIds") java.util.List<String> accountIds,
                                             @Param("since") LocalDateTime since);

    /** Total income (deposit + received transfers) and expenses (sent transfers) */
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
           "WHERE (t.sourceAccount.id IN :accountIds OR t.destinationAccount.id IN :accountIds) " +
           "AND t.status = 'COMPLETED' AND t.createdAt >= :since " +
           "GROUP BY t.type")
    java.util.List<Object[]> getAmountByType(@Param("accountIds") java.util.List<String> accountIds,
                                              @Param("since") LocalDateTime since);
}
