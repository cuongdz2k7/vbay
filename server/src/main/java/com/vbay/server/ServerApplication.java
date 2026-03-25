package com.vbay.server;

import com.vbay.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    private static final int PORT = 8123;

    public static void main(String[] args) {
        System.out.println("Server is running on port " + PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException exception) {
            System.err.println("Failed to start or run the server: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
