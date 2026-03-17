package com.banking.api.service.impl;

import com.banking.api.config.RedisConfig;
import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.request.CreateAccountRequest;
import com.banking.api.model.dto.response.AccountLookupResponse;
import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.UserRepository;
import com.banking.api.service.AccountService;
import com.banking.api.messaging.EventPublisher;
import com.banking.api.model.event.AccountEvent;
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
        private final EventPublisher eventPublisher;

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

                // Publish event to RabbitMQ
                eventPublisher.publishAccountCreated(AccountEvent.builder()
                                .eventType(AccountEvent.EventType.CREATED)
                                .accountId(savedAccount.getId())
                                .accountNumber(accountNumber)
                                .accountName(savedAccount.getAccountName())
                                .accountType(savedAccount.getAccountType().name())
                                .balance(savedAccount.getBalance())
                                .currency(savedAccount.getCurrency())
                                .ownerEmail(user.getEmail())
                                .ownerName(user.getFullName())
                                .timestamp(java.time.LocalDateTime.now())
                                .build());

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
                                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber",
                                                accountNumber));
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

                // Publish event to RabbitMQ
                eventPublisher.publishAccountClosed(AccountEvent.builder()
                                .eventType(AccountEvent.EventType.CLOSED)
                                .accountId(accountId)
                                .accountNumber(account.getAccountNumber())
                                .accountName(account.getAccountName())
                                .accountType(account.getAccountType().name())
                                .balance(account.getBalance())
                                .currency(account.getCurrency())
                                .ownerEmail(account.getUser().getEmail())
                                .ownerName(account.getUser().getFullName())
                                .timestamp(java.time.LocalDateTime.now())
                                .build());

                return mapToResponse(closedAccount);
        }

        @Override
        @Transactional(readOnly = true)
        public AccountLookupResponse lookupAccount(String accountNumber) {
                Account account = accountRepository.findByAccountNumber(accountNumber)
                                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber",
                                                accountNumber));

                User owner = account.getUser();
                String maskedName = maskName(owner.getFirstName(), owner.getLastName());

                return AccountLookupResponse.builder()
                                .accountNumber(account.getAccountNumber())
                                .ownerName(maskedName)
                                .accountType(account.getAccountType().name())
                                .active(account.getStatus() == AccountStatus.ACTIVE)
                                .build();
        }

        /**
         * Mask owner name for privacy: "NGUYEN VAN A" → "NGUYEN V*** A***"
         */
        private String maskName(String firstName, String lastName) {
                String maskedFirst = firstName.length() > 1
                                ? firstName.charAt(0) + "***"
                                : firstName;
                String maskedLast = lastName.length() > 1
                                ? lastName.charAt(0) + "***"
                                : lastName;
                return maskedFirst.toUpperCase() + " " + maskedLast.toUpperCase();
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
