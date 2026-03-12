package com.banking.api.repository;

import com.banking.api.model.entity.Account;
import com.banking.api.model.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUserId(String userId);

    Page<Account> findByUserId(String userId, Pageable pageable);

    Page<Account> findByUserIdAndStatus(String userId, AccountStatus status, Pageable pageable);

    long countByUserId(String userId);
}
