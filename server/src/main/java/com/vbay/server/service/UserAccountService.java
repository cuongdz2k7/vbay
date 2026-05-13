package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.dto.userDTO.DepositBalanceRequest;

public class UserAccountService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;

    public UserAccountService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            DomainEventPublisher domainEventPublisher) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public UserBalanceResult depositBalance(DepositBalanceRequest request, ClientSession session) throws SQLException {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to deposit balance");
        }
        validateDepositRequest(request);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                userRepository.depositAvailableBalance(session.getUserId(), request.getAmount());
                User user = userRepository.findById(session.getUserId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );
                UserBalanceResult result = ResultMapper.toUserBalanceResult(
                    user,
                    request.getAmount(),
                    "DEPOSIT",
                    LocalDateTime.now()
                );
                connection.commit();

                domainEventPublisher.publish(new UserBalanceUpdatedDomainEvent(result, result.getUpdatedAt()));
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void validateDepositRequest(DepositBalanceRequest request) {
        if (request == null) {
            throw new ValidationException("Deposit request is required");
        }
        if (request.getAmount() == null) {
            throw new ValidationException("Deposit amount is required");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deposit amount must be greater than 0");
        }
    }
}
