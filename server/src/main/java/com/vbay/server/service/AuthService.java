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
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;
import java.time.LocalDateTime;

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

    public LoginResponse login(LoginRequest request) throws SQLException {
        validateLoginRequest(request);
        String username = request.getUsername().trim();
        logInfo("LOGIN_ATTEMPT", "username=" + username);
        //Login as ADMIN
        if (username.equalsIgnoreCase("admin")) {
            String passwordStr = request.getPassword() != null ? new String(request.getPassword()) : "";
            if (!passwordStr.equals("admin")) {
                logError("LOGIN_FAILED", "username=" + username + ", reason: invalid_password");
                throw new AuthenticationException("Invalid username or password");
            }
            try (Connection connection = connectionProvider.getConnection()) {
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                Optional<User> userOptional = userRepository.findByUsername("admin");
                User adminUser;
                if (userOptional.isEmpty()) {
                    adminUser = new User(
                        "admin",
                        "admin@vbay.com",
                        passwordHasher.hash("admin".toCharArray()),
                        "0397085350",
                        Position.ADMIN,
                        UserStatus.ACTIVE,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        null,
                        java.time.LocalDate.now().toString()
                    );
                    userRepository.save(adminUser);
                    adminUser = userRepository.findByUsername("admin")
                        .orElseThrow(() -> new IllegalStateException("Failed to retrieve auto-provisioned admin user"));
                    logInfo("ADMIN_PROVISIONED", "Admin user auto-created in database.");
                } else {
                    adminUser = userOptional.get();
                    boolean updated = false;
                    if (adminUser.getPosition() != Position.ADMIN) {
                        try (java.sql.PreparedStatement stmt = connection.prepareStatement("UPDATE users SET position = ? WHERE id = ?")) {
                            stmt.setString(1, Position.ADMIN.name());
                            stmt.setLong(2, adminUser.getId());
                            stmt.executeUpdate();
                        }
                        adminUser.setPosition(Position.ADMIN);
                        updated = true;
                    }
                    if (adminUser.getUserStatus() != UserStatus.ACTIVE) {
                        userRepository.updateStatus(adminUser.getId(), UserStatus.ACTIVE);
                        adminUser.setStatus(UserStatus.ACTIVE);
                        updated = true;
                    }
                    if (updated) {
                        logInfo("ADMIN_RESTORED", "Admin user status/position corrected in database.");
                    }
                }

                logInfo("LOGIN_SUCCESS", "userId=" + adminUser.getId() + ", username=" + adminUser.getUserName());
                return new LoginResponse(
                    adminUser.getId(),
                    adminUser.getUserName(),
                    adminUser.getEmail(),
                    adminUser.getPosition(),
                    adminUser.getAvailableBalance(),
                    adminUser.getHoldBalance(),
                    adminUser.getWarningCount(),
                    adminUser.getLockUntil() != null ? adminUser.getLockUntil().toString() : null
                );
            } finally {
                if (request != null && request.getPassword() != null) {
                    Arrays.fill(request.getPassword(), '\0');
                }
            }
        }

        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                if (userRepository.existsBannedUserByUsername(username)) {
                    logError("LOGIN_FAILED", "username=" + username + ", reason: user_permanently_banned");
                    throw new AuthenticationException("Login failed: you are banned from the server");
                }
                logError("LOGIN_FAILED", "username=" + username + ", reason: user_not_found");
                throw new AuthenticationException("Invalid username or password");
            }
            User user = userOptional.get();
            if (user.isBanned()) {
                if (user.getUserStatus() == UserStatus.BANNED) {
                    if (user.getLockUntil() != null && LocalDateTime.now().isAfter(user.getLockUntil())) {
                        userRepository.updateStatus(user.getId(), UserStatus.ACTIVE);
                        userRepository.setLockUntil(user.getId(), null);
                        user.setStatus(UserStatus.ACTIVE);
                        user.setLockUntil(null);
                    } else {
                        String durationStr = "indefinitely";
                        if (user.getLockUntil() != null) {
                            durationStr = formatRemainingDuration(LocalDateTime.now(), user.getLockUntil());
                        }
                        logError("LOGIN_FAILED", "username=" + username + ", reason: user_banned_until_" + user.getLockUntil());
                        throw new AuthenticationException("Log in failed : You have been banned for " + durationStr);
                    }
                } else {
                    String reasonStr = "No reason provided.";
                    Optional<String> banReason = userRepository.findLatestBanReason(user.getId());
                    if (banReason.isPresent() && !banReason.get().isBlank()) {
                        reasonStr = banReason.get();
                    }
                    logError("LOGIN_FAILED", "username=" + username + ", reason: " + reasonStr);
                    
                    String message;
                    if (user.getUserStatus() == com.vbay.shared.enums.auth.UserStatus.DELETED) {
                        message = "Your account has been permanently banned and deleted.\nReason: " + reasonStr;
                    } else {
                        message = "Your account has been banned.\nReason: " + reasonStr;
                    }
                    throw new AuthenticationException(message);
                }
            }


            if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
                logError("LOGIN_FAILED", "username=" + username + ", reason: invalid_password");
                throw new AuthenticationException("Invalid username or password");
            }
            logInfo("LOGIN_SUCCESS", "userId=" + user.getId() + ", username=" + user.getUserName());
            return new LoginResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPosition(),
                user.getAvailableBalance(),
                user.getHoldBalance(),
                user.getWarningCount(),
                user.getLockUntil() != null ? user.getLockUntil().toString() : null
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

        if (username.equalsIgnoreCase("admin")) {
            throw new ValidationException("Username 'admin' is reserved.");
        }

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

    public Respond<Void> handleRegister(String requestId, JsonElement payload) throws SQLException {
        RegisterRequest registerRequest = JsonUtils.fromJson(payload, RegisterRequest.class);
        if (registerRequest == null) {
            return new Respond<>(requestId, false, "Invalid register request", null);
        }
        register(registerRequest);
        return new Respond<>(requestId, true, "Register successful", null);
    }

    private String formatRemainingDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isAfter(end)) {
            return "0 seconds";
        }
        
        LocalDateTime temp = LocalDateTime.from(start);

        long years = temp.until(end, java.time.temporal.ChronoUnit.YEARS);
        temp = temp.plusYears(years);

        long days = temp.until(end, java.time.temporal.ChronoUnit.DAYS);
        temp = temp.plusDays(days);

        long hours = temp.until(end, java.time.temporal.ChronoUnit.HOURS);
        temp = temp.plusHours(hours);

        long minutes = temp.until(end, java.time.temporal.ChronoUnit.MINUTES);
        temp = temp.plusMinutes(minutes);

        long seconds = temp.until(end, java.time.temporal.ChronoUnit.SECONDS);

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append(" year").append(years > 1 ? "s" : "").append(" ");
        if (days > 0) sb.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        if (hours > 0) sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        if (minutes > 0) sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append(" second").append(seconds != 1 ? "s" : "");
        
        return sb.toString().trim();
    }
}
