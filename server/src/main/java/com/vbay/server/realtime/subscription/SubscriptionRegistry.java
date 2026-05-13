package com.vbay.server.realtime.subscription;

import java.util.Set;

import com.vbay.server.network_connection.ClientConnection;
import com.vbay.shared.dto.realtimeDTO.Room;

///đây là trung tâm đăng kí và quản lý object tồn tại trong RAM
///sau này có thể mở rộng thêm redis (khi nhiều server và cần thêm 1 layer trong infrastructue network)
public interface SubscriptionRegistry {
    public void subscribe (Room room, ClientConnection connection);
    public void unsubscribe (Room room, ClientConnection connection);
    public void unsubscribeAll(ClientConnection connection);
    public Set<ClientConnection> findSubscribers(Room room);

}
