package com.example.budgettracker.repository;

import com.example.budgettracker.entity.Expense;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class ExpenseSpecification {

    public static Specification<Expense> filterExpenses(String keyword, String category, String paymentMode,
            LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern));
            }

            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (StringUtils.hasText(paymentMode)) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMode"), paymentMode));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
