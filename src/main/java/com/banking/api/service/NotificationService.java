package com.banking.api.service;

import com.banking.api.model.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for pushing real-time notifications to WebSocket clients.
 * 
 * Destinations:
 *   /topic/notifications — Broadcast to all connected clients
 *   /queue/notifications — User-specific (via convertAndSendToUser)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific user via broadcast.
     * Frontend filters by userId to only show relevant notifications.
     * (convertAndSendToUser requires WebSocket authentication which we don't have)
     */
    public void sendToUser(String userId, NotificationEvent notification) {
        notification.setTimestamp(LocalDateTime.now());
        notification.setUserId(userId);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("📤 WS notification sent to user={}: type={}, title={}",
                userId, notification.getType(), notification.getTitle());
    }

    /**
     * Broadcast notification to all connected clients.
     */
    public void broadcast(NotificationEvent notification) {
        notification.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("📢 WS broadcast: type={}, title={}", notification.getType(), notification.getTitle());
    }

    // ============ Convenience methods ============

    public void notifyAccountCreated(String userId, String accountNumber, String accountName) {
        sendToUser(userId, NotificationEvent.builder()
                .type(NotificationEvent.NotificationType.ACCOUNT_CREATED)
                .title("Tài khoản đã được tạo")
                .message("Tài khoản " + accountName + " (" + accountNumber + ") đã được tạo thành công")
                .data(Map.of("accountNumber", accountNumber))
                .build());
    }

    public void notifyTransferSent(String userId, String refNumber, String amount, String destAccount) {
        sendToUser(userId, NotificationEvent.builder()
                .type(NotificationEvent.NotificationType.TRANSFER_SENT)
                .title("Chuyển tiền thành công")
                .message("Đã chuyển " + amount + " đến " + destAccount + " (Ref: " + refNumber + ")")
                .data(Map.of("referenceNumber", refNumber, "destinationAccount", destAccount))
                .build());
    }

    public void notifyTransferReceived(String userId, String refNumber, String amount, String fromAccount) {
        sendToUser(userId, NotificationEvent.builder()
                .type(NotificationEvent.NotificationType.TRANSFER_RECEIVED)
                .title("Nhận tiền")
                .message("Đã nhận " + amount + " từ " + fromAccount + " (Ref: " + refNumber + ")")
                .data(Map.of("referenceNumber", refNumber, "sourceAccount", fromAccount))
                .build());
    }

    public void notifyDepositReceived(String userId, String refNumber, String amount, String accountNumber) {
        sendToUser(userId, NotificationEvent.builder()
                .type(NotificationEvent.NotificationType.DEPOSIT_RECEIVED)
                .title("Nạp tiền thành công")
                .message("Đã nạp " + amount + " vào " + accountNumber + " (Ref: " + refNumber + ")")
                .data(Map.of("referenceNumber", refNumber, "accountNumber", accountNumber))
                .build());
    }
}
