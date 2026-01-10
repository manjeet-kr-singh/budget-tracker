package com.example.budgettracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // "GLOBAL" for total monthly budget, or specific category name

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "budget_month", nullable = false)
    private Integer month; // 1-12

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    // We can add month/year columns later for historical budgets,
    // for now we assume this applies to current/all months to get started quickly.
}
