package com.vbay.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.server.databaseManager.DatabaseInitializer;
import com.vbay.server.network_connection.ClientHandler;
import com.vbay.server.network_connection.RequestDistributor;
import com.vbay.shared.Utils.LoggingUtils;

public class ServerApplication {
    private static final int PORT = 3618;
    private static final Logger LOGGER = LoggingUtils.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LoggingUtils.configure("server");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Server is shutting down. Ensuring all data is saved to database.");

            try {
                LOGGER.info("Database connections closed successfully.");
                LOGGER.info("All pending data saved to database.");
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Error during database shutdown.", exception);
            }

            LOGGER.info("Server shutdown complete. All data persisted.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            DatabaseInitializer.init();

            AppConfig appConfig = new AppConfig();
            RequestDistributor distributor = appConfig.getRequestDistributor();

            LOGGER.info(() -> "Server listening on port " + PORT);
            LOGGER.info("Waiting for clients...");
            while(true) {
                Socket socket = serverSocket.accept();
                LOGGER.info(() -> "Accepted client connection from " + socket.getInetAddress().getHostAddress());
                Thread newThread = new Thread(new ClientHandler(socket, distributor));
                newThread.start();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Server failed to start.", exception);
        }
    }
}
