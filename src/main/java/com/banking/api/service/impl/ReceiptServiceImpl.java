package com.banking.api.service.impl;

import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.service.ReceiptService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat NUM_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public String generateHtmlReceipt(TransactionResponse tx) {
        String typeLabel = tx.getType() == TransactionType.TRANSFER ? "Chuyển khoản" : "Nạp tiền";
        String amountFormatted = formatAmount(tx.getAmount()) + " " + tx.getCurrency();
        String feeFormatted = formatAmount(tx.getFee()) + " " + tx.getCurrency();
        BigDecimal total = tx.getAmount().add(tx.getFee() != null ? tx.getFee() : BigDecimal.ZERO);
        String totalFormatted = formatAmount(total) + " " + tx.getCurrency();
        String dateFormatted = tx.getCreatedAt() != null ? tx.getCreatedAt().format(DATE_FMT) : "N/A";

        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8">
                <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body { font-family:'Segoe UI',sans-serif; background:#0f1923; color:#e2e8f0; padding:24px; }
                .receipt { max-width:480px; margin:auto; background:#1a2332; border-radius:16px;
                           border:1px solid rgba(255,255,255,.1); overflow:hidden; }
                .header { background:linear-gradient(135deg,#6366f1,#8b5cf6); padding:32px 24px; text-align:center; }
                .header h2 { font-size:20px; color:#fff; margin-bottom:4px; }
                .header .ref { font-size:13px; color:rgba(255,255,255,.7); }
                .status { display:inline-block; background:rgba(16,185,129,.2); color:#10b981;
                          padding:4px 16px; border-radius:20px; font-size:13px; font-weight:600;
                          margin-top:12px; }
                .body { padding:24px; }
                .row { display:flex; justify-content:space-between; padding:12px 0;
                       border-bottom:1px solid rgba(255,255,255,.06); }
                .row:last-child { border-bottom:none; }
                .row .label { color:#94a3b8; font-size:14px; }
                .row .value { color:#e2e8f0; font-size:14px; font-weight:600; text-align:right; }
                .total-row { background:rgba(99,102,241,.1); border-radius:12px; padding:16px;
                             margin-top:16px; display:flex; justify-content:space-between; }
                .total-row .label { color:#a5b4fc; font-weight:600; }
                .total-row .value { color:#818cf8; font-size:18px; font-weight:700; }
                .footer { text-align:center; padding:16px 24px; color:#64748b; font-size:12px;
                          border-top:1px solid rgba(255,255,255,.06); }
                .success { background:rgba(16,185,129,.2); color:#10b981; }
                .failed { background:rgba(239,68,68,.2); color:#ef4444; }
                @media print { body { background:#fff; color:#111; } .receipt { border:1px solid #ddd; background:#fff; }
                  .header { print-color-adjust:exact; -webkit-print-color-adjust:exact; }
                  .row .label, .footer { color:#666; } .row .value { color:#111; } }
                </style></head><body>
                <div class="receipt">
                  <div class="header">
                    <h2>%s</h2>
                    <div class="ref">%s</div>
                    <div class="status %s">%s</div>
                  </div>
                  <div class="body">
                    <div class="row"><span class="label">Ngày giao dịch</span><span class="value">%s</span></div>
                    %s
                    %s
                    <div class="row"><span class="label">Số tiền</span><span class="value">%s</span></div>
                    <div class="row"><span class="label">Phí giao dịch</span><span class="value">%s</span></div>
                    <div class="total-row"><span class="label">Tổng cộng</span><span class="value">%s</span></div>
                    %s
                  </div>
                  <div class="footer">Premium Banking — Biên lai điện tử<br>%s</div>
                </div>
                </body></html>
                """
                .formatted(
                        typeLabel,
                        tx.getReferenceNumber(),
                        tx.getStatus().name().equals("COMPLETED") ? "success" : "failed",
                        tx.getStatus().name().equals("COMPLETED") ? "✓ Thành công" : "✗ Thất bại",
                        dateFormatted,
                        tx.getSourceAccountNumber() != null
                                ? "<div class=\"row\"><span class=\"label\">TK nguồn</span><span class=\"value\">"
                                        + tx.getSourceAccountNumber() + "</span></div>"
                                : "",
                        tx.getDestinationAccountNumber() != null
                                ? "<div class=\"row\"><span class=\"label\">TK đích</span><span class=\"value\">"
                                        + tx.getDestinationAccountNumber() + "</span></div>"
                                : "",
                        amountFormatted,
                        feeFormatted,
                        totalFormatted,
                        tx.getDescription() != null
                                ? "<div class=\"row\"><span class=\"label\">Nội dung</span><span class=\"value\">"
                                        + tx.getDescription() + "</span></div>"
                                : "",
                        dateFormatted);
    }

    @Override
    public byte[] generatePdfReceipt(TransactionResponse tx) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            String typeLabel = tx.getType() == TransactionType.TRANSFER ? "BIEN LAI CHUYEN KHOAN" : "BIEN LAI NAP TIEN";
            String dateFormatted = tx.getCreatedAt() != null ? tx.getCreatedAt().format(DATE_FMT) : "N/A";

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new java.awt.Color(99, 102, 241));
            Paragraph title = new Paragraph(typeLabel, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);

            // Reference
            Font refFont = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY);
            Paragraph ref = new Paragraph(tx.getReferenceNumber(), refFont);
            ref.setAlignment(Element.ALIGN_CENTER);
            ref.setSpacingAfter(4);
            document.add(ref);

            // Status
            Font statusFont = new Font(Font.HELVETICA, 12, Font.BOLD,
                    tx.getStatus().name().equals("COMPLETED") ? new java.awt.Color(16, 185, 129) : java.awt.Color.RED);
            Paragraph status = new Paragraph(
                    tx.getStatus().name().equals("COMPLETED") ? "THANH CONG" : "THAT BAI", statusFont);
            status.setAlignment(Element.ALIGN_CENTER);
            status.setSpacingAfter(20);
            document.add(status);

            // Separator
            document.add(new Paragraph("────────────────────────────────────"));

            // Details table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 40, 60 });
            table.setSpacingBefore(12);

            addRow(table, "Ngay giao dich", dateFormatted);
            if (tx.getSourceAccountNumber() != null)
                addRow(table, "TK nguon", tx.getSourceAccountNumber());
            if (tx.getDestinationAccountNumber() != null)
                addRow(table, "TK dich", tx.getDestinationAccountNumber());
            addRow(table, "So tien", formatAmount(tx.getAmount()) + " " + tx.getCurrency());
            addRow(table, "Phi giao dich", formatAmount(tx.getFee()) + " " + tx.getCurrency());

            BigDecimal total = tx.getAmount().add(tx.getFee() != null ? tx.getFee() : BigDecimal.ZERO);
            Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD, new java.awt.Color(99, 102, 241));
            PdfPCell labelCell = new PdfPCell(new Phrase("TONG CONG", boldFont));
            labelCell.setBorder(Rectangle.TOP);
            labelCell.setPaddingTop(8);
            PdfPCell valueCell = new PdfPCell(new Phrase(formatAmount(total) + " " + tx.getCurrency(), boldFont));
            valueCell.setBorder(Rectangle.TOP);
            valueCell.setPaddingTop(8);
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(labelCell);
            table.addCell(valueCell);

            if (tx.getDescription() != null) {
                addRow(table, "Noi dung", tx.getDescription());
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("\n\nPremium Banking — Bien lai dien tu\n" + dateFormatted,
                    new Font(Font.HELVETICA, 9, Font.ITALIC, java.awt.Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF receipt", e);
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null)
            return "0";
        return NUM_FMT.format(amount.longValue());
    }
}
