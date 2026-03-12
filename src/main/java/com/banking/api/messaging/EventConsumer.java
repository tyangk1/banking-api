package com.banking.api.messaging;

import com.banking.api.config.RabbitMQConfig;
import com.banking.api.model.event.AccountEvent;
import com.banking.api.model.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes domain events from RabbitMQ queues.
 * 
 * In a real-world scenario, these consumers would:
 * - Send email/SMS notifications
 * - Update analytics dashboards
 * - Trigger fraud detection workflows
 * - Generate audit logs
 * - Push WebSocket notifications to connected clients
 */
@Component
@Slf4j
public class EventConsumer {

    // ============ Account Events ============
    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE)
    public void handleAccountEvent(AccountEvent event) {
        log.info("📩 [ACCOUNT_EVENT] type={}, accountNumber={}, owner={}",
                event.getEventType(), event.getAccountNumber(), event.getOwnerEmail());

        switch (event.getEventType()) {
            case CREATED -> handleAccountCreated(event);
            case CLOSED -> handleAccountClosed(event);
        }
    }

    private void handleAccountCreated(AccountEvent event) {
        log.info("✅ Account created notification → Sending welcome email to {} for account {}",
                event.getOwnerEmail(), event.getAccountNumber());
        // TODO: Integrate with email service (SendGrid, SES, etc.)
        // emailService.sendWelcomeEmail(event.getOwnerEmail(), event.getAccountNumber());
    }

    private void handleAccountClosed(AccountEvent event) {
        log.info("🔒 Account closed notification → Sending confirmation to {} for account {}",
                event.getOwnerEmail(), event.getAccountNumber());
        // TODO: Send account closure confirmation email
    }

    // ============ Transaction Events ============
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("📩 [TRANSACTION_EVENT] type={}, ref={}, amount={} {}",
                event.getEventType(), event.getReferenceNumber(),
                event.getAmount(), event.getCurrency());

        switch (event.getEventType()) {
            case TRANSFER -> handleTransferCompleted(event);
            case DEPOSIT -> handleDepositCompleted(event);
        }
    }

    private void handleTransferCompleted(TransactionEvent event) {
        log.info("💸 Transfer notification → {} → {}, amount: {} {}, ref: {}",
                event.getSourceAccountNumber(), event.getDestinationAccountNumber(),
                event.getAmount(), event.getCurrency(), event.getReferenceNumber());
        // TODO: Send transfer confirmation to sender + receiver
        // TODO: Trigger fraud detection if amount > threshold
    }

    private void handleDepositCompleted(TransactionEvent event) {
        log.info("💰 Deposit notification → account: {}, amount: {} {}, ref: {}",
                event.getDestinationAccountNumber(),
                event.getAmount(), event.getCurrency(), event.getReferenceNumber());
        // TODO: Send deposit confirmation
    }

    // ============ Notification Events ============
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(String message) {
        log.info("🔔 [NOTIFICATION] {}", message);
        // TODO: Push to WebSocket, send push notification, etc.
    }
}
