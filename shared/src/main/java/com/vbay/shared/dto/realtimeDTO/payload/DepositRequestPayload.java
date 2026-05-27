package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;

public class DepositRequestPayload {
    private long depositId;
    private long userId;
    private String username;
    private BigDecimal amount;
    private String status;
    private String message;

    public DepositRequestPayload() {}

    public DepositRequestPayload(long depositId, long userId, String username, BigDecimal amount, String status, String message) {
        this.depositId = depositId;
        this.userId = userId;
        this.username = username;
        this.amount = amount;
        this.status = status;
        this.message = message;
    }

    public long getDepositId() { return depositId; }
    public void setDepositId(long depositId) { this.depositId = depositId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
