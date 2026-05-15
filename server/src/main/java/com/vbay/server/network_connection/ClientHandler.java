package com.vbay.server.network_connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.protocol.Respond;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LoggingUtils.getLogger(ClientHandler.class);
    private final Socket socket; 
    private final RequestDistributor distributor;
    private final SubscriptionService subscriptionService;

    private final ClientSession session = new ClientSession();
    
    public ClientHandler(Socket socket, RequestDistributor distributor, SubscriptionService subscriptionService) {
        this.socket = socket;
        this.distributor = distributor;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress();
        LOGGER.info(() -> "Client connected: " + clientAddress);
        try (
            Socket clientsocket = socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientsocket.getOutputStream(), true)
        ) {
            ClientConnection connection = new ClientConnection(session, out);
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isBlank()) {
                        LOGGER.warning(() -> "Empty request from " + clientAddress);
                        continue;
                    }

                    Respond<?> response = distributor.dispatch(line, session, connection);
                    connection.send(response);
                }
            } finally {
                subscriptionService.disconnect(connection);
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Client connection error for " + clientAddress, exception);
        }
        finally {
            LOGGER.info(() -> "Client disconnected: " + clientAddress);
            session.clearSession();
        }
    }
}
