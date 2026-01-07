package com.example.budgettracker.enums;

public enum Category {
    FOOD("Food & Dining"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health & Fitness"),
    SHOPPING("Shopping"),
    HOUSING("Housing"),
    EDUCATION("Education"),
    PERSONAL("Personal Care"),
    TRAVEL("Travel"),
    OTHERS("Others");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
