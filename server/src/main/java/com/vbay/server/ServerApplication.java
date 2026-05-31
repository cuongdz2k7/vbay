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
import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.server.scheduler.AuctionTaskScheduler;
import com.vbay.server.upload.ImageHttpServer;
import com.vbay.shared.Utils.LoggingUtils;

public class ServerApplication {
    private static final int PORT = 3618;
    private static final Logger LOGGER = LoggingUtils.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LoggingUtils.configure("server");

        ImageHttpServer imageHttpServer = new ImageHttpServer();
        AppConfig appConfig = new AppConfig();
        AuctionTaskScheduler auctionScheduler = appConfig.getAuctionScheduler();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SHUTDOWN] Server is shutting down. Ensuring all data is saved to database.");

            try {
                imageHttpServer.stop();
                auctionScheduler.shutdown();

                System.out.println("[SHUTDOWN] Database connections closed successfully.");
                System.out.println("[SHUTDOWN] All pending data saved to database.");
            } catch (Exception exception) {
                System.err.println("[SHUTDOWN] Error during database shutdown: " + exception.getMessage());
            }

            System.out.println("[SHUTDOWN] Server shutdown complete. All data persisted.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            DatabaseInitializer.init();
            imageHttpServer.start();
            auctionScheduler.start();

            RequestDistributor distributor = appConfig.getRequestDistributor();
            SubscriptionService subscriptionService = appConfig.getSubscriptionService();
            com.vbay.server.network_connection.ClientConnectionRegistry connectionRegistry = appConfig.getConnectionRegistry();

            LOGGER.info(() -> "Server listening on port " + PORT);
            LOGGER.info("Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                LOGGER.info(() -> "Accepted client connection from " + socket.getInetAddress().getHostAddress());
                Thread newThread = new Thread(new ClientHandler(socket, distributor, subscriptionService, connectionRegistry));
                newThread.start();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Server failed to start.", exception);
        }
    }
}
