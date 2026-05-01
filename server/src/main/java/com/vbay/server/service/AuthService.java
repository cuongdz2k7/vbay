package com.vbay.server.service;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import com.vbay.server.model.User;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
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
   /*
    ValidationException là unchecked exception, có thể throw ở bất cứ đâu trong code mà không cần khai báo throws. 
    Nó sẽ bubble lên cho đến khi được catch hoặc crash server nếu không được catch.
   */
    private void validateLoginRequest (LoginRequest request) {
        if (request == null) {
            throw new ValidationException("Login request is required");
        }
        ValidationUtils.requireNotBlank(request.getEmail(), "Email is required");
        ValidationUtils.requireNotBlank(request.getPassword(), "Password is required");
    }
    //
    private static void logInfo(String action, String detail) {
        System.out.println("\n[AUTH][" + action + "] " + detail);
    }

    private static void logError(String action, String detail) {
        System.err.println("\n[AUTH][" + action + "] " + detail);
    }

    public LoginResponse login (LoginRequest request) throws SQLException {
        validateLoginRequest(request);
        String email = request.getEmail().trim();
        logInfo("LOGIN_ATTEMPT", "email=" + email);

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) { 
                logError("LOGIN_FAILED", "email=" + email + ", reason: user_not_found");
                throw new AuthenticationException("Invalid email or password");
            }
            User user = userOptional.get(); 
            if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
                logError("LOGIN_FAILED", "email=" + email + ", reason: invalid_password");
                throw new AuthenticationException("Invalid username or password");
            }
            logInfo("LOGIN_SUCCESS", "userId=" + user.getId() + ", email=" + user.getEmail());
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
        ///có thể thêm validate password mạnh yếu ở đây sau khi có PasswordPolicy
    }

    public void register(RegisterRequest request) throws SQLException {
        validateRegisterRequest(request);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        logInfo("REGISTER_ATTEMPT", "username=" + username + ", email=" + email);

        try {
            User newUser = new User(
                username,
                email,
                passwordHasher.hash(request.getPassword()),
                request.getPhoneNumber(),
                BigDecimal.ZERO
            );
            userRepository.save(newUser);
            logInfo("REGISTER_SUCCESS", "username=" + username + ", email=" + email);
        } finally {
            if (request != null && request.getPassword() != null) {
                Arrays.fill(request.getPassword(), '\0');
            }
        }
    }




    
}
