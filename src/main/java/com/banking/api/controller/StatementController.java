package com.banking.api.controller;

import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/v1/statements")
@RequiredArgsConstructor
@Tag(name = "Account Statements", description = "APIs for generating and downloading account statements")
@SecurityRequirement(name = "bearerAuth")
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{accountId}/pdf")
    @Operation(summary = "Download account statement PDF",
               description = "Generate and download a PDF statement for the specified account and date range")
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable String accountId,
            @Parameter(description = "Start date (yyyy-MM-dd), default: first of last month")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (yyyy-MM-dd), default: today")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        byte[] pdfBytes = statementService.generateAccountStatement(accountId, principal.getId(), fromDate, toDate);

        String from = (fromDate != null ? fromDate : LocalDate.now().minusMonths(1).withDayOfMonth(1))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String to2 = (toDate != null ? toDate : LocalDate.now())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "statement_" + from + "_" + to2 + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
