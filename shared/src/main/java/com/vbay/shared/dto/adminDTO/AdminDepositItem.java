package com.vbay.shared.dto.adminDTO;

import com.vbay.shared.enums.payment.DepositRequestStatus;
import java.math.BigDecimal;

public class AdminDepositItem {
    private long depositId;
    private long userId;
    private String username;
    private BigDecimal amount;
    private DepositRequestStatus status;
    private String createdAt;

    public AdminDepositItem() {}

    public AdminDepositItem(long depositId, long userId, String username, BigDecimal amount, DepositRequestStatus status, String createdAt) {
        this.depositId = depositId;
        this.userId = userId;
        this.username = username;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getDepositId() { return depositId; }
    public void setDepositId(long depositId) { this.depositId = depositId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public DepositRequestStatus getStatus() { return status; }
    public void setStatus(DepositRequestStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
