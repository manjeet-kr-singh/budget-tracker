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
    private final com.example.budgettracker.repository.UserRepository userRepository;

    private com.example.budgettracker.entity.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public BigDecimal getGlobalBudget() {
        return budgetRepository.findByCategoryAndUser("GLOBAL", getCurrentUser())
                .map(Budget::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    public void setGlobalBudget(BigDecimal amount) {
        com.example.budgettracker.entity.User user = getCurrentUser();
        Budget budget = budgetRepository.findByCategoryAndUser("GLOBAL", user)
                .orElse(new Budget());
        if (budget.getId() == null) {
            budget.setCategory("GLOBAL");
            budget.setMonth(LocalDate.now().getMonthValue());
            budget.setYear(LocalDate.now().getYear());
            budget.setUser(user);
        }
        budget.setAmount(amount);
        budgetRepository.save(budget);
    }

    public List<Budget> getAllBudgets() {
        // This likely needs to be filtered by user too? simpler to find all for user
        // But repo doesn't have findAllByUser yet. Or just use month/year ones.
        // Assuming this method is slightly legacy or unused, but let's check repo.
        // I will add findByUser to repo if strictly needed, but better to avoid blanket
        // getAll.
        // usage check: budgetController might use it?
        // For now, return empty or implement proper fetch.
        // Creating logic to fetch all for user:
        // Since I haven't added findByUser in BudgetRepository yet, I'll stick to
        // specific method calls.
        // Wait, I should add findByUser to BudgetRepository to support this properly.
        // For now, I'll filter in memory or throw/return empty.
        // Better: Find all budgets for the user.
        return budgetRepository.findAll().stream()
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(getCurrentUser().getId()))
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalMonthlyBudget(int month, int year) {
        return budgetRepository.findByMonthAndYearAndUser(month, year, getCurrentUser()).stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // New method for monthly planning
    public com.example.budgettracker.dto.MonthlyBudgetDTO getMonthlyBudgetPlan(int month, int year) {
        List<Budget> existingBudgets = budgetRepository.findByMonthAndYearAndUser(month, year, getCurrentUser());

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
        com.example.budgettracker.entity.User user = getCurrentUser();
        // Simple iteration to save each
        for (com.example.budgettracker.dto.CategoryBudgetDTO item : dto.getCategoryBudgets()) {
            Budget budget = budgetRepository
                    .findByCategoryAndMonthAndYearAndUser(item.getCategory().getDisplayName(), dto.getMonth(),
                            dto.getYear(), user)
                    .orElse(new Budget());

            if (budget.getId() == null) {
                budget.setCategory(item.getCategory().getDisplayName());
                budget.setMonth(dto.getMonth());
                budget.setYear(dto.getYear());
                budget.setUser(user);
            }
            budget.setAmount(item.getAmount());
            budgetRepository.save(budget);
        }
    }

    // Legacy method - updated to support month/year
    public void saveBudget(Budget budget) {
        com.example.budgettracker.entity.User user = getCurrentUser();
        // Defaulting to current date if missing
        if (budget.getMonth() == null)
            budget.setMonth(LocalDate.now().getMonthValue());
        if (budget.getYear() == null)
            budget.setYear(LocalDate.now().getYear());

        Optional<Budget> existing = budgetRepository.findByCategoryAndMonthAndYearAndUser(budget.getCategory(),
                budget.getMonth(), budget.getYear(), user);
        if (existing.isPresent() && (budget.getId() == null || !budget.getId().equals(existing.get().getId()))) {
            Budget b = existing.get();
            b.setAmount(budget.getAmount());
            budgetRepository.save(b);
        } else {
            budget.setUser(user);
            budgetRepository.save(budget);
        }
    }

    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id).orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Access Denied");
        }
        budgetRepository.deleteById(id);
    }

    public List<BudgetStatusDTO> getBudgetStatus(LocalDate date) {
        com.example.budgettracker.entity.User user = getCurrentUser();
        LocalDate refDate = (date != null) ? date : LocalDate.now();
        int month = refDate.getMonthValue();
        int year = refDate.getYear();

        LocalDate start = refDate.withDayOfMonth(1);
        LocalDate end = refDate.withDayOfMonth(refDate.lengthOfMonth());

        // Get monthly budgets
        List<Budget> budgets = budgetRepository.findByMonthAndYearAndUser(month, year, user);

        // Get expenses sum
        Map<String, BigDecimal> expenseMap = expenseRepository.sumAmountByCategory(start, end, user).stream()
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
