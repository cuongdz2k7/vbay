package com.vbay.shared.dto;

public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String phone_number;

    public UserDTO(String id, String username, String email, String phone_number) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone_number = phone_number;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + this.getId() + '\'' +
            ", username='" + this.getUsername() + '\'' +
            ", email='" + this.getEmail() + '\'' +
            ", phone_number='" + this.getPhone_number() + '\'' +
            '}';
    }
}
