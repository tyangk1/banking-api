package com.banking.api.service;

import java.time.LocalDate;

public interface StatementService {

    byte[] generateAccountStatement(String accountId, String userId, LocalDate fromDate, LocalDate toDate);
}
