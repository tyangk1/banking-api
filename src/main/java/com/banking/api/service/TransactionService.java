package com.banking.api.service;

import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request, String userId);

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse getByReferenceNumber(String referenceNumber);

    Page<TransactionResponse> getTransactionsByAccountId(String accountId, Pageable pageable);

    Page<TransactionResponse> searchTransactions(
            String accountId,
            TransactionType type,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword,
            String category,
            Pageable pageable);
}
