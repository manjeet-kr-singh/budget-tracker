package com.example.budgettracker.controller;

import com.example.budgettracker.dto.BudgetStatusDTO;
import com.example.budgettracker.entity.Budget;
import com.example.budgettracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/budgets")
    public String showBudgetsPage(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model) {

        if (month == null || year == null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        java.time.LocalDate date = java.time.LocalDate.of(year, month, 1);
        List<BudgetStatusDTO> budgetStatuses = budgetService.getBudgetStatus(date);

        // Prepare the Planning DTO
        com.example.budgettracker.dto.MonthlyBudgetDTO monthlyPlan = budgetService.getMonthlyBudgetPlan(month, year);

        model.addAttribute("budgetStatuses", budgetStatuses);
        model.addAttribute("monthlyPlan", monthlyPlan);
        model.addAttribute("selectedMonth", String.format("%d-%02d", year, month)); // YYYY-MM
        model.addAttribute("activePage", "budgets");
        return "budgets";
    }

    @PostMapping("/saveMonthlyBudget")
    public String saveMonthlyBudget(
            @ModelAttribute("monthlyPlan") com.example.budgettracker.dto.MonthlyBudgetDTO monthlyPlan) {
        budgetService.saveMonthlyBudgetPlan(monthlyPlan);
        return "redirect:/budgets?month=" + monthlyPlan.getMonth() + "&year=" + monthlyPlan.getYear();
    }

    @PostMapping("/budgets/delete/{id}")
    public String deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return "redirect:/budgets";
    }
}
