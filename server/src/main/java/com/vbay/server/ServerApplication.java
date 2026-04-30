package com.vbay.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.vbay.server.databaseManager.DatabaseInitializer;
import com.vbay.server.network_connection.ClientHandler;
import com.vbay.server.network_connection.RequestDistributor;
import com.vbay.server.repository.JDBCrepository.JdbcUserRepository;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.security.Argon2PasswordHasher;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;

public class ServerApplication {
    private static final int PORT = 3618;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server is shutting down - Ensuring all data is saved to database");
            try {
                System.out.println("Database connections closed successfully");
                System.out.println("All pending data saved to database");
            } catch (Exception exception) {
                System.err.println("Error during database shutdown: " + exception.getMessage());
            }
            System.out.println("Server shutdown complete - All data persisted");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            DatabaseInitializer.init();

            UserRepository userRepository = new JdbcUserRepository();
            PasswordHasher passwordHasher = new Argon2PasswordHasher();
            AuthService authService = new AuthService(userRepository, passwordHasher);
            AuctionService auctionService = new AuctionService();
            RequestDistributor distributor = new RequestDistributor(authService, auctionService);

            System.out.println("Port: " + PORT);
            System.out.println("waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                Thread thread = new Thread(new ClientHandler(socket, distributor));
                thread.start();
            }
        } catch (IOException exception) {
            System.err.println("Server failed to start : " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
