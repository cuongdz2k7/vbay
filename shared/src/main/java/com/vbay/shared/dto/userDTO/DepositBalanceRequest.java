package com.vbay.shared.dto.userDTO;

import java.math.BigDecimal;

public class DepositBalanceRequest {
    private BigDecimal amount;

    public DepositBalanceRequest() {
    }

    public DepositBalanceRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
