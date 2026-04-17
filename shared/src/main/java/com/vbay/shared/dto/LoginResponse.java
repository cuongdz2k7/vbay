package com.vbay.shared.dto;

import com.vbay.shared.status.Position;

public class LoginResponse {
    private String userId;
    private String username;
    private String email;
    private Position position;

    public LoginResponse(String userId, String username, String email, Position position) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.position = position;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Position getPosition() {
        return position;
    }
}
