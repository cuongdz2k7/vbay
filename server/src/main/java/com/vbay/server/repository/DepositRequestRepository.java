package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import com.vbay.server.model.DepositRequestRow;

public interface DepositRequestRepository {
    DepositRequestRow save(DepositRequestRow row) throws SQLException;
    List<DepositRequestRow> findAllPending() throws SQLException;
    Optional<DepositRequestRow> findById(long depositId) throws SQLException;
    void updateStatus(long depositId, String status, Long adminId) throws SQLException;
}
