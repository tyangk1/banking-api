package com.banking.api.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a transaction (transfer/deposit) is completed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {

    public enum EventType { TRANSFER, DEPOSIT }

    private EventType eventType;
    private String transactionId;
    private String referenceNumber;
    private BigDecimal amount;
    private BigDecimal fee;
    private String currency;
    private String description;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String initiatorEmail;
    private String initiatorName;
    private String receiverEmail;
    private String receiverName;
    private LocalDateTime timestamp;
}
