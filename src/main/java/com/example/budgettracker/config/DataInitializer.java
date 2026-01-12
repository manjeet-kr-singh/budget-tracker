package com.example.budgettracker.config;

import com.example.budgettracker.entity.User;
import com.example.budgettracker.enums.Role;
import com.example.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: admin/admin");
        }
    }
}
