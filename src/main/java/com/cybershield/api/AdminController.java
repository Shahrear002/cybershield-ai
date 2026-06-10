package com.cybershield.api;

import com.cybershield.api.model.User;
import com.cybershield.api.model.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record UserCreationRequest(
            @NotBlank @Email String username,
            @NotBlank String password,
            @NotBlank String role
    ) {}

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationRequest request) {
        log.info("Admin attempting to provision new user: {}", request.username());

        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"User already exists\"}");
        }

        User.Role mappedRole;
        try {
            mappedRole = User.Role.valueOf(request.role());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid role\"}");
        }

        User newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(mappedRole)
                .build();

        userRepository.save(newUser);
        log.info("Successfully provisioned user: {} with role: {}", request.username(), request.role());

        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"User successfully created\"}");
    }
}
