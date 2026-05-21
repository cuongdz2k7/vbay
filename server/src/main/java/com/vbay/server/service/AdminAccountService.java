package com.vbay.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.repository.AdminAccountRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.security.PasswordHasher;
import com.vbay.shared.Utils.LoggingUtils;

public class AdminAccountService {
    private static final Logger LOGGER = LoggingUtils.getLogger(AdminAccountService.class);
    private static final List<AdminSeed> DEFAULT_ADMIN_SEEDS = List.of(
        new AdminSeed("admin", "admin@vbay.local", "admin123")
    );

    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final PasswordHasher passwordHasher;

    public AdminAccountService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.passwordHasher = passwordHasher;
    }

    public void seedDefaultAdmins() {
        try (Connection connection = connectionProvider.getConnection()) {
            AdminAccountRepository adminAccountRepository = repositoryFactory.createAdminAccountRepository(connection);
            for (AdminSeed adminSeed : DEFAULT_ADMIN_SEEDS) {
                seedAdmin(adminAccountRepository, adminSeed);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to seed admin users: " + exception.getMessage(), exception);
        }
    }

    private void seedAdmin(AdminAccountRepository adminAccountRepository, AdminSeed adminSeed) throws SQLException {
        char[] password = adminSeed.password().toCharArray();
        try {
            String passwordHash = passwordHasher.hash(password);
            if (adminAccountRepository.existsByUsernameOrEmail(adminSeed.username(), adminSeed.email())) {
                adminAccountRepository.updateAdminAccount(adminSeed.username(), adminSeed.email(), passwordHash);
                LOGGER.info(() -> "Updated seeded admin account username=" + adminSeed.username());
            } else {
                adminAccountRepository.insertAdminAccount(adminSeed.username(), adminSeed.email(), passwordHash);
                LOGGER.info(() -> "Seeded admin account username=" + adminSeed.username());
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private record AdminSeed(String username, String email, String password) {
    }
}
