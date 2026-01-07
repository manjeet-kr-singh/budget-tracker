package com.example.budgettracker.controller;

import com.example.budgettracker.dto.DashboardDataDTO;
import com.example.budgettracker.entity.Expense;
import com.example.budgettracker.repository.ExpenseRepository;
import com.example.budgettracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

        private final ExpenseRepository expenseRepository;
        private final BudgetService budgetService;

        @GetMapping("/stats")
        public ResponseEntity<DashboardDataDTO> getDashboardStats(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                // Default to current month if date not provided (using the 1st of the month as
                // reference)
                LocalDate refDate = (date != null) ? date : LocalDate.now();
                YearMonth yearMonth = YearMonth.from(refDate);
                LocalDate startOfMonth = yearMonth.atDay(1);
                LocalDate endOfMonth = yearMonth.atEndOfMonth();
                LocalDate today = LocalDate.now();

                // 1. Total Expenses (This Month)
                List<Expense> monthlyExpenses = expenseRepository.findByDateBetween(startOfMonth, endOfMonth);
                BigDecimal totalExpense = monthlyExpenses.stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 2. Today's Spend
                // Only calculate if the requested month includes Today, or just query for
                // 'refDate' if intent is specific day?
                // Requirement says "Today's total". Usually implies "Real Today".
                List<Expense> todaysExpenses = expenseRepository.findByDate(today);
                BigDecimal todaysExpense = todaysExpenses.stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Monthly Budget (Sum of category budgets for this month)
                BigDecimal monthlyBudget = budgetService.getTotalMonthlyBudget(yearMonth.getMonthValue(),
                                yearMonth.getYear());

                // 4. Remaining
                BigDecimal remainingBudget = monthlyBudget.subtract(totalExpense);

                // 5. Chart Data
                Map<String, BigDecimal> categorySplit = new HashMap<>();
                expenseRepository.sumAmountByCategory(startOfMonth, endOfMonth)
                                .forEach(row -> categorySplit.put((String) row[0], (BigDecimal) row[1]));

                Map<String, BigDecimal> paymentModeSplit = new HashMap<>();
                expenseRepository.sumAmountByPaymentMode(startOfMonth, endOfMonth)
                                .forEach(row -> paymentModeSplit.put((String) row[0], (BigDecimal) row[1]));

                // Trend (TreeMap for sorting by date keys)
                Map<String, BigDecimal> dailyTrend = new TreeMap<>();
                expenseRepository.sumAmountByDate(startOfMonth, endOfMonth)
                                .forEach(row -> dailyTrend.put(row[0].toString(), (BigDecimal) row[1]));

                DashboardDataDTO data = DashboardDataDTO.builder()
                                .totalExpense(totalExpense)
                                .todaysExpense(todaysExpense)
                                .monthlyBudget(monthlyBudget)
                                .remainingBudget(remainingBudget)
                                .categorySplit(categorySplit)
                                .paymentModeSplit(paymentModeSplit)
                                .dailyTrend(dailyTrend)
                                .build();

                return ResponseEntity.ok(data);
        }
}
