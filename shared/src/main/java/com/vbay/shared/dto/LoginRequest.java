package com.vbay.shared.dto;

public class LoginRequest {
    private String username;
    private String email;
    private char[] password;
    private String phone_number;

    public LoginRequest(String username, String email, String password, String phone_number) {
        this(username, email, password == null ? null : password.toCharArray(), phone_number);
    }

    public LoginRequest(String username, String email, char[] password, String phone_number) {
        this.username = username;
        this.email = email;
        this.password = password == null ? null : password.clone();
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

    public String getPhone_number() {
        return phone_number;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
            "username='" + this.getUsername() + '\'' +
            ", email='" + this.getEmail() + '\'' +
            ", password='" + (this.getPassword() == null ? null : "***") + '\'' +
            ", phone_number='" + this.getPhoneNumber() + '\'' +
            '}';
    }
}
// Khong hien password that, chi hien da co gia tri hay chua.
