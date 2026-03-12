package com.banking.api.service;

import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request, String userId);

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse getByReferenceNumber(String referenceNumber);

    Page<TransactionResponse> getTransactionsByAccountId(String accountId, Pageable pageable);
}
