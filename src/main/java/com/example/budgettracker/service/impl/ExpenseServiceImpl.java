package com.example.budgettracker.service.impl;

import com.example.budgettracker.dto.ExpenseDTO;
import com.example.budgettracker.entity.Expense;
import com.example.budgettracker.repository.ExpenseRepository;
import com.example.budgettracker.repository.ExpenseSpecification;
import com.example.budgettracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public void saveExpense(ExpenseDTO expenseDTO) {
        Expense expense = mapToEntity(expenseDTO);
        expenseRepository.save(expense);
    }

    @Override
    public void saveAll(List<Expense> expenses) {
        expenseRepository.saveAll(expenses);
    }

    @Override
    public List<ExpenseDTO> getAllExpenses(String keyword) {
        if (keyword != null) {
            return expenseRepository.findByDescriptionContainingIgnoreCase(keyword).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }
        return expenseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ExpenseDTO> getExpensesPaginated(String keyword, String category, String paymentMode,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Expense> spec = ExpenseSpecification.filterExpenses(keyword, category, paymentMode, startDate,
                endDate);
        return expenseRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        return mapToDTO(expense);
    }

    @Override
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setDate(expenseDTO.getDate());
        expense.setCategory(expenseDTO.getCategory());
        expense.setPaymentMode(expenseDTO.getPaymentMode());
        expense.setUpdatedAt(java.time.LocalDateTime.now());

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToDTO(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    private Expense mapToEntity(ExpenseDTO expenseDTO) {
        Expense expense = new Expense();
        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setDate(expenseDTO.getDate());
        expense.setCategory(expenseDTO.getCategory());
        expense.setPaymentMode(expenseDTO.getPaymentMode());
        return expense;
    }

    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setId(expense.getId());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setDate(expense.getDate());
        expenseDTO.setCategory(expense.getCategory());
        expenseDTO.setPaymentMode(expense.getPaymentMode());
        return expenseDTO;
    }
}
