package com.vbay.server.service;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.User;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.validation.ValidationUtils;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.dto.authDTO.RegisterRequest;

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    ///add object to check wheather the passwork is weak/strong : PasswordPolicy
    
    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    private boolean isUniqueConstraintViolation(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        String lower = message.toLowerCase();
        return lower.contains("unique constraint failed")
            || lower.contains("sqlite_constraint_unique");
    }

    private void validateLoginRequest (LoginRequest request) {
        if (request == null) {
            throw new ValidationException("Login request is required");
        }
        ValidationUtils.requireNotBlank(request.getEmail(), "Email is required");
        ValidationUtils.requireNotBlank(request.getPassword(), "Password is required");
    }

    public LoginResponse login (LoginRequest request) throws SQLException, AuthenticationException {
        validateLoginRequest(request);

        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail().trim());
            if (userOptional.isEmpty()) { 
                throw new AuthenticationException("Invalid email or password");
            }
            User user = userOptional.get(); 
            if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password");
            }
            return new LoginResponse(user.getId(),
                                    user.getUserName(),
                                    user.getEmail(),
                                    user.getPosition());
        }
        finally {
            ///xóa pass trong request để tránh leak, bị attacker dump từ RAM
            if (request != null && request.getPassword() != null) {
                Arrays.fill(request.getPassword(), '\0');
            }
        }
    }

    private void validateRegisterRequest (RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Register request is required");
        }
        ValidationUtils.requireNotBlank(request.getUsername(), "Username is required");
        ValidationUtils.requireNotBlank(request.getEmail(), "Email is required");
        ValidationUtils.requireNotBlank(request.getPassword(), "Password is required");
    }
    public void register(RegisterRequest request) throws SQLException {
        validateRegisterRequest(request);

        try {
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                throw new ValidationException("Username already exists");
            }

            if (userRepository.existsByEmail(request.getEmail().trim())) {
                throw new ValidationException("Email already exists");
            }

            User newUser = new User(
                request.getUsername().trim(),
                request.getEmail().trim(),
                passwordHasher.hash(request.getPassword()),
                request.getPhoneNumber(),
                BigDecimal.ZERO
            );
            userRepository.save(newUser);
        } catch (SQLException e) {
                if (isUniqueConstraintViolation(e)) {
                    throw new ValidationException("Username or email already exists");
                }
                throw e;
        } finally {
            if (request != null && request.getPassword() != null) {
                Arrays.fill(request.getPassword(), '\0');
            }
        }
    }




    
}
