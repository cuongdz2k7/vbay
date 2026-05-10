package com.vbay.server.realtime.subscription.validation;

import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.enums.realtime.RoomType;

public class AuctionListRoomSubscriptionRule implements RoomSubscriptionRule {
    @Override
    public boolean support (Room room) {
        return room.getType() == RoomType.AUCTION_LIST;
    }

    @Override
    public void validate(Room room, ClientSession session) {
        if (room.getTargetId() != null) {
            throw new ValidationException("AUCTION_LIST room does not accept targetId");
        }

        // Nếu sau này app không yêu cầu đăng nhập để view sản phẩm.
        
        if (session == null || !session.isAuthenticated()) {
             throw new AuthenticationException("Login required");
         }

        // Sau này validate filter:
        // category hợp lệ không
        // status hợp lệ không
        // sellerId hợp lệ không
    }
}
