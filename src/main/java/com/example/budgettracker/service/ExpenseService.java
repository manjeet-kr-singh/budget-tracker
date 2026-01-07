package com.example.budgettracker.service;

import com.example.budgettracker.dto.ExpenseDTO;
import com.example.budgettracker.entity.Expense;
import java.util.List;

public interface ExpenseService {
    void saveExpense(ExpenseDTO expenseDTO);

    void saveAll(List<Expense> expenses);

    List<ExpenseDTO> getAllExpenses(String keyword);

    ExpenseDTO getExpenseById(Long id);

    ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO);

    org.springframework.data.domain.Page<ExpenseDTO> getExpensesPaginated(String keyword,
            String category,
            String paymentMode,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            org.springframework.data.domain.Pageable pageable);

    void deleteExpense(Long id);
}
