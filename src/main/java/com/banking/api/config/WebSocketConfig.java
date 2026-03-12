package com.banking.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration using STOMP over SockJS.
 * 
 * Endpoints:
 *   /ws  — WebSocket connection endpoint (SockJS fallback)
 * 
 * Broker prefixes:
 *   /topic — Broadcast messages (e.g., /topic/notifications)
 *   /queue — User-specific messages (e.g., /queue/account-updates)
 * 
 * Application destination prefix:
 *   /app — Client-to-server messages (e.g., /app/send)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for /topic (broadcast) and /queue (user-specific)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations (convertAndSendToUser)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
