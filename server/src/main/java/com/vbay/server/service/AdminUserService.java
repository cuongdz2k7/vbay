package com.vbay.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.shared.dto.adminDTO.AdminUserDTO;
import com.vbay.shared.dto.adminDTO.AdminUserListResponse;
import com.vbay.shared.dto.adminDTO.BanUserRequest;
import com.vbay.shared.dto.adminDTO.UnbanUserRequest;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class AdminUserService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;

    public AdminUserService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
    }

    public AdminUserListResponse getUsers(ClientSession session) throws SQLException {
        requireAdmin(session);

        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            List<AdminUserDTO> users = userRepository.findAllUsers().stream()
                .map(this::toAdminUserDTO)
                .toList();
            return new AdminUserListResponse(users);
        }
    }

    public AdminUserDTO banUser(BanUserRequest request, ClientSession session) throws SQLException {
        requireAdmin(session);
        validateTargetUserId(request == null ? 0 : request.getUserId());

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                User user = userRepository.lockUserForUpdate(request.getUserId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );
                validateBanTarget(user, session);
                userRepository.updateStatus(user.getId(), UserStatus.BANNED);
                User updatedUser = userRepository.findById(user.getId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );
                connection.commit();
                return toAdminUserDTO(updatedUser);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public AdminUserDTO unbanUser(UnbanUserRequest request, ClientSession session) throws SQLException {
        requireAdmin(session);
        validateTargetUserId(request == null ? 0 : request.getUserId());

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                User user = userRepository.lockUserForUpdate(request.getUserId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );
                validateUnbanTarget(user, session);
                userRepository.updateStatus(user.getId(), UserStatus.ACTIVE);
                User updatedUser = userRepository.findById(user.getId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );
                connection.commit();
                return toAdminUserDTO(updatedUser);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void requireAdmin(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("Admin login required");
        }
        if (session.getPosition() != Position.ADMIN) {
            throw new AuthenticationException("Admin permission required");
        }
    }

    private void validateTargetUserId(long userId) {
        if (userId <= 0) {
            throw new ValidationException("User id is required");
        }
    }

    private void validateBanTarget(User user, ClientSession session) {
        if (user.getId() == session.getUserId()) {
            throw new ValidationException("Admin cannot ban their own account");
        }
        if (user.getPosition() == Position.ADMIN) {
            throw new ValidationException("Admin account cannot be banned");
        }
        if (user.getUserStatus() == UserStatus.DELETED) {
            throw new ValidationException("Deleted user cannot be banned");
        }
        if (user.getUserStatus() == UserStatus.BANNED) {
            throw new ValidationException("User is already banned");
        }
    }

    private void validateUnbanTarget(User user, ClientSession session) {
        if (user.getId() == session.getUserId()) {
            throw new ValidationException("Admin cannot update their own account status");
        }
        if (user.getPosition() == Position.ADMIN) {
            throw new ValidationException("Admin account status cannot be changed here");
        }
        if (user.getUserStatus() == UserStatus.DELETED) {
            throw new ValidationException("Deleted user cannot be unbanned");
        }
        if (user.getUserStatus() != UserStatus.BANNED) {
            throw new ValidationException("Only banned users can be unbanned");
        }
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUserName(),
            user.getPosition(),
            user.getUserStatus(),
            user.getTimeinit()
        );
    }
}
