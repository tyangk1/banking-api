package com.banking.api.messaging;

import com.banking.api.config.RabbitMQConfig;
import com.banking.api.model.event.AccountEvent;
import com.banking.api.model.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to RabbitMQ.
 * All publishing is asynchronous (@Async) so it doesn't block the main thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Async
    public void publishAccountCreated(AccountEvent event) {
        log.info("Publishing ACCOUNT_CREATED event: accountNumber={}", event.getAccountNumber());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_ACCOUNT_CREATED,
                event
        );
    }

    @Async
    public void publishAccountClosed(AccountEvent event) {
        log.info("Publishing ACCOUNT_CLOSED event: accountNumber={}", event.getAccountNumber());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_ACCOUNT_CLOSED,
                event
        );
    }

    @Async
    public void publishTransfer(TransactionEvent event) {
        log.info("Publishing TRANSFER event: ref={}, amount={}", event.getReferenceNumber(), event.getAmount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_TX_TRANSFER,
                event
        );
    }

    @Async
    public void publishDeposit(TransactionEvent event) {
        log.info("Publishing DEPOSIT event: ref={}, amount={}", event.getReferenceNumber(), event.getAmount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_TX_DEPOSIT,
                event
        );
    }
}
