package com.example.budgettracker.repository;

import com.example.budgettracker.entity.Expense;
import com.example.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    List<Expense> findByDescriptionContainingIgnoreCaseAndUser(String keyword, User user);

    // For Dashboard
    List<Expense> findByDateBetweenAndUser(LocalDate startDate, LocalDate endDate, User user);

    List<Expense> findByDateAndUser(LocalDate date, User user);

    List<Expense> findByUser(User user);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate GROUP BY e.category")
    List<Object[]> sumAmountByCategory(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT e.paymentMode, SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate GROUP BY e.paymentMode")
    List<Object[]> sumAmountByPaymentMode(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT e.date, SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate GROUP BY e.date ORDER BY e.date ASC")
    List<Object[]> sumAmountByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year GROUP BY MONTH(e.date)")
    List<Object[]> sumAmountByMonth(@Param("year") int year, @Param("user") User user);
}
