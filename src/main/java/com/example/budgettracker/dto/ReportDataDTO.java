package com.example.budgettracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ReportDataDTO {
    private Map<String, BigDecimal> monthlyTrend;
    private Map<String, BigDecimal> categorySplit;
    private Map<String, BigDecimal> paymentModeSplit;
    private BigDecimal totalSpentYear;
    private BigDecimal averageMonthlySpend;
}
