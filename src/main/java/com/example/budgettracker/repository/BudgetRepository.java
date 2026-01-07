package com.example.budgettracker.repository;

import com.example.budgettracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByCategory(String category);

    List<Budget> findByMonthAndYear(Integer month, Integer year);

    Optional<Budget> findByCategoryAndMonthAndYear(String category, Integer month, Integer year);
}
