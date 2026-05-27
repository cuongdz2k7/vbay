package com.vbay.shared.dto.adminDTO;

import java.util.List;

public class AdminUserListResponse {
    private List<AdminUserItem> users;

    public AdminUserListResponse() {}

    public AdminUserListResponse(List<AdminUserItem> users) {
        this.users = users;
    }

    public List<AdminUserItem> getUsers() { return users; }
    public void setUsers(List<AdminUserItem> users) { this.users = users; }
}
