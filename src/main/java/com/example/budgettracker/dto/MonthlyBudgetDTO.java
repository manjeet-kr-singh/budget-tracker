package com.example.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBudgetDTO {
    private Integer month;
    private Integer year;
    private List<CategoryBudgetDTO> categoryBudgets;
}
