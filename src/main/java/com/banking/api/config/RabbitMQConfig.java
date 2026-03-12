package com.banking.api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration with Topic Exchange pattern:
 *   banking.exchange (Topic Exchange)
 *     ├── banking.account.* → account-queue (created, closed)
 *     ├── banking.transaction.* → transaction-queue (transfer, deposit)
 *     └── banking.notification.* → notification-queue (alerts, reports)
 */
@Configuration
public class RabbitMQConfig {

    // ============ Exchange ============
    public static final String EXCHANGE_NAME = "banking.exchange";

    // ============ Queues ============
    public static final String ACCOUNT_QUEUE = "account-queue";
    public static final String TRANSACTION_QUEUE = "transaction-queue";
    public static final String NOTIFICATION_QUEUE = "notification-queue";

    // ============ Routing Keys ============
    public static final String ROUTING_ACCOUNT_CREATED = "banking.account.created";
    public static final String ROUTING_ACCOUNT_CLOSED = "banking.account.closed";
    public static final String ROUTING_TX_TRANSFER = "banking.transaction.transfer";
    public static final String ROUTING_TX_DEPOSIT = "banking.transaction.deposit";
    public static final String ROUTING_NOTIFICATION = "banking.notification.alert";

    // ============ Exchange Bean ============
    @Bean
    public TopicExchange bankingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // ============ Queue Beans ============
    @Bean
    public Queue accountQueue() {
        return QueueBuilder.durable(ACCOUNT_QUEUE).build();
    }

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(TRANSACTION_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    // ============ Bindings ============
    @Bean
    public Binding accountBinding(Queue accountQueue, TopicExchange bankingExchange) {
        return BindingBuilder.bind(accountQueue).to(bankingExchange).with("banking.account.*");
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue, TopicExchange bankingExchange) {
        return BindingBuilder.bind(transactionQueue).to(bankingExchange).with("banking.transaction.*");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange bankingExchange) {
        return BindingBuilder.bind(notificationQueue).to(bankingExchange).with("banking.notification.*");
    }

    // ============ Message Converter (JSON) ============
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
