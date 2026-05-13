package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.validation.ValidationUtils;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.dto.authDTO.RegisterRequest;
import com.vbay.shared.protocol.Respond;

public class AuthService {
    private static final Logger LOGGER = LoggingUtils.getLogger(AuthService.class);
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final PasswordHasher passwordHasher;

    public AuthService(ConnectionProvider connectionProvider, RepositoryFactory repositoryFactory, PasswordHasher passwordHasher) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.passwordHasher = passwordHasher;
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new ValidationException("Login request is required");
        }
        ValidationUtils.requireNotBlank(request.getUsername(), "Username is required");
        ValidationUtils.requireNotBlank(request.getPassword(), "Password is required");
    }

    private static void logInfo(String action, String detail) {
        LOGGER.info(() -> "[AUTH][" + action + "] " + detail);
    }

    private static void logError(String action, String detail) {
        LOGGER.warning(() -> "[AUTH][" + action + "] " + detail);
    }

    public Respond<LoginResponse> handleLogin(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        if (session == null) {
            throw new ValidationException("Client session is required");
        }
        if (session.isAuthenticated()) {
            throw new AuthenticationException("Client is already logged in");
        }

        LoginRequest request = JsonUtils.fromJson(payload, LoginRequest.class);
        if (request == null) {
            throw new ValidationException("Invalid login payload");
        }

        LoginResponse response = login(request);
        session.setSession(response.getUserId(), response.getUsername(), response.getPosition());
        return new Respond<>(requestId, true, "Login successful", response);
    }

    public Respond<Void> handleLogout(String requestId, ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User is not logged in");
        }

        session.clearSession();
        return new Respond<>(requestId, true, "Logout successful", null);
    }

    public Respond<Void> handleRegister(String requestId, JsonElement payload) throws SQLException {
        RegisterRequest request = JsonUtils.fromJson(payload, RegisterRequest.class);
        if (request == null) {
            throw new ValidationException("Invalid register payload");
        }

        register(request);
        return new Respond<>(requestId, true, "Registration successful", null);
    }

    public LoginResponse login(LoginRequest request) throws SQLException {
        validateLoginRequest(request);
        String email = request.getEmail().trim();
        logInfo("LOGIN_ATTEMPT", "email=" + email);

        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                logError("LOGIN_FAILED", "email=" + email + ", reason: user_not_found");
                throw new AuthenticationException("Invalid email or password");
            }
            User user = userOptional.get();
            if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
                logError("LOGIN_FAILED", "email=" + email + ", reason: invalid_password");
                throw new AuthenticationException("Invalid email or password");
            }
            logInfo("LOGIN_SUCCESS", "userId=" + user.getId() + ", email=" + user.getEmail());
            return new LoginResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPosition(),
                user.getAvailableBalance(),
                user.getHoldBalance()
            );
        } finally {
            if (request != null && request.getPassword() != null) {
                Arrays.fill(request.getPassword(), '\0');
            }
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Register request is required");
        }
        ValidationUtils.requireNotBlank(request.getUsername(), "Username is required");
        ValidationUtils.requireNotBlank(request.getEmail(), "Email is required");
        ValidationUtils.requireNotBlank(request.getPassword(), "Password is required");
    }

    public void register(RegisterRequest request) throws SQLException {
        validateRegisterRequest(request);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        logInfo("REGISTER_ATTEMPT", "username=" + username + ", email=" + email);

        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
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
