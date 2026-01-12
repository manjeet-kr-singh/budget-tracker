package com.example.budgettracker.controller;

import com.example.budgettracker.dto.ReportDataDTO;
import com.example.budgettracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ExpenseRepository expenseRepository;
    private final com.example.budgettracker.repository.UserRepository userRepository;

    private com.example.budgettracker.entity.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/reports") // This overrides the one in ExpenseController if priority matches, but better
                            // to remove it from there later.
    public String reports(Model model) {
        model.addAttribute("activePage", "reports");
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "reports";
    }

    @GetMapping("/api/reports/data")
    @ResponseBody
    public ResponseEntity<ReportDataDTO> getReportData(@RequestParam int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        com.example.budgettracker.entity.User user = getCurrentUser();

        // 1. Monthly Trend
        Map<String, BigDecimal> monthlyTrend = new LinkedHashMap<>();
        // Initialize all months to 0
        for (Month month : Month.values()) {
            monthlyTrend.put(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), BigDecimal.ZERO);
        }

        List<Object[]> monthData = expenseRepository.sumAmountByMonth(year, user);
        for (Object[] row : monthData) {
            int monthIndex = (int) row[0]; // 1-12
            BigDecimal amount = (BigDecimal) row[1];
            String monthName = Month.of(monthIndex).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyTrend.put(monthName, amount);
        }

        // 2. Category Split (Reuse existing query with yearly range)
        Map<String, BigDecimal> categorySplit = new LinkedHashMap<>();
        expenseRepository.sumAmountByCategory(startOfYear, endOfYear, user)
                .forEach(row -> categorySplit.put((String) row[0], (BigDecimal) row[1]));

        // 3. Payment Mode Split
        Map<String, BigDecimal> paymentModeSplit = new LinkedHashMap<>();
        expenseRepository.sumAmountByPaymentMode(startOfYear, endOfYear, user)
                .forEach(row -> paymentModeSplit.put((String) row[0], (BigDecimal) row[1]));

        // 4. Totals
        BigDecimal totalSpentYear = monthlyTrend.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageMonthlySpend = totalSpentYear.divide(BigDecimal.valueOf(12), 2,
                java.math.RoundingMode.HALF_UP);

        ReportDataDTO data = ReportDataDTO.builder()
                .monthlyTrend(monthlyTrend)
                .categorySplit(categorySplit)
                .paymentModeSplit(paymentModeSplit)
                .totalSpentYear(totalSpentYear)
                .averageMonthlySpend(averageMonthlySpend)
                .build();

        return ResponseEntity.ok(data);
    }
}
