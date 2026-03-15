package com.banking.api.service.impl;

import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.service.AccountService;
import com.banking.api.service.AiChatService;
import com.banking.api.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final WebClient webClient;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-2.0-flash}")
    private String geminiModel;

    public AiChatServiceImpl(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    private static final String SYSTEM_PROMPT = """
            Bạn là "AI Financial Advisor" — trợ lý tài chính thông minh của Premium Banking.
            
            NHIỆM VỤ của bạn:
            - Phân tích chi tiêu, thu nhập dựa trên lịch sử giao dịch của khách hàng
            - Đưa ra lời khuyên tài chính thực tế, cụ thể
            - Phát hiện xu hướng chi tiêu bất thường
            - Gợi ý cách tiết kiệm và quản lý tài chính cá nhân
            - Trả lời câu hỏi về tài chính, đầu tư cơ bản
            
            QUY TẮC:
            - Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp
            - Sử dụng emoji phù hợp để dễ đọc
            - Trả lời ngắn gọn, súc tích (tối đa 300 từ)
            - Đưa ra con số cụ thể khi phân tích
            - Nếu không có dữ liệu, hãy nói rõ và đưa lời khuyên chung
            - KHÔNG BAO GIỜ tiết lộ thông tin nhạy cảm hay số tài khoản đầy đủ
            """;

    @Override
    public String chat(String userId, String message, List<Map<String, String>> conversationHistory) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return fallbackResponse(message);
        }

        try {
            // Build financial context from user data
            String financialContext = buildFinancialContext(userId);

            // Build Gemini API request
            List<Map<String, Object>> contents = new ArrayList<>();

            // System instruction as first user message
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", SYSTEM_PROMPT + "\n\nDỮ LIỆU TÀI CHÍNH CỦA KHÁCH HÀNG:\n" + financialContext))
            ));
            contents.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text", "Tôi đã nhận được dữ liệu tài chính của bạn. Tôi sẵn sàng phân tích và tư vấn. Bạn cần hỗ trợ gì?"))
            ));

            // Add conversation history
            if (conversationHistory != null) {
                for (Map<String, String> msg : conversationHistory) {
                    contents.add(Map.of(
                            "role", msg.get("role").equals("user") ? "user" : "model",
                            "parts", List.of(Map.of("text", msg.get("content")))
                    ));
                }
            }

            // Add current message
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", message))
            ));

            Map<String, Object> requestBody = Map.of(
                    "contents", contents,
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 1024,
                            "topP", 0.9
                    )
            );

            // Call Gemini API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}", geminiModel, geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractResponseText(response);

        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return fallbackResponse(message);
        }
    }

    @Override
    public String getFinancialSummary(String userId) {
        return chat(userId, "Hãy cho tôi bản tóm tắt tình hình tài chính tổng quan: tổng số dư, chi tiêu gần đây, và một lời khuyên ngắn.", null);
    }

    private String buildFinancialContext(String userId) {
        StringBuilder ctx = new StringBuilder();

        try {
            // 1. Account summary
            List<AccountResponse> accs = accountService.getAccountsByUserId(userId);
            BigDecimal totalBalance = BigDecimal.ZERO;
            ctx.append("📊 TÀI KHOẢN:\n");
            for (AccountResponse acc : accs) {
                ctx.append(String.format("  - %s (%s): %s %s\n",
                        acc.getAccountName() != null ? acc.getAccountName() : acc.getAccountType(),
                        acc.getAccountType(),
                        formatCurrency(acc.getBalance()),
                        acc.getCurrency()));
                totalBalance = totalBalance.add(acc.getBalance());
            }
            ctx.append(String.format("  → Tổng số dư: %s VND\n\n", formatCurrency(totalBalance)));

            // 2. Recent transactions (last 30 days, up to 50)
            if (!accs.isEmpty()) {
                ctx.append("📋 GIAO DỊCH 30 NGÀY GẦN NHẤT:\n");
                int txCount = 0;
                BigDecimal totalIncome = BigDecimal.ZERO;
                BigDecimal totalExpense = BigDecimal.ZERO;
                Map<String, BigDecimal> categorySpending = new HashMap<>();

                for (AccountResponse acc : accs) {
                    Page<TransactionResponse> txPage = transactionService.searchTransactions(
                            acc.getId(), null, null,
                            LocalDate.now().minusDays(30), LocalDate.now(),
                            null, null, null, null,
                            PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")));

                    for (TransactionResponse tx : txPage.getContent()) {
                        txCount++;
                        String type = tx.getType() != null ? tx.getType().toString() : "UNKNOWN";
                        BigDecimal amount = tx.getAmount();

                        if ("DEPOSIT".equals(type) || "TRANSFER_IN".equals(type)) {
                            totalIncome = totalIncome.add(amount);
                        } else {
                            totalExpense = totalExpense.add(amount);
                        }

                        String category = tx.getCategory() != null ? tx.getCategory() : "Khác";
                        categorySpending.merge(category, amount, BigDecimal::add);
                    }
                }

                ctx.append(String.format("  - Tổng giao dịch: %d\n", txCount));
                ctx.append(String.format("  - Tổng thu nhập: +%s VND\n", formatCurrency(totalIncome)));
                ctx.append(String.format("  - Tổng chi tiêu: -%s VND\n", formatCurrency(totalExpense)));

                if (!categorySpending.isEmpty()) {
                    ctx.append("  - Chi tiêu theo danh mục:\n");
                    categorySpending.entrySet().stream()
                            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                            .limit(5)
                            .forEach(e -> ctx.append(String.format("    • %s: %s VND\n", e.getKey(), formatCurrency(e.getValue()))));
                }
            }

        } catch (Exception e) {
            ctx.append("(Không thể tải dữ liệu giao dịch)\n");
            log.warn("Failed to build financial context for user: {}", userId, e);
        }

        return ctx.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractResponseText(Map<String, Object> response) {
        if (response == null) return "Xin lỗi, không nhận được phản hồi từ AI.";

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
        }

        return "Xin lỗi, đã xảy ra lỗi khi xử lý phản hồi.";
    }

    private String fallbackResponse(String message) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("chi tiêu") || lowerMsg.contains("spending")) {
            return "📊 **Phân tích chi tiêu**\n\nĐể xem phân tích chi tiêu chi tiết, vui lòng cấu hình Gemini API key.\n\n" +
                   "💡 **Tip**: Hãy theo dõi chi tiêu hàng ngày và đặt ngân sách cho từng danh mục để kiểm soát tài chính tốt hơn.";
        } else if (lowerMsg.contains("tiết kiệm") || lowerMsg.contains("saving")) {
            return "💰 **Lời khuyên tiết kiệm**\n\n" +
                   "1. Áp dụng quy tắc 50/30/20: 50% nhu cầu, 30% mong muốn, 20% tiết kiệm\n" +
                   "2. Tự động chuyển tiền vào tài khoản tiết kiệm đầu tháng\n" +
                   "3. Cắt giảm chi tiêu không cần thiết\n" +
                   "4. Đặt mục tiêu tiết kiệm cụ thể";
        } else if (lowerMsg.contains("đầu tư") || lowerMsg.contains("invest")) {
            return "📈 **Tư vấn đầu tư cơ bản**\n\n" +
                   "1. Xây dựng quỹ dự phòng 3-6 tháng chi phí trước\n" +
                   "2. Đa dạng hóa danh mục đầu tư\n" +
                   "3. Đầu tư dài hạn, không chạy theo xu hướng ngắn hạn\n" +
                   "4. Chỉ đầu tư số tiền bạn có thể chấp nhận mất";
        }
        return "🤖 **AI Financial Advisor**\n\nXin chào! Tôi có thể giúp bạn:\n\n" +
               "• Phân tích chi tiêu hàng tháng\n" +
               "• Gợi ý cách tiết kiệm\n" +
               "• Tư vấn đầu tư cơ bản\n" +
               "• Phát hiện giao dịch bất thường\n\n" +
               "💡 Để trải nghiệm đầy đủ, cấu hình `GEMINI_API_KEY` trong environment.";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}
