package com.vbay.server.realtime.subscription.validation;

import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.enums.realtime.RoomType;

///user room: xem sự thay đổi của các parameter của chính mình
public class UserRoomSubscriptionRule implements RoomSubscriptionRule {
    @Override
    public boolean support (Room room) {
        return room.getType() == RoomType.USER;
    }

    @Override
    public void validate(Room room, ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("Login required");
        }

        if (room.getTargetId() == null) {
            throw new ValidationException("USER room requires targetId");
        }

        if (room.getFilter() != null && !room.getFilter().isEmpty()) {
            throw new ValidationException("USER room does not accept filter");
        }
        ///targetId phải bằng session.userId
        if (!room.getTargetId().equals(session.getUserId())) {
            throw new AuthenticationException("Cannot subscribe another user's room");
        }
    }
}
