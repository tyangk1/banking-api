package com.banking.api.service.impl;

import com.banking.api.config.RedisConfig;
import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.request.CreateAccountRequest;
import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.UserRepository;
import com.banking.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS_BY_USER, key = "#userId"),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS, allEntries = true)
    })
    public AccountResponse createAccount(CreateAccountRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created: {} for user: {} [cache evicted: accountsByUser:{}]",
                accountNumber, user.getEmail(), userId);

        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_ACCOUNTS, key = "#accountId")
    public AccountResponse getAccountById(String accountId) {
        log.debug("Cache MISS — loading account by id: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_ACCOUNT_BY_NUMBER, key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.debug("Cache MISS — loading account by number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_ACCOUNTS_BY_USER, key = "#userId")
    public List<AccountResponse> getAccountsByUserId(String userId) {
        log.debug("Cache MISS — loading accounts for user: {}", userId);
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByUserId(String userId, Pageable pageable) {
        // Pageable results are not cached (dynamic pagination)
        return accountRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS, key = "#accountId"),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNT_BY_NUMBER, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS_BY_USER, key = "#userId")
    })
    public AccountResponse closeAccount(String accountId, String userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only close your own accounts");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);
        Account closedAccount = accountRepository.save(account);
        log.info("Account closed: {} [cache evicted: accounts:{}, accountsByUser:{}]",
                account.getAccountNumber(), accountId, userId);

        return mapToResponse(closedAccount);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = 1_000_000_000L + ThreadLocalRandom.current().nextLong(9_000_000_000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .ownerName(account.getUser().getFullName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
