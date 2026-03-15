package com.banking.api.repository;

import com.banking.api.model.entity.ApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {

    Page<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId, Pageable pageable);

    Page<ApprovalRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
