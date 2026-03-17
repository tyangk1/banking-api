package com.banking.api.service;

import com.banking.api.model.dto.response.TransactionResponse;

public interface ReceiptService {
    String generateHtmlReceipt(TransactionResponse transaction);

    byte[] generatePdfReceipt(TransactionResponse transaction);
}
