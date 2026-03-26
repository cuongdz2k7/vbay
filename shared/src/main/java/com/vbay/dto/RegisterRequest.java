package com.vbay.dto;

public class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public String phone_number;

    public RegisterRequest(String username, String email, String password, String phone_number) {
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
