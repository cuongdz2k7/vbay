package com.vbay.server.network_connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.protocol.Respond;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RequestDistributor distributor;
    private final ClientSession session = new ClientSession();

    public ClientHandler(Socket socket, RequestDistributor distributor) {
        this.socket = socket;
        this.distributor = distributor;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
        try (
            Socket clientSocket = socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    System.out.println("Empty request");
                    continue;
                }
                if ("exit".equalsIgnoreCase(line)) {
                    out.println("Exit current socket");
                    break;
                }
                Respond<?> response = distributor.dispatch(line, session);
                String jsonResponse = JsonUtils.toJson(response);
                out.println(jsonResponse);
            }
        } catch (IOException exception) {
            System.err.println("Client connection error: " + exception.getMessage());
        } finally {
            System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
        }
    }
}
