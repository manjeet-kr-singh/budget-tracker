package com.example.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusDTO {
    private Long id;
    private String category;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private double percentage;
    private String status; // "Safe", "Warning", "Overspent"
}
