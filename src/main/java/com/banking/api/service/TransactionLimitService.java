package com.banking.api.service;

import com.banking.api.model.dto.response.ApprovalRequestResponse;
import com.banking.api.model.dto.response.TransactionLimitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionLimitService {

    TransactionLimitResponse getLimits(String accountId, String userId);

    /** Check limits before transfer. Throws exception or creates approval request if exceeded. */
    void validateAndRecordUsage(String accountId, java.math.BigDecimal amount, String userId,
                                 String destinationAccountNumber, String currency, String description);

    // Approval workflow
    Page<ApprovalRequestResponse> getPendingApprovals(Pageable pageable);

    Page<ApprovalRequestResponse> getMyApprovalRequests(String userId, Pageable pageable);

    ApprovalRequestResponse approveRequest(String requestId, String approverId);

    ApprovalRequestResponse rejectRequest(String requestId, String approverId, String reason);
}
