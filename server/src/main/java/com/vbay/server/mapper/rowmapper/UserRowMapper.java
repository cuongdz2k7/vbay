package com.vbay.server.mapper.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.vbay.server.model.User;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class UserRowMapper {
    private UserRowMapper() {}
    public static User mapUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"), 
                        rs.getString("phone_number"),
                        Position.valueOf(rs.getString("position")),
                        UserStatus.valueOf(rs.getString("status")),
                        rs.getBigDecimal("available_balance"),
                        rs.getBigDecimal("hold_balance"),
                        rs.getString("time_init"));
        user.setId(rs.getLong("id"));
        user.setVersion(rs.getLong("version"));
        return user;
    }
}
