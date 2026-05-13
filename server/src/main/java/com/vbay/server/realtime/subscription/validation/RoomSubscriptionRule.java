package com.vbay.server.realtime.subscription.validation;

import com.vbay.server.network_connection.ClientSession;
import com.vbay.shared.dto.realtimeDTO.Room;

///gần như là factory method, khi nào có thêm request thì implement thêm
/// đảm bảo OCP
public interface RoomSubscriptionRule {
    //support check rule này có áp dụng cho Roomtype này không? 
    public boolean support (Room room);
    public void validate (Room room, ClientSession session);
}
