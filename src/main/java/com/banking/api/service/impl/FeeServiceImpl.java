package com.banking.api.service.impl;

import com.banking.api.repository.AccountRepository;
import com.banking.api.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService {

    private static final BigDecimal FEE_TIER_1 = new BigDecimal("3300"); // < 5M
    private static final BigDecimal FEE_TIER_2 = new BigDecimal("5500"); // 5M - 50M
    private static final BigDecimal FEE_TIER_3 = new BigDecimal("11000"); // > 50M

    private static final BigDecimal THRESHOLD_5M = new BigDecimal("5000000");
    private static final BigDecimal THRESHOLD_50M = new BigDecimal("50000000");

    private final AccountRepository accountRepository;

    @Override
    public BigDecimal calculateTransferFee(BigDecimal amount) {
        if (amount.compareTo(THRESHOLD_5M) < 0) {
            return FEE_TIER_1;
        } else if (amount.compareTo(THRESHOLD_50M) <= 0) {
            return FEE_TIER_2;
        } else {
            return FEE_TIER_3;
        }
    }

    @Override
    public boolean isInternalTransfer(String sourceAccountNumber, String destinationAccountNumber) {
        // All accounts in our system are internal. In a real banking system,
        // we would check if the destination belongs to another bank.
        boolean sourceExists = accountRepository.existsByAccountNumber(sourceAccountNumber);
        boolean destExists = accountRepository.existsByAccountNumber(destinationAccountNumber);
        return sourceExists && destExists;
    }
}
