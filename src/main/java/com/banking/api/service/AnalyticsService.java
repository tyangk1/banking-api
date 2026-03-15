package com.banking.api.service;

import com.banking.api.model.dto.response.SpendingAnalyticsResponse;

import java.time.LocalDate;

public interface AnalyticsService {

    SpendingAnalyticsResponse getSpendingAnalytics(String accountId, String userId, LocalDate fromDate, LocalDate toDate);

    SpendingAnalyticsResponse getDashboardAnalytics(String userId, LocalDate fromDate, LocalDate toDate);
}
