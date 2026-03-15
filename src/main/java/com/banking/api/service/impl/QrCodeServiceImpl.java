package com.banking.api.service.impl;

import com.banking.api.service.QrCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private static final String SEPARATOR = "|";
    private static final String PREFIX = "BANKQR";

    @Override
    public byte[] generateQrCode(String accountNumber, String holderName, BigDecimal amount, int size) {
        // Build payload: BANKQR|accountNumber|holderName|amount
        StringBuilder payload = new StringBuilder();
        payload.append(PREFIX).append(SEPARATOR)
               .append(accountNumber).append(SEPARATOR)
               .append(holderName);
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            payload.append(SEPARATOR).append(amount.toPlainString());
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 2
            );

            BitMatrix matrix = writer.encode(payload.toString(), BarcodeFormat.QR_CODE, size, size, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            log.info("Generated QR code for account: {}, size: {}x{}", accountNumber, size, size);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @Override
    public QrPayload decodePayload(String payload) {
        if (payload == null || !payload.startsWith(PREFIX + SEPARATOR)) {
            throw new IllegalArgumentException("Invalid QR payload format");
        }

        String[] parts = payload.split("\\|");
        if (parts.length < 3) {
            throw new IllegalArgumentException("QR payload must have at least: BANKQR|accountNumber|holderName");
        }

        String accountNumber = parts[1];
        String holderName = parts[2];
        BigDecimal amount = parts.length > 3 ? new BigDecimal(parts[3]) : null;

        return new QrPayload(accountNumber, holderName, amount);
    }
}
