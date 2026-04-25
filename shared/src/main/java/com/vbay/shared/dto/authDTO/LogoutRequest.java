package com.vbay.shared.dto.authDTO;

public class LogoutRequest {
    private String userId;

    public LogoutRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "LogoutRequest{" +
            "userId='" + this.getUserId() + '\'' +
            '}';
    }
}
