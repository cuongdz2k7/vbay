package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.model.User;

public interface UserRepository { 
    Optional<User> findByUsername(String username) throws SQLException; 
    Optional<User> findByEmail(String email) throws SQLException;
    Optional<User> findById(long id) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    User save(User user) throws SQLException;
    Optional<User> lockUserForUpdate(long userId) throws SQLException;
    void releaseHoldBalance(long userId, BigDecimal amount) throws SQLException;
    void holdBalance(long userId, BigDecimal amount) throws SQLException;
    void decreaseAvailableBalance(long userId, BigDecimal amount) throws SQLException;
}
