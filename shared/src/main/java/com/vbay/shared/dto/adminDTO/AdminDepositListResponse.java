package com.vbay.shared.dto.adminDTO;

import java.util.List;

public class AdminDepositListResponse {
    private List<AdminDepositItem> deposits;

    public AdminDepositListResponse() {}

    public AdminDepositListResponse(List<AdminDepositItem> deposits) {
        this.deposits = deposits;
    }

    public List<AdminDepositItem> getDeposits() { return deposits; }
    public void setDeposits(List<AdminDepositItem> deposits) { this.deposits = deposits; }
}
