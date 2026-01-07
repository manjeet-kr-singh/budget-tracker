package com.example.budgettracker.dto;

import com.example.budgettracker.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudgetDTO {
    private Category category;
    private BigDecimal amount;
}
