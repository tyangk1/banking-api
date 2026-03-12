package com.banking.api.service;

import com.banking.api.model.dto.request.CreateAccountRequest;
import com.banking.api.model.dto.response.AccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request, String userId);

    AccountResponse getAccountById(String accountId);

    AccountResponse getAccountByNumber(String accountNumber);

    List<AccountResponse> getAccountsByUserId(String userId);

    Page<AccountResponse> getAccountsByUserId(String userId, Pageable pageable);

    AccountResponse closeAccount(String accountId, String userId);
}
