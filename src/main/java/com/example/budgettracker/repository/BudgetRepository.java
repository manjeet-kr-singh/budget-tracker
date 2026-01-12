package com.example.budgettracker.repository;

import com.example.budgettracker.entity.Budget;
import com.example.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByCategory(String category);

    // Filter by User
    Optional<Budget> findByCategoryAndUser(String category, User user);

    List<Budget> findByMonthAndYearAndUser(Integer month, Integer year, User user);

    Optional<Budget> findByCategoryAndMonthAndYearAndUser(String category, Integer month, Integer year, User user);
}
