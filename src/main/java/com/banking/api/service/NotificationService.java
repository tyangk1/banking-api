package com.banking.api.service;

import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.response.NotificationResponse;
import com.banking.api.model.entity.Notification;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.NotificationType;
import com.banking.api.model.event.NotificationEvent;
import com.banking.api.repository.NotificationRepository;
import com.banking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for pushing real-time notifications to WebSocket clients
 * AND persisting them to the database for the Notification Center.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ============ WebSocket Push ============

    public void sendToUser(String userId, NotificationEvent notification) {
        notification.setTimestamp(LocalDateTime.now());
        notification.setUserId(userId);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("📤 WS notification sent to user={}: type={}, title={}",
                userId, notification.getType(), notification.getTitle());
    }

    public void broadcast(NotificationEvent notification) {
        notification.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("📢 WS broadcast: type={}, title={}", notification.getType(), notification.getTitle());
    }

    // ============ DB Persistence + WebSocket ============

    @Transactional
    public void persistAndSend(String userId, NotificationType type, String title, String message, String referenceId) {
        // 1. Persist to DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);

        // 2. Push via WebSocket
        sendToUser(userId, NotificationEvent.builder()
                .type(mapType(type))
                .title(title)
                .message(message)
                .data(referenceId != null ? Map.of("referenceId", referenceId) : Map.of())
                .build());
    }

    // ============ Notification Center CRUD ============

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void markAsRead(String userId, String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!n.getUser().getId().equals(userId)) {
            throw new com.banking.api.exception.BadRequestException("Notification does not belong to user");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    // ============ Convenience methods ============

    public void notifyAccountCreated(String userId, String accountNumber, String accountName) {
        persistAndSend(userId, NotificationType.SYSTEM,
                "Tài khoản đã được tạo",
                "Tài khoản " + accountName + " (" + accountNumber + ") đã được tạo thành công",
                accountNumber);
    }

    public void notifyTransferSent(String userId, String refNumber, String amount, String destAccount) {
        persistAndSend(userId, NotificationType.TRANSFER_SENT,
                "Chuyển tiền thành công",
                "Đã chuyển " + amount + " đến " + destAccount + " (Ref: " + refNumber + ")",
                refNumber);
    }

    public void notifyTransferReceived(String userId, String refNumber, String amount, String fromAccount) {
        persistAndSend(userId, NotificationType.TRANSFER_RECEIVED,
                "Nhận tiền",
                "Đã nhận " + amount + " từ " + fromAccount + " (Ref: " + refNumber + ")",
                refNumber);
    }

    public void notifyDepositReceived(String userId, String refNumber, String amount, String accountNumber) {
        persistAndSend(userId, NotificationType.DEPOSIT,
                "Nạp tiền thành công",
                "Đã nạp " + amount + " vào " + accountNumber + " (Ref: " + refNumber + ")",
                refNumber);
    }

    // ============ Mappers ============

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private NotificationEvent.NotificationType mapType(NotificationType type) {
        return switch (type) {
            case TRANSFER_SENT -> NotificationEvent.NotificationType.TRANSFER_SENT;
            case TRANSFER_RECEIVED -> NotificationEvent.NotificationType.TRANSFER_RECEIVED;
            case DEPOSIT -> NotificationEvent.NotificationType.DEPOSIT_RECEIVED;
            case LOGIN -> NotificationEvent.NotificationType.SYSTEM_ALERT;
            case SECURITY_ALERT -> NotificationEvent.NotificationType.SYSTEM_ALERT;
            case SYSTEM -> NotificationEvent.NotificationType.SYSTEM_ALERT;
        };
    }
}
