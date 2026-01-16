package com.example.budgettracker.service;

import com.example.budgettracker.entity.User;

public interface UserService {
    // Other user methods can be added here
    void updatePreferences(String username, String theme, String currency);

    User getUserByUsername(String username);
}
