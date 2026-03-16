package com.banking.api.service;

import java.math.BigDecimal;

public interface FeeService {

    /**
     * Calculate transfer fee based on amount.
     * Fee schedule:
     * - < 5,000,000 VND: 3,300 VND
     * - 5M - 50M VND: 5,500 VND
     * - > 50,000,000 VND: 11,000 VND
     */
    BigDecimal calculateTransferFee(BigDecimal amount);

    /**
     * Check if transfer is internal (same bank). Internal transfers have no fee.
     */
    boolean isInternalTransfer(String sourceAccountNumber, String destinationAccountNumber);
}
