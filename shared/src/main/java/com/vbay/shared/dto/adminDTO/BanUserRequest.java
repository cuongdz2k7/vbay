package com.vbay.shared.dto.adminDTO;

public class BanUserRequest {
    private long userId;

    public BanUserRequest() {
    }

    public BanUserRequest(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
