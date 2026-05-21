package com.vbay.shared.dto.adminDTO;

public class UnbanUserRequest {
    private long userId;

    public UnbanUserRequest() {
    }

    public UnbanUserRequest(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
