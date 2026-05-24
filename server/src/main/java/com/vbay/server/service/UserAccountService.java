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
import com.google.gson.JsonElement;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.userDTO.DepositBalanceRequest;
import com.vbay.shared.dto.userDTO.UserBalanceResponse;
import com.vbay.shared.protocol.Respond;

public class UserAccountService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;
    private final com.vbay.server.realtime.transport.RealtimeBroadcaster realtimeBroadcaster;

    public UserAccountService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            DomainEventPublisher domainEventPublisher) {
        this(connectionProvider, repositoryFactory, domainEventPublisher, null);
    }

    public UserAccountService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            DomainEventPublisher domainEventPublisher,
            com.vbay.server.realtime.transport.RealtimeBroadcaster realtimeBroadcaster) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
        this.realtimeBroadcaster = realtimeBroadcaster;
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
                com.vbay.server.repository.DepositRequestRepository depositRepo = repositoryFactory.createDepositRequestRepository(connection);
                
                User user = userRepository.findById(session.getUserId()).orElseThrow(
                    () -> new ValidationException("User not found")
                );

                // Create a PENDING deposit request in DB
                com.vbay.server.model.DepositRequestRow row = new com.vbay.server.model.DepositRequestRow(
                    0L,
                    user.getId(),
                    user.getUserName(),
                    request.getAmount(),
                    "PENDING",
                    null,
                    null,
                    null
                );
                row = depositRepo.save(row);
                
                connection.commit();

                // Broadcast ADMIN_DEPOSIT_REQUESTED event to the AUCTION_LIST lobby room so admins can receive it
                com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload adminPayload = new com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload(
                    row.getId(),
                    row.getUserId(),
                    row.getUsername(),
                    row.getAmount(),
                    "PENDING",
                    "New deposit request from " + row.getUsername()
                );
                
                com.vbay.shared.dto.realtimeDTO.Room adminRoom = new com.vbay.shared.dto.realtimeDTO.Room();
                adminRoom.setType(com.vbay.shared.enums.realtime.RoomType.AUCTION_LIST);
                adminRoom.setTargetId(0L);
                
                if (realtimeBroadcaster != null) {
                    realtimeBroadcaster.broadcast(new com.vbay.shared.protocol.RealtimeEvent<>(
                        com.vbay.shared.enums.realtime.RealtimeEventType.ADMIN_DEPOSIT_REQUESTED,
                        adminRoom,
                        adminPayload
                    ));
                }

                // Return current unchanged balance
                return ResultMapper.toUserBalanceResult(
                    user,
                    "DEPOSIT_PENDING",
                    LocalDateTime.now()
                );
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Respond<UserBalanceResponse> handleDepositBalance(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        DepositBalanceRequest request = JsonUtils.fromJson(payload, DepositBalanceRequest.class);
        if (request == null) {
            return new Respond<>(requestId, false, "Invalid deposit balance request", null);
        }
        UserBalanceResult result = depositBalance(request, session);
        return new Respond<>(requestId, true, "Balance deposited successfully", ResultMapper.toUserBalanceResponse(result));
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
