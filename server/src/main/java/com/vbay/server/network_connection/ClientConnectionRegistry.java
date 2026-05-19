package com.vbay.server.network_connection;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.shared.Utils.LoggingUtils;

public class ClientConnectionRegistry {
    private static final Logger LOGGER = LoggingUtils.getLogger(ClientConnectionRegistry.class);
    private final Map<Long, ClientConnection> activeConnections = new ConcurrentHashMap<>();

    public void register(long userId, ClientConnection connection) {
        activeConnections.put(userId, connection);
        LOGGER.info(() -> "Registered ClientConnection for userId: " + userId);
    }

    public void unregister(long userId) {
        activeConnections.remove(userId);
        LOGGER.info(() -> "Unregistered ClientConnection for userId: " + userId);
    }

    public ClientConnection findByUserId(long userId) {
        return activeConnections.get(userId);
    }

    public void disconnectUser(long userId) {
        ClientConnection connection = activeConnections.remove(userId);
        if (connection != null) {
            try {
                LOGGER.info(() -> "Force disconnecting userId: " + userId);
                connection.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while force disconnecting user: " + userId, e);
            }
        }
    }
}
