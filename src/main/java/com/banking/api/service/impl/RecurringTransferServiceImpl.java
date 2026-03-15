package com.banking.api.service.impl;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.request.CreateRecurringTransferRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.RecurringTransferResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.RecurringTransfer;
import com.banking.api.model.enums.RecurringFrequency;
import com.banking.api.model.enums.RecurringStatus;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.RecurringTransferRepository;
import com.banking.api.service.RecurringTransferService;
import com.banking.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringTransferServiceImpl implements RecurringTransferService {

    private final RecurringTransferRepository recurringRepo;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    @Override
    public RecurringTransferResponse create(CreateRecurringTransferRequest request, String userId) {
        // Validate source account belongs to user
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getSourceAccountId()));

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new BadRequestException("Source account does not belong to you");
        }

        // Validate frequency-specific fields
        if (request.getFrequency() == RecurringFrequency.WEEKLY && request.getDayOfWeek() == null) {
            throw new BadRequestException("dayOfWeek is required for WEEKLY frequency");
        }
        if (request.getFrequency() == RecurringFrequency.MONTHLY && request.getDayOfMonth() == null) {
            throw new BadRequestException("dayOfMonth is required for MONTHLY frequency");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        RecurringTransfer recurring = RecurringTransfer.builder()
                .user(sourceAccount.getUser())
                .sourceAccount(sourceAccount)
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .frequency(request.getFrequency())
                .dayOfWeek(request.getDayOfWeek())
                .dayOfMonth(request.getDayOfMonth())
                .startDate(startDate)
                .endDate(request.getEndDate())
                .maxExecutions(request.getMaxExecutions())
                .nextExecution(calculateNextExecution(request.getFrequency(), startDate,
                        request.getDayOfWeek(), request.getDayOfMonth()))
                .build();

        recurringRepo.save(recurring);
        log.info("Created recurring transfer: id={}, user={}, {} {} -> {}, frequency={}",
                recurring.getId(), userId, recurring.getAmount(), recurring.getCurrency(),
                recurring.getDestinationAccountNumber(), recurring.getFrequency());

        return toResponse(recurring);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransferResponse> getByUser(String userId) {
        return recurringRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransferResponse getById(String id, String userId) {
        RecurringTransfer rt = findAndAuthorize(id, userId);
        return toResponse(rt);
    }

    @Override
    public RecurringTransferResponse pause(String id, String userId) {
        RecurringTransfer rt = findAndAuthorize(id, userId);
        if (rt.getStatus() != RecurringStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE recurring transfers can be paused");
        }
        rt.setStatus(RecurringStatus.PAUSED);
        recurringRepo.save(rt);
        log.info("Paused recurring transfer: id={}", id);
        return toResponse(rt);
    }

    @Override
    public RecurringTransferResponse resume(String id, String userId) {
        RecurringTransfer rt = findAndAuthorize(id, userId);
        if (rt.getStatus() != RecurringStatus.PAUSED) {
            throw new BadRequestException("Only PAUSED recurring transfers can be resumed");
        }
        rt.setStatus(RecurringStatus.ACTIVE);
        // Recalculate next execution from now
        rt.setNextExecution(calculateNextExecution(rt.getFrequency(), LocalDate.now(),
                rt.getDayOfWeek(), rt.getDayOfMonth()));
        recurringRepo.save(rt);
        log.info("Resumed recurring transfer: id={}", id);
        return toResponse(rt);
    }

    @Override
    public void cancel(String id, String userId) {
        RecurringTransfer rt = findAndAuthorize(id, userId);
        if (rt.getStatus() == RecurringStatus.CANCELLED) {
            throw new BadRequestException("Recurring transfer is already cancelled");
        }
        rt.setStatus(RecurringStatus.CANCELLED);
        recurringRepo.save(rt);
        log.info("Cancelled recurring transfer: id={}", id);
    }

    @Override
    public int executeDueTransfers() {
        List<RecurringTransfer> dueTransfers = recurringRepo.findDueForExecution(LocalDateTime.now());
        int successCount = 0;

        for (RecurringTransfer rt : dueTransfers) {
            try {
                // Check end date
                if (rt.getEndDate() != null && LocalDate.now().isAfter(rt.getEndDate())) {
                    rt.setStatus(RecurringStatus.COMPLETED);
                    recurringRepo.save(rt);
                    log.info("Recurring transfer {} completed (end date reached)", rt.getId());
                    continue;
                }

                // Check max executions
                if (rt.getMaxExecutions() != null && rt.getExecutionCount() >= rt.getMaxExecutions()) {
                    rt.setStatus(RecurringStatus.COMPLETED);
                    recurringRepo.save(rt);
                    log.info("Recurring transfer {} completed (max executions reached)", rt.getId());
                    continue;
                }

                // Execute the transfer
                TransferRequest transferRequest = TransferRequest.builder()
                        .sourceAccountNumber(rt.getSourceAccount().getAccountNumber())
                        .destinationAccountNumber(rt.getDestinationAccountNumber())
                        .amount(rt.getAmount())
                        .description("[Auto] " + (rt.getDescription() != null ? rt.getDescription() : "Scheduled transfer"))
                        .build();

                transactionService.transfer(transferRequest, rt.getUser().getId());

                // Update state
                rt.setLastExecuted(LocalDateTime.now());
                rt.setExecutionCount(rt.getExecutionCount() + 1);
                rt.setLastError(null);
                rt.setNextExecution(calculateNextExecution(rt.getFrequency(), LocalDate.now().plusDays(1),
                        rt.getDayOfWeek(), rt.getDayOfMonth()));
                recurringRepo.save(rt);

                successCount++;
                log.info("✅ Executed recurring transfer: id={}, count={}, next={}",
                        rt.getId(), rt.getExecutionCount(), rt.getNextExecution());

            } catch (Exception e) {
                rt.setLastError(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "Unknown error");
                rt.setNextExecution(calculateNextExecution(rt.getFrequency(), LocalDate.now().plusDays(1),
                        rt.getDayOfWeek(), rt.getDayOfMonth()));
                recurringRepo.save(rt);
                log.error("❌ Failed recurring transfer: id={}, error={}", rt.getId(), e.getMessage());
            }
        }

        return successCount;
    }

    // ==================== Helpers ====================

    private RecurringTransfer findAndAuthorize(String id, String userId) {
        RecurringTransfer rt = recurringRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransfer", "id", id));
        if (!rt.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have access to this recurring transfer");
        }
        return rt;
    }

    private LocalDateTime calculateNextExecution(RecurringFrequency frequency, LocalDate fromDate,
                                                  Integer dayOfWeek, Integer dayOfMonth) {
        LocalDate nextDate = switch (frequency) {
            case DAILY -> fromDate;
            case WEEKLY -> {
                LocalDate d = fromDate;
                while (d.getDayOfWeek().getValue() != dayOfWeek) {
                    d = d.plusDays(1);
                }
                yield d;
            }
            case MONTHLY -> {
                int dom = Math.min(dayOfMonth, fromDate.lengthOfMonth());
                LocalDate d = fromDate.withDayOfMonth(dom);
                if (d.isBefore(fromDate)) d = d.plusMonths(1).withDayOfMonth(Math.min(dayOfMonth, d.plusMonths(1).lengthOfMonth()));
                yield d;
            }
        };
        return nextDate.atTime(LocalTime.of(8, 0)); // Execute at 8:00 AM
    }

    private RecurringTransferResponse toResponse(RecurringTransfer rt) {
        return RecurringTransferResponse.builder()
                .id(rt.getId())
                .sourceAccountId(rt.getSourceAccount().getId())
                .sourceAccountNumber(rt.getSourceAccount().getAccountNumber())
                .destinationAccountNumber(rt.getDestinationAccountNumber())
                .amount(rt.getAmount())
                .currency(rt.getCurrency())
                .description(rt.getDescription())
                .frequency(rt.getFrequency())
                .dayOfWeek(rt.getDayOfWeek())
                .dayOfMonth(rt.getDayOfMonth())
                .startDate(rt.getStartDate())
                .endDate(rt.getEndDate())
                .status(rt.getStatus())
                .nextExecution(rt.getNextExecution())
                .lastExecuted(rt.getLastExecuted())
                .executionCount(rt.getExecutionCount())
                .maxExecutions(rt.getMaxExecutions())
                .lastError(rt.getLastError())
                .createdAt(rt.getCreatedAt())
                .build();
    }
}
