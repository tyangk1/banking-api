package com.banking.api.service;

import com.banking.api.model.dto.request.CreateRecurringTransferRequest;
import com.banking.api.model.dto.response.RecurringTransferResponse;

import java.util.List;

public interface RecurringTransferService {

    RecurringTransferResponse create(CreateRecurringTransferRequest request, String userId);

    List<RecurringTransferResponse> getByUser(String userId);

    RecurringTransferResponse getById(String id, String userId);

    RecurringTransferResponse pause(String id, String userId);

    RecurringTransferResponse resume(String id, String userId);

    void cancel(String id, String userId);

    /** Called by scheduler to execute all due recurring transfers. */
    int executeDueTransfers();
}
