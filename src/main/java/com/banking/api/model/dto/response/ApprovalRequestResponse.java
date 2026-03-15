package com.banking.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Approval request response")
public class ApprovalRequestResponse {

    @Schema(description = "Request ID")
    private String id;

    @Schema(description = "Requester name")
    private String requesterName;

    @Schema(description = "Requester email")
    private String requesterEmail;

    @Schema(description = "Source account number")
    private String sourceAccountNumber;

    @Schema(description = "Destination account number")
    private String destinationAccountNumber;

    @Schema(description = "Transfer amount")
    private BigDecimal amount;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Status: PENDING, APPROVED, REJECTED")
    private String status;

    @Schema(description = "Rejection reason if rejected")
    private String rejectionReason;

    @Schema(description = "Which limit was exceeded")
    private String limitType;

    @Schema(description = "The limit value that was exceeded")
    private BigDecimal limitValue;

    @Schema(description = "Approver name (if approved/rejected)")
    private String approverName;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}
