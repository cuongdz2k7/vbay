package com.vbay.server.realtime.subscription;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.vbay.server.network_connection.ClientConnection;
import com.vbay.shared.dto.realtimeDTO.Room;


public class InMemorySubscriptionRegistry implements SubscriptionRegistry {
    private final Map<String, Set<ClientConnection>> subscribersByRoom = new ConcurrentHashMap<>();
    ///room -> connections
    private final Map<ClientConnection, Set<String>> roomsByConnection = new ConcurrentHashMap<>();
    ///connection -> rooms
    @Override
    public void subscribe (Room room, ClientConnection connection) {
        String roomKey = room.key();
        /*
        ComputIfAbsent:
            Nếu key đã có value:
                trả về value hiện tại

            Nếu key chưa có value:
                gọi function để tạo value mới        

        .newKeySet = tạo ra 1 set mới, nó là kiểu keySet.
        */
        subscribersByRoom.computeIfAbsent(roomKey, ignored -> ConcurrentHashMap.newKeySet()).add(connection);
        roomsByConnection.computeIfAbsent(connection, ignored -> ConcurrentHashMap.newKeySet()).add(roomKey);
    }

    @Override
    public void unsubscribe (Room room, ClientConnection connection) {
        String roomKey = room.key();
        ///computeIfPresent: Chỉ xử lý khi key đã tồn tại trong map
        subscribersByRoom.computeIfPresent(roomKey, (key, subscribers) -> {
            subscribers.remove(connection);
            if (subscribers.isEmpty()) {
                return null;
            }
            return subscribers;
        });
    } 

    @Override
    public void unsubscribeAll(ClientConnection connection) {
        Set<String> rooms = roomsByConnection.remove(connection);
        ///trả về value cũ trước khi bị xóa
        if (rooms != null) {
            for (String roomKey : rooms) {
                Set<ClientConnection> subscribers = subscribersByRoom.get(roomKey);
                if (subscribers != null) {
                    subscribers.remove(connection);
                    if (subscribers.isEmpty()) {
                        subscribersByRoom.remove(roomKey);
                    }
                }
            }
        }
    }

    @Override
    public Set<ClientConnection> findSubscribers(Room room) {
        return subscribersByRoom.getOrDefault(room.key(), ConcurrentHashMap.newKeySet());
    }

}
