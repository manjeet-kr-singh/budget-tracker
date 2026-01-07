package com.example.budgettracker.repository;

import com.example.budgettracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    List<Expense> findByDescriptionContainingIgnoreCase(String keyword);

    // For Dashboard
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByDate(LocalDate date);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate GROUP BY e.category")
    List<Object[]> sumAmountByCategory(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.paymentMode, SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate GROUP BY e.paymentMode")
    List<Object[]> sumAmountByPaymentMode(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.date, SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate GROUP BY e.date ORDER BY e.date ASC")
    List<Object[]> sumAmountByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e WHERE YEAR(e.date) = :year GROUP BY MONTH(e.date)")
    List<Object[]> sumAmountByMonth(@Param("year") int year);
}
