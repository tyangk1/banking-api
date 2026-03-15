package com.banking.api.controller;

import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.entity.User;
import com.banking.api.repository.UserRepository;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.OtpService;
import com.banking.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/otp")
@RequiredArgsConstructor
@Tag(name = "OTP Verification", description = "APIs for OTP-based transfer verification")
@SecurityRequirement(name = "bearerAuth")
public class OtpController {

    private final OtpService otpService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/request")
    @Operation(summary = "Request OTP for a transfer",
               description = "Generates and sends OTP to user's email for transfers >= 5,000,000 VND")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestOtp(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        // Check if OTP is needed
        if (!otpService.requiresOtp(request.getAmount())) {
            return ResponseEntity.ok(ApiResponse.success("OTP not required for this amount",
                    Map.of("requiresOtp", false)));
        }

        // Get user email
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String transferDetails = String.format(
                "Từ: %s → Đến: %s\nSố tiền: %s VND\nNội dung: %s",
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount().toPlainString(),
                request.getDescription() != null ? request.getDescription() : "Không có"
        );

        String otpToken = otpService.generateAndSendOtp(
                principal.getId(), user.getEmail(), transferDetails);

        // Mask email for display
        String email = user.getEmail();
        String maskedEmail = email.substring(0, 2) + "***" + email.substring(email.indexOf('@'));

        return ResponseEntity.ok(ApiResponse.success("OTP đã được gửi đến email của bạn",
                Map.of(
                        "requiresOtp", true,
                        "otpToken", otpToken,
                        "maskedEmail", maskedEmail,
                        "expiresIn", 300 // 5 minutes in seconds
                )));
    }

    @PostMapping("/verify-and-transfer")
    @Operation(summary = "Verify OTP and execute transfer",
               description = "Verifies the OTP code and executes the transfer if valid")
    public ResponseEntity<ApiResponse<TransactionResponse>> verifyAndTransfer(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String otpToken = (String) body.get("otpToken");
        String otpCode = (String) body.get("otpCode");

        // Verify OTP
        if (!otpService.verifyOtp(otpToken, otpCode)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<TransactionResponse>builder()
                            .success(false)
                            .message("Mã OTP không hợp lệ hoặc đã hết hạn")
                            .build());
        }

        // Build TransferRequest from body
        TransferRequest transferRequest = TransferRequest.builder()
                .sourceAccountNumber((String) body.get("sourceAccountNumber"))
                .destinationAccountNumber((String) body.get("destinationAccountNumber"))
                .amount(new java.math.BigDecimal(body.get("amount").toString()))
                .description((String) body.get("description"))
                .build();

        // Execute transfer
        TransactionResponse response = transactionService.transfer(transferRequest, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Giao dịch thành công!", response));
    }
}
