package com.vbay.server.mapper.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.vbay.server.Model.User;
import com.vbay.shared.enums.Position;
import com.vbay.shared.enums.shared_status.UserStatus;


public class UserRowMapper {
    private UserRowMapper() {}
    public static User mapUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"), 
                        rs.getString("phone_number"),
                        Position.valueOf(rs.getString("position")),
                        UserStatus.valueOf(rs.getString("status")),
                        rs.getBigDecimal("balance"),
                        rs.getString("time_init"));
        user.setId(rs.getLong("id"));
        return user;
    }
}
