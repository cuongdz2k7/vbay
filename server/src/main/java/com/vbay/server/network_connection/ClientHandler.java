package com.vbay.server.network_connection;
//Import
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.shared.protocol.Respond;

// Listening
public class ClientHandler implements Runnable {
    private final Socket socket; 
    private final RequestDistributor distributor;
    private final SubscriptionService subscriptionService;

    private final ClientSession session = new ClientSession();
    
    //Constructor
    public ClientHandler(Socket socket, RequestDistributor distributor, SubscriptionService subscriptionService) {
        this.socket = socket;
        this.distributor = distributor;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
        try (
            Socket clientsocket = socket; ///tạo con trỏ đến socket để try-with-resources
            BufferedReader in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream())); // client gui request
            PrintWriter out = new PrintWriter(clientsocket.getOutputStream(), true) // client nhan response
        ) {
            ClientConnection clientConnection = new ClientConnection(session, out);
            try {
                String line;
                while ((line = in.readLine()) != null) { /// = null khi không kết nối đc tới phía client nữa
                    if (line.isBlank()) {
                        continue;
                    }

                    Respond<?> response = distributor.dispatch(line, session);
                    clientConnection.send(response);
                }
            } finally {
                subscriptionService.disconnect(clientConnection);
            }
        } catch (IOException exception) {
            System.err.println("Client connection error: " + exception.getMessage());
        }
        finally{
            System.out.println("Client disconnected: " +socket.getInetAddress().getHostAddress());
            session.clearSession();
        }
    }
}
