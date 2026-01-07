package com.example.budgettracker.service;

import com.example.budgettracker.dto.BudgetStatusDTO;
import com.example.budgettracker.entity.Budget;
import com.example.budgettracker.repository.BudgetRepository;
import com.example.budgettracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BigDecimal getGlobalBudget() {
        return budgetRepository.findByCategory("GLOBAL")
                .map(Budget::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    public void setGlobalBudget(BigDecimal amount) {
        Budget budget = budgetRepository.findByCategory("GLOBAL")
                .orElse(new Budget());
        if (budget.getId() == null) {
            budget.setCategory("GLOBAL");
            // Defaults for global if needed
            budget.setMonth(LocalDate.now().getMonthValue());
            budget.setYear(LocalDate.now().getYear());
        }
        budget.setAmount(amount);
        budgetRepository.save(budget);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public BigDecimal getTotalMonthlyBudget(int month, int year) {
        return budgetRepository.findByMonthAndYear(month, year).stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // New method for monthly planning
    public com.example.budgettracker.dto.MonthlyBudgetDTO getMonthlyBudgetPlan(int month, int year) {
        List<Budget> existingBudgets = budgetRepository.findByMonthAndYear(month, year);

        List<com.example.budgettracker.dto.CategoryBudgetDTO> categoryBudgets = java.util.Arrays
                .stream(com.example.budgettracker.enums.Category.values())
                .map(cat -> {
                    BigDecimal amount = existingBudgets.stream()
                            .filter(b -> b.getCategory().equals(cat.getDisplayName()))
                            .findFirst()
                            .map(Budget::getAmount)
                            .orElse(BigDecimal.ZERO);
                    return new com.example.budgettracker.dto.CategoryBudgetDTO(cat, amount);
                })
                .collect(Collectors.toList());

        return new com.example.budgettracker.dto.MonthlyBudgetDTO(month, year, categoryBudgets);
    }

    public void saveMonthlyBudgetPlan(com.example.budgettracker.dto.MonthlyBudgetDTO dto) {
        // Simple iteration to save each
        for (com.example.budgettracker.dto.CategoryBudgetDTO item : dto.getCategoryBudgets()) {
            Budget budget = budgetRepository
                    .findByCategoryAndMonthAndYear(item.getCategory().getDisplayName(), dto.getMonth(), dto.getYear())
                    .orElse(new Budget());

            if (budget.getId() == null) {
                budget.setCategory(item.getCategory().getDisplayName());
                budget.setMonth(dto.getMonth());
                budget.setYear(dto.getYear());
            }
            budget.setAmount(item.getAmount());
            budgetRepository.save(budget);
        }
    }

    // Legacy method - updated to support month/year
    public void saveBudget(Budget budget) {
        // Defaulting to current date if missing
        if (budget.getMonth() == null)
            budget.setMonth(LocalDate.now().getMonthValue());
        if (budget.getYear() == null)
            budget.setYear(LocalDate.now().getYear());

        Optional<Budget> existing = budgetRepository.findByCategoryAndMonthAndYear(budget.getCategory(),
                budget.getMonth(), budget.getYear());
        if (existing.isPresent() && (budget.getId() == null || !budget.getId().equals(existing.get().getId()))) {
            Budget b = existing.get();
            b.setAmount(budget.getAmount());
            budgetRepository.save(b);
        } else {
            budgetRepository.save(budget);
        }
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    public List<BudgetStatusDTO> getBudgetStatus(LocalDate date) {
        LocalDate refDate = (date != null) ? date : LocalDate.now();
        int month = refDate.getMonthValue();
        int year = refDate.getYear();

        LocalDate start = refDate.withDayOfMonth(1);
        LocalDate end = refDate.withDayOfMonth(refDate.lengthOfMonth());

        // Get monthly budgets
        List<Budget> budgets = budgetRepository.findByMonthAndYear(month, year);

        // Get expenses sum
        Map<String, BigDecimal> expenseMap = expenseRepository.sumAmountByCategory(start, end).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]));

        return budgets.stream()
                .filter(b -> !"GLOBAL".equalsIgnoreCase(b.getCategory()))
                .map(b -> {
                    BigDecimal spent = expenseMap.getOrDefault(b.getCategory(), BigDecimal.ZERO);
                    BigDecimal budget = b.getAmount();
                    double percentage = 0.0;
                    if (budget.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = spent.divide(budget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }

                    String status = "Safe";
                    if (percentage >= 100)
                        status = "Overspent";
                    else if (percentage >= 80)
                        status = "Warning";

                    return BudgetStatusDTO.builder()
                            .id(b.getId())
                            .category(b.getCategory())
                            .budgetAmount(budget)
                            .spentAmount(spent)
                            .percentage(percentage)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
