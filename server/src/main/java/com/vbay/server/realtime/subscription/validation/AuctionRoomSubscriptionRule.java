package com.vbay.server.realtime.subscription.validation;

import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.enums.realtime.RoomType;

public class AuctionRoomSubscriptionRule implements RoomSubscriptionRule {
    
    @Override
    public boolean support (Room room) {
        return room.getType() == RoomType.AUCTION;
    }

    @Override
    public void validate(Room room, ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("Login required");
        }

        if (room.getTargetId() == null) {
            throw new ValidationException("AUCTION room requires targetId");
        }

        if (room.getFilter() != null && !room.getFilter().isEmpty()) {
            throw new ValidationException("AUCTION room does not accept filter");
        }

        // Sau này check DB:
        // auction có tồn tại không
        // user có quyền xem auction không
    }
}
