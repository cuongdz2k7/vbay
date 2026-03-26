package com.vbay.shared.dto;

public class LoginRequest {
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

    @Override
    public String toString() {
        return "LoginRequest{" +
            "username='" + this.getUsername() + '\'' +
            ", email='" + this.getEmail() + '\'' +
            ", password='" + (this.getPassword() == null ? null : "***") + '\'' +
            ", phone_number='" + this.getPhone_number() + '\'' +
            '}';
    }
}
// Không hiện password thật --? chỉ hiện xem pass CÓ TỒN TẠI hay KHÔNG\
// --> giảm thiểu leak pass
