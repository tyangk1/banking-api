package com.banking.api.repository;

import com.banking.api.model.entity.RecurringTransfer;
import com.banking.api.model.enums.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringTransferRepository extends JpaRepository<RecurringTransfer, String> {

    List<RecurringTransfer> findByUserIdOrderByCreatedAtDesc(String userId);

    List<RecurringTransfer> findByStatus(RecurringStatus status);

    /**
     * Find all active recurring transfers that are due for execution.
     */
    @Query("SELECT r FROM RecurringTransfer r WHERE r.status = 'ACTIVE' AND r.nextExecution <= :now")
    List<RecurringTransfer> findDueForExecution(@Param("now") LocalDateTime now);

    long countByUserIdAndStatus(String userId, RecurringStatus status);
}
