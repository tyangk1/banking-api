package com.banking.api.service.impl;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.response.ApprovalRequestResponse;
import com.banking.api.model.dto.response.TransactionLimitResponse;
import com.banking.api.model.entity.*;
import com.banking.api.repository.*;
import com.banking.api.service.TransactionLimitService;
import com.banking.api.service.TransactionService;
import com.banking.api.model.dto.request.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionLimitServiceImpl implements TransactionLimitService {

    private final TransactionLimitRepository limitRepository;
    private final ApprovalRequestRepository approvalRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public TransactionLimitResponse getLimits(String accountId, String userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view limits for your own accounts");
        }

        TransactionLimit limit = limitRepository.findByAccountId(accountId)
                .orElse(null);

        if (limit == null) {
            // Return default limits
            return TransactionLimitResponse.builder()
                    .accountId(accountId)
                    .accountNumber(account.getAccountNumber())
                    .dailyLimit(new BigDecimal("100000000"))
                    .monthlyLimit(new BigDecimal("500000000"))
                    .singleTransactionLimit(new BigDecimal("50000000"))
                    .currentDailyUsed(BigDecimal.ZERO)
                    .currentMonthlyUsed(BigDecimal.ZERO)
                    .dailyRemaining(new BigDecimal("100000000"))
                    .monthlyRemaining(new BigDecimal("500000000"))
                    .build();
        }

        limit.resetDailyIfNeeded();
        limit.resetMonthlyIfNeeded();

        return mapToLimitResponse(limit, account);
    }

    @Override
    public void validateAndRecordUsage(String accountId, BigDecimal amount, String userId,
                                        String destinationAccountNumber, String currency, String description) {
        TransactionLimit limit = limitRepository.findByAccountId(accountId).orElse(null);
        if (limit == null) {
            // No limits configured — allow
            return;
        }

        limit.resetDailyIfNeeded();
        limit.resetMonthlyIfNeeded();

        // Check single transaction limit
        if (amount.compareTo(limit.getSingleTransactionLimit()) > 0) {
            log.warn("Transfer exceeds single transaction limit: {} > {}", amount, limit.getSingleTransactionLimit());
            createApprovalRequest(userId, accountId, destinationAccountNumber, amount, currency, description,
                    "SINGLE_TRANSACTION", limit.getSingleTransactionLimit());
            throw new BadRequestException("Transfer amount " + amount + " exceeds single transaction limit of " +
                    limit.getSingleTransactionLimit() + ". An approval request has been created.");
        }

        // Check daily limit
        BigDecimal projectedDaily = limit.getCurrentDailyUsed().add(amount);
        if (projectedDaily.compareTo(limit.getDailyLimit()) > 0) {
            log.warn("Transfer exceeds daily limit: {} + {} > {}", limit.getCurrentDailyUsed(), amount, limit.getDailyLimit());
            createApprovalRequest(userId, accountId, destinationAccountNumber, amount, currency, description,
                    "DAILY", limit.getDailyLimit());
            throw new BadRequestException("Transfer would exceed daily limit of " + limit.getDailyLimit() +
                    " (used: " + limit.getCurrentDailyUsed() + "). An approval request has been created.");
        }

        // Check monthly limit
        BigDecimal projectedMonthly = limit.getCurrentMonthlyUsed().add(amount);
        if (projectedMonthly.compareTo(limit.getMonthlyLimit()) > 0) {
            log.warn("Transfer exceeds monthly limit: {} + {} > {}", limit.getCurrentMonthlyUsed(), amount, limit.getMonthlyLimit());
            createApprovalRequest(userId, accountId, destinationAccountNumber, amount, currency, description,
                    "MONTHLY", limit.getMonthlyLimit());
            throw new BadRequestException("Transfer would exceed monthly limit of " + limit.getMonthlyLimit() +
                    " (used: " + limit.getCurrentMonthlyUsed() + "). An approval request has been created.");
        }

        // Record usage
        limit.recordUsage(amount);
        limitRepository.save(limit);
        log.info("Recorded limit usage for account {}: daily {}/{}, monthly {}/{}",
                accountId, limit.getCurrentDailyUsed(), limit.getDailyLimit(),
                limit.getCurrentMonthlyUsed(), limit.getMonthlyLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestResponse> getPendingApprovals(Pageable pageable) {
        return approvalRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable)
                .map(this::mapToApprovalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestResponse> getMyApprovalRequests(String userId, Pageable pageable) {
        return approvalRepository.findByRequesterIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToApprovalResponse);
    }

    @Override
    public ApprovalRequestResponse approveRequest(String requestId, String approverId) {
        ApprovalRequest request = approvalRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", "id", requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("This request has already been " + request.getStatus().toLowerCase());
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));

        request.setStatus("APPROVED");
        request.setApprover(approver);
        ApprovalRequest saved = approvalRepository.save(request);
        log.info("Approval request {} approved by {}", requestId, approverId);

        return mapToApprovalResponse(saved);
    }

    @Override
    public ApprovalRequestResponse rejectRequest(String requestId, String approverId, String reason) {
        ApprovalRequest request = approvalRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", "id", requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("This request has already been " + request.getStatus().toLowerCase());
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));

        request.setStatus("REJECTED");
        request.setApprover(approver);
        request.setRejectionReason(reason != null ? reason : "No reason provided");
        ApprovalRequest saved = approvalRepository.save(request);
        log.info("Approval request {} rejected by {}: {}", requestId, approverId, reason);

        return mapToApprovalResponse(saved);
    }

    // ==================== Private Helpers ====================

    private void createApprovalRequest(String userId, String accountId, String destAccountNumber,
                                        BigDecimal amount, String currency, String description,
                                        String limitType, BigDecimal limitValue) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Account sourceAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .requester(requester)
                .sourceAccount(sourceAccount)
                .destinationAccountNumber(destAccountNumber)
                .amount(amount)
                .currency(currency)
                .description(description)
                .limitType(limitType)
                .limitValue(limitValue)
                .build();

        approvalRepository.save(approvalRequest);
        log.info("Created approval request — user: {}, amount: {}, limit: {} exceeded ({})",
                userId, amount, limitType, limitValue);
    }

    private TransactionLimitResponse mapToLimitResponse(TransactionLimit limit, Account account) {
        BigDecimal dailyRemaining = limit.getDailyLimit().subtract(limit.getCurrentDailyUsed()).max(BigDecimal.ZERO);
        BigDecimal monthlyRemaining = limit.getMonthlyLimit().subtract(limit.getCurrentMonthlyUsed()).max(BigDecimal.ZERO);

        return TransactionLimitResponse.builder()
                .id(limit.getId())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .singleTransactionLimit(limit.getSingleTransactionLimit())
                .currentDailyUsed(limit.getCurrentDailyUsed())
                .currentMonthlyUsed(limit.getCurrentMonthlyUsed())
                .dailyRemaining(dailyRemaining)
                .monthlyRemaining(monthlyRemaining)
                .build();
    }

    private ApprovalRequestResponse mapToApprovalResponse(ApprovalRequest request) {
        return ApprovalRequestResponse.builder()
                .id(request.getId())
                .requesterName(request.getRequester().getFullName())
                .requesterEmail(request.getRequester().getEmail())
                .sourceAccountNumber(request.getSourceAccount().getAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .status(request.getStatus())
                .rejectionReason(request.getRejectionReason())
                .limitType(request.getLimitType())
                .limitValue(request.getLimitValue())
                .approverName(request.getApprover() != null ? request.getApprover().getFullName() : null)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
