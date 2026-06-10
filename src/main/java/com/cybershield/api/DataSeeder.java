package com.cybershield.api;

import com.cybershield.api.model.User;
import com.cybershield.api.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "shahrear002@gmail.com";
        if (userRepository.findByUsername(adminEmail).isEmpty()) {
            log.info("Master admin account not found. Seeding admin user: {}", adminEmail);
            User admin = User.builder()
                    .username(adminEmail)
                    .password(passwordEncoder.encode("123456"))
                    .role(User.Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Master admin account successfully seeded.");
        } else {
            log.info("Master admin account already exists. Skipping seeding.");
        }
    }
}
