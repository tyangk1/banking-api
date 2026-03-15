package com.banking.api.repository;

import com.banking.api.model.entity.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, String> {

    Optional<TransactionLimit> findByAccountId(String accountId);
}
