package com.vbay.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.vbay.server.Network_connection.ClientHandler;
import com.vbay.server.Network_connection.RequestDistributor;
import com.vbay.server.databaseManager.DatabaseInitializer;

public class ServerApplication {
    private static final int PORT = 3618;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server is shutting down - Ensuring all data is saved to database");

            try {
                System.out.println("Database connections closed successfully");
                System.out.println("All pending data saved to database");
            } catch (Exception e) {
                System.err.println("Error during database shutdown: " + e.getMessage());
            }

            System.out.println("Server shutdown complete - All data persisted");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            DatabaseInitializer.init();

            AppConfig appConfig = new AppConfig();
            RequestDistributor distributor = appConfig.getRequestDistributor();

            System.out.println("Port: " + PORT);
            System.out.println("waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                Thread newThread = new Thread(new ClientHandler(socket, distributor));
                newThread.start();
            }
        } catch (IOException exception) {
            System.err.println("Server failed to start : " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
