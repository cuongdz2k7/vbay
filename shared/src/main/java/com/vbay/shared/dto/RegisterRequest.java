package com.vbay.shared.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private char[] password;
    private String phone_number;

    public RegisterRequest(String username, String email, char[] password, String phone_number) {
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

    public char[] getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
            "username='" + this.getUsername() + '\'' +
            ", email='" + this.getEmail() + '\'' +
            ", password='" + (this.getPassword() == null ? null : "***") + '\'' +
            ", phone_number='" + this.getPhoneNumber() + '\'' +
            '}';
    }
}
