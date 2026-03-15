package com.banking.api.service;

import java.util.List;
import java.util.Map;

public interface AiChatService {

    /**
     * Send a chat message to the AI financial advisor.
     * Context is automatically built from user's transaction history and account data.
     *
     * @param userId the authenticated user's ID
     * @param message the user's question/message
     * @param conversationHistory previous messages for multi-turn conversation
     * @return the AI's response text
     */
    String chat(String userId, String message, List<Map<String, String>> conversationHistory);

    /**
     * Get a quick financial summary/insight for the user.
     */
    String getFinancialSummary(String userId);
}
