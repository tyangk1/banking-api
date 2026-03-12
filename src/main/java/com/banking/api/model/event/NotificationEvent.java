package com.banking.api.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket notification payload sent to connected clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    public enum NotificationType {
        ACCOUNT_CREATED,
        ACCOUNT_CLOSED,
        TRANSFER_SENT,
        TRANSFER_RECEIVED,
        DEPOSIT_RECEIVED,
        BALANCE_UPDATED,
        SYSTEM_ALERT
    }

    private NotificationType type;
    private String title;
    private String message;
    private String userId;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
}
