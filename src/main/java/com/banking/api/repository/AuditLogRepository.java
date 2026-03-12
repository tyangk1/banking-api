package com.banking.api.repository;

import com.banking.api.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    List<AuditLog> findByUserIdAndTimestampBetween(
            String userId, LocalDateTime start, LocalDateTime end);

    long countByUserIdAndActionAndTimestampAfter(
            String userId, String action, LocalDateTime after);
}
