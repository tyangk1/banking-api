package com.banking.api.service;

public interface QrCodeService {

    /**
     * Generate a QR code PNG image as byte[] for an account.
     * QR payload format: BANK|accountNumber|holderName|amount(optional)
     */
    byte[] generateQrCode(String accountNumber, String holderName, java.math.BigDecimal amount, int size);

    /**
     * Decode a QR code payload string into structured data.
     */
    QrPayload decodePayload(String payload);

    record QrPayload(String accountNumber, String holderName, java.math.BigDecimal amount) {}
}
