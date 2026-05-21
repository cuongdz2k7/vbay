package com.vbay.shared.dto.adminDTO;

import java.util.List;

public class AdminUserListResponse {
    private List<AdminUserDTO> users;

    public AdminUserListResponse() {
    }

    public AdminUserListResponse(List<AdminUserDTO> users) {
        this.users = users;
    }

    public List<AdminUserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUserDTO> users) {
        this.users = users;
    }
}
