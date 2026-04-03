package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.Model.User;

public interface UserRepository {
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findByEmail(String email) throws SQLException;
    Optional<User> findByPhoneNumber(String phoneNumber) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    void save(User user) throws SQLException;
}
