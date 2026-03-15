package com.banking.api.controller;

import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.AccountService;
import com.banking.api.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code", description = "APIs for QR code-based transfers")
@SecurityRequirement(name = "bearerAuth")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final AccountService accountService;

    @GetMapping(value = "/generate/{accountId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate QR code for an account",
               description = "Returns a PNG image of the QR code containing account info for receiving transfers")
    public ResponseEntity<byte[]> generateQr(
            @PathVariable String accountId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(defaultValue = "400") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        // Get account details
        AccountResponse account = accountService.getAccountById(accountId);

        byte[] qrImage = qrCodeService.generateQrCode(
                account.getAccountNumber(), account.getOwnerName(), amount, size);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qr-" + account.getAccountNumber() + ".png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @PostMapping("/decode")
    @Operation(summary = "Decode QR payload", description = "Parse QR payload string into structured transfer info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> decodeQr(
            @RequestBody Map<String, String> body) {
        String payload = body.get("payload");
        QrCodeService.QrPayload decoded = qrCodeService.decodePayload(payload);

        Map<String, Object> result = Map.of(
                "accountNumber", decoded.accountNumber(),
                "holderName", decoded.holderName(),
                "amount", decoded.amount() != null ? decoded.amount() : 0
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
