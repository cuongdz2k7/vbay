package com.vbay.server.network_connection;
//Import
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.protocol.Respond;

// Listening
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LoggingUtils.getLogger(ClientHandler.class);
    private final Socket socket; 
    private final RequestDistributor Distributor;

    private final ClientSession session = new ClientSession();
    
    ///ClientSession
    
    //Constructor
    public ClientHandler(Socket socket, RequestDistributor Distributor) {
        this.socket = socket;
        this.Distributor = Distributor;
    }

    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress();
        LOGGER.info(() -> "Client connected: " + clientAddress);
        try (
            Socket clientsocket = socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream())); // client gui request
            PrintWriter out = new PrintWriter(clientsocket.getOutputStream(), true) // client nhan response
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()){
                    LOGGER.warning(() -> "Empty request from " + clientAddress);
                    continue;
                }
                if ("exit".equalsIgnoreCase(line)){
                    out.println("Exit current socket");
                    session.clearSession();
                    break;
                }
                Respond<?> response = Distributor.dispatch(line, session);
                String jsonResponse = JsonUtils.toJson(response);
                out.println(jsonResponse);
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Client connection error for " + clientAddress, exception);
        }
        finally{
            LOGGER.info(() -> "Client disconnected: " + clientAddress);
        }
    }
}
