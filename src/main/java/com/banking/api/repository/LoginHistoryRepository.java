package com.banking.api.repository;

import com.banking.api.model.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {

    Page<LoginHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
