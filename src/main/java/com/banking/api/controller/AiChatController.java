package com.banking.api.controller;

import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "AI Financial Advisor chatbot APIs")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "Send message to AI advisor",
               description = "Chat with the AI financial advisor. Includes conversation history for multi-turn.")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String message = (String) body.get("message");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");

        String response = aiChatService.chat(principal.getId(), message, history);

        return ResponseEntity.ok(ApiResponse.success(Map.of("response", response)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get AI financial summary",
               description = "Get a quick AI-powered financial summary and insights")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String summary = aiChatService.getFinancialSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("summary", summary)));
    }
}
