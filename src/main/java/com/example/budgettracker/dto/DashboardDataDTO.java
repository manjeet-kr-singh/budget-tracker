package com.example.budgettracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardDataDTO {
    // KPIs
    private BigDecimal totalExpense;
    private BigDecimal todaysExpense;
    private BigDecimal monthlyBudget;
    private BigDecimal remainingBudget;

    // Charts
    private Map<String, BigDecimal> categorySplit;
    private Map<String, BigDecimal> paymentModeSplit;
    private Map<String, BigDecimal> dailyTrend; // Date -> Amount
}
