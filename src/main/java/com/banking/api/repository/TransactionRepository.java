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
}
