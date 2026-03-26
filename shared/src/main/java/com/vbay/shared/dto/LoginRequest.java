package com.vbay.shared.dto;

public abstract class LoginRequest {
    private String username;
    private String email;
    private String password;
    private String phone_number;

    public LoginRequest(String username, String email, String password, String phone_number) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone_number = phone_number;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getPhone_number() {
        return phone_number;
    }
    
}
