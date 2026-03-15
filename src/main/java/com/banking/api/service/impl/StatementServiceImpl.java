package com.banking.api.service.impl;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.Transaction;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.TransactionRepository;
import com.banking.api.repository.specification.TransactionSpecification;
import com.banking.api.service.StatementService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatementServiceImpl implements StatementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Fonts
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(51, 51, 51));
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(51, 51, 51));
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);

    @Override
    public byte[] generateAccountStatement(String accountId, String userId,
                                            LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only generate statements for your own accounts");
        }

        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        if (toDate == null) toDate = LocalDate.now();

        // Fetch transactions
        List<Transaction> transactions = transactionRepository.findAll(
                TransactionSpecification.withFilters(
                        accountId, null, null, fromDate, toDate, null, null, null, null),
                Sort.by(Sort.Direction.ASC, "createdAt"));

        log.info("Generating PDF statement for account {} — {} transactions ({} to {})",
                accountId, transactions.size(), fromDate, toDate);

        return buildPdf(account, transactions, fromDate, toDate);
    }

    // ==================== PDF Builder ====================

    private byte[] buildPdf(Account account, List<Transaction> transactions,
                            LocalDate fromDate, LocalDate toDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 50, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Bank Header
            addBankHeader(document);

            // Statement Title
            Paragraph title = new Paragraph("ACCOUNT STATEMENT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Account Info Table
            addAccountInfo(document, account, fromDate, toDate);

            // Transactions Table
            addTransactionTable(document, transactions, account.getId());

            // Summary
            addSummary(document, transactions, account);

            // Footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF statement", e);
            throw new RuntimeException("Failed to generate PDF statement: " + e.getMessage());
        }
    }

    private void addBankHeader(Document document) throws DocumentException {
        Paragraph bankName = new Paragraph("PREMIUM BANKING", new Font(Font.HELVETICA, 22, Font.BOLD, new Color(0, 51, 102)));
        bankName.setAlignment(Element.ALIGN_CENTER);
        document.add(bankName);

        Paragraph bankSlogan = new Paragraph("Your Trusted Financial Partner", new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY));
        bankSlogan.setAlignment(Element.ALIGN_CENTER);
        bankSlogan.setSpacingAfter(5);
        document.add(bankSlogan);

        // Separator line
        Paragraph line = new Paragraph("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0, 51, 102)));
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingAfter(15);
        document.add(line);
    }

    private void addAccountInfo(Document document, Account account, LocalDate from, LocalDate to) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 2});
        infoTable.setSpacingAfter(15);

        Color infoBg = new Color(240, 245, 250);

        addInfoRow(infoTable, "Account Holder:", account.getUser().getFullName(), infoBg);
        addInfoRow(infoTable, "Account Number:", account.getAccountNumber(), infoBg);
        addInfoRow(infoTable, "Account Type:", account.getAccountType().name(), infoBg);
        addInfoRow(infoTable, "Currency:", account.getCurrency(), infoBg);
        addInfoRow(infoTable, "Current Balance:", formatAmount(account.getBalance(), account.getCurrency()), infoBg);
        addInfoRow(infoTable, "Statement Period:", from.format(DATE_FMT) + " — " + to.format(DATE_FMT), infoBg);

        document.add(infoTable);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Color bg) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(0);
        labelCell.setBackgroundColor(bg);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(0);
        valueCell.setBackgroundColor(bg);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private void addTransactionTable(Document document, List<Transaction> transactions, String accountId)
            throws DocumentException {

        Paragraph txTitle = new Paragraph("Transaction Details", new Font(Font.HELVETICA, 13, Font.BOLD, new Color(0, 51, 102)));
        txTitle.setSpacingBefore(10);
        txTitle.setSpacingAfter(8);
        document.add(txTitle);

        if (transactions.isEmpty()) {
            Paragraph noTx = new Paragraph("No transactions found in this period.", VALUE_FONT);
            noTx.setSpacingAfter(15);
            document.add(noTx);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.3f, 0.8f, 1.2f, 0.8f, 1.5f, 1.0f});

        Color headerBg = new Color(0, 51, 102);

        // Headers
        String[] headers = {"Date", "Type", "Amount", "Fee", "Description", "Balance After"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Rows
        Color evenRow = new Color(248, 250, 252);
        Color oddRow = Color.WHITE;
        Color incomeColor = new Color(0, 128, 0);
        Color expenseColor = new Color(180, 0, 0);

        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            Color rowBg = (i % 2 == 0) ? evenRow : oddRow;
            boolean isIncome = isIncome(tx, accountId);
            String sign = isIncome ? "+" : "-";
            Color amountColor = isIncome ? incomeColor : expenseColor;

            addCell(table, tx.getCreatedAt().format(DATETIME_FMT), BODY_FONT, rowBg, Element.ALIGN_LEFT);
            addCell(table, tx.getType().name(), BODY_FONT, rowBg, Element.ALIGN_CENTER);

            // Amount with color
            PdfPCell amountCell = new PdfPCell(new Phrase(sign + formatAmount(tx.getAmount(), tx.getCurrency()),
                    new Font(Font.HELVETICA, 9, Font.BOLD, amountColor)));
            amountCell.setBackgroundColor(rowBg);
            amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            amountCell.setPadding(5);
            table.addCell(amountCell);

            addCell(table, formatAmount(tx.getFee(), tx.getCurrency()), BODY_FONT, rowBg, Element.ALIGN_RIGHT);

            // Description (truncate if too long)
            String desc = tx.getDescription() != null ? tx.getDescription() : "";
            if (desc.length() > 35) desc = desc.substring(0, 32) + "...";
            addCell(table, desc, BODY_FONT, rowBg, Element.ALIGN_LEFT);

            addCell(table, formatAmount(tx.getBalanceAfterTransaction(), tx.getCurrency()), BODY_FONT, rowBg, Element.ALIGN_RIGHT);
        }

        document.add(table);
    }

    private void addSummary(Document document, List<Transaction> transactions, Account account) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Summary",
                new Font(Font.HELVETICA, 13, Font.BOLD, new Color(0, 51, 102)));
        summaryTitle.setSpacingBefore(15);
        summaryTitle.setSpacingAfter(8);
        document.add(summaryTitle);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int incomeCount = 0;
        int expenseCount = 0;

        for (Transaction tx : transactions) {
            if (isIncome(tx, account.getId())) {
                totalIncome = totalIncome.add(tx.getAmount());
                incomeCount++;
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
                expenseCount++;
            }
        }

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryTable.setWidths(new float[]{1, 1});

        Color summaryBg = new Color(240, 245, 250);

        addInfoRow(summaryTable, "Total Transactions:", String.valueOf(transactions.size()), summaryBg);
        addInfoRow(summaryTable, "Total Income (" + incomeCount + " txns):", "+" + formatAmount(totalIncome, account.getCurrency()), summaryBg);
        addInfoRow(summaryTable, "Total Expense (" + expenseCount + " txns):", "-" + formatAmount(totalExpense, account.getCurrency()), summaryBg);

        BigDecimal net = totalIncome.subtract(totalExpense);
        addInfoRow(summaryTable, "Net Change:", (net.signum() >= 0 ? "+" : "") + formatAmount(net, account.getCurrency()), summaryBg);

        document.add(summaryTable);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "\nThis statement is generated electronically by Premium Banking. " +
                "Generated on " + LocalDate.now().format(DATE_FMT) + ". " +
                "For any queries, contact support@premiumbanking.com.",
                FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    // ==================== Helpers ====================

    private void addCell(PdfPTable table, String text, Font font, Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private boolean isIncome(Transaction tx, String accountId) {
        if (tx.getType() == TransactionType.DEPOSIT) {
            return tx.getDestinationAccount() != null && tx.getDestinationAccount().getId().equals(accountId);
        }
        return tx.getDestinationAccount() != null && tx.getDestinationAccount().getId().equals(accountId);
    }

    private String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) return "0";
        if ("VND".equals(currency)) {
            return String.format("%,.0f VND", amount);
        }
        return String.format("%,.2f %s", amount, currency);
    }
}
