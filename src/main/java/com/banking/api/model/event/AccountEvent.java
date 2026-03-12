package com.banking.api.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when an account is created or closed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEvent implements Serializable {

    public enum EventType { CREATED, CLOSED }

    private EventType eventType;
    private String accountId;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String ownerEmail;
    private String ownerName;
    private LocalDateTime timestamp;
}
