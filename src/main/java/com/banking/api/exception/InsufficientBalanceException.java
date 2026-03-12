package com.banking.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String accountNumber, java.math.BigDecimal requested, java.math.BigDecimal available) {
        super(String.format("Insufficient balance in account %s. Requested: %s, Available: %s",
                accountNumber, requested, available));
    }
}
