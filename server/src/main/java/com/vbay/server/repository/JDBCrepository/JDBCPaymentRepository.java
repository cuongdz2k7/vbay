package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.vbay.server.model.Payment;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.shared.enums.payment.PaymentStatus;

public class JDBCPaymentRepository implements PaymentRepository {
    private final Connection connection;

    public JDBCPaymentRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Payment save(Payment payment) throws SQLException {
        String sql = """
            INSERT INTO payments (
                auction_id, buyer_id, seller_id, winning_bid_id, amount, type, status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, payment.getAuctionId());
            statement.setLong(2, payment.getBuyerId());
            statement.setLong(3, payment.getSellerId());
            statement.setLong(4, payment.getWinningBidId());
            statement.setBigDecimal(5, payment.getAmount());
            statement.setString(6, payment.getType().name());
            statement.setString(7, payment.getStatus().name());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    payment.setId(rs.getLong(1));
                    return payment;
                }
            }
        }

        throw new SQLException("Creating payment failed, no ID obtained.");
    }

    @Override
    public void updateStatus(long paymentId, PaymentStatus newStatus) throws SQLException {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.name());
            statement.setLong(2, paymentId);
            statement.executeUpdate();
        }
    }
}
