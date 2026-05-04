package com.vbay.server.repository;

import java.sql.SQLException;

import com.vbay.server.model.Payment;
import com.vbay.shared.status.shared_status.PaymentStatus;

public interface PaymentRepository {
    Payment save(Payment payment) throws SQLException;

    void updateStatus(long paymentId, PaymentStatus newStatus) throws SQLException;
}
