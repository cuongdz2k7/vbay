package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.User;
import com.vbay.shared.enums.auth.UserStatus;

public interface UserRepository { 
    Optional<User> findByUsername(String username) throws SQLException; 
    Optional<User> findByEmail(String email) throws SQLException;
    Optional<User> findById(long id) throws SQLException;
    List<User> findAllUsers() throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    User save(User user) throws SQLException;
    void updateStatus(long userId, UserStatus status) throws SQLException;
    Optional<User> lockUserForUpdate(long userId) throws SQLException;
    long releaseHoldBalance(long userId, BigDecimal amount) throws SQLException;
    long holdBalance(long userId, BigDecimal amount) throws SQLException;
    long decreaseAvailableBalance(long userId, BigDecimal amount) throws SQLException;
    long depositAvailableBalance(long userId, BigDecimal amount) throws SQLException;
}
