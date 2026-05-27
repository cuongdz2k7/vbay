package com.vbay.shared.dto.adminDTO;

public class AdminDepositActionRequest {
    private long depositId;

    public AdminDepositActionRequest() {}

    public AdminDepositActionRequest(long depositId) {
        this.depositId = depositId;
    }

    public long getDepositId() { return depositId; }
    public void setDepositId(long depositId) { this.depositId = depositId; }
}
