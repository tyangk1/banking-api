package com.banking.api.service.impl;

import com.banking.api.config.RedisConfig;
import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.InsufficientBalanceException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.Transaction;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.TransactionRepository;
import com.banking.api.service.TransactionService;
import com.banking.api.messaging.EventPublisher;
import com.banking.api.model.event.TransactionEvent;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    @Caching(evict = {
            // Evict cached account data since balances changed
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNT_BY_NUMBER, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS_BY_USER, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_TRANSACTIONS, allEntries = true)
    })
    public TransactionResponse transfer(TransferRequest request, String userId) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new BadRequestException("Source and destination accounts cannot be the same");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getSourceAccountNumber()));

        Account destAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getDestinationAccountNumber()));

        // Validate ownership
        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only transfer from your own accounts");
        }

        // Validate active status
        validateAccountActive(sourceAccount);
        validateAccountActive(destAccount);

        // Validate balance
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    sourceAccount.getAccountNumber(), request.getAmount(), sourceAccount.getBalance());
        }

        // Execute transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destAccount.setBalance(destAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .currency(sourceAccount.getCurrency())
                .description(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .balanceAfterTransaction(sourceAccount.getBalance())
                .sourceAccount(sourceAccount)
                .destinationAccount(destAccount)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transfer completed: {} → {}, amount: {}, ref: {} [all caches evicted]",
                sourceAccount.getAccountNumber(), destAccount.getAccountNumber(),
                request.getAmount(), savedTransaction.getReferenceNumber());

        // Publish event to RabbitMQ
        eventPublisher.publishTransfer(TransactionEvent.builder()
                .eventType(TransactionEvent.EventType.TRANSFER)
                .transactionId(savedTransaction.getId())
                .referenceNumber(savedTransaction.getReferenceNumber())
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .currency(sourceAccount.getCurrency())
                .description(request.getDescription())
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destAccount.getAccountNumber())
                .initiatorEmail(sourceAccount.getUser().getEmail())
                .initiatorName(sourceAccount.getUser().getFullName())
                .timestamp(java.time.LocalDateTime.now())
                .build());

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNT_BY_NUMBER, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_ACCOUNTS_BY_USER, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_TRANSACTIONS, allEntries = true)
    })
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getAccountNumber()));

        validateAccountActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .currency(account.getCurrency())
                .description(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .balanceAfterTransaction(account.getBalance())
                .destinationAccount(account)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Deposit completed: account: {}, amount: {}, ref: {} [all caches evicted]",
                account.getAccountNumber(), request.getAmount(), savedTransaction.getReferenceNumber());

        // Publish event to RabbitMQ
        eventPublisher.publishDeposit(TransactionEvent.builder()
                .eventType(TransactionEvent.EventType.DEPOSIT)
                .transactionId(savedTransaction.getId())
                .referenceNumber(savedTransaction.getReferenceNumber())
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .currency(account.getCurrency())
                .description(request.getDescription())
                .destinationAccountNumber(account.getAccountNumber())
                .timestamp(java.time.LocalDateTime.now())
                .build());

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_TRANSACTIONS, key = "#referenceNumber")
    public TransactionResponse getByReferenceNumber(String referenceNumber) {
        log.debug("Cache MISS — loading transaction by ref: {}", referenceNumber);
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "referenceNumber", referenceNumber));
        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccountId(String accountId, Pageable pageable) {
        // Pageable results not cached (dynamic pagination)
        return transactionRepository.findByAccountId(accountId, pageable)
                .map(this::mapToResponse);
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account " + account.getAccountNumber() + " is not active");
        }
    }

    private String generateReferenceNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + date + "-" + uuid;
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .sourceAccountNumber(transaction.getSourceAccount() != null
                        ? transaction.getSourceAccount().getAccountNumber() : null)
                .destinationAccountNumber(transaction.getDestinationAccount() != null
                        ? transaction.getDestinationAccount().getAccountNumber() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
