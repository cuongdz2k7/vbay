package com.vbay.server.repository;

import java.sql.SQLException;

public interface AdminAccountRepository {
    boolean existsByUsernameOrEmail(String username, String email) throws SQLException;
    void insertAdminAccount(String username, String email, String passwordHash) throws SQLException;
    void updateAdminAccount(String username, String email, String passwordHash) throws SQLException;
}
