package com.vbay;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.network.SocketClient;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.SceneManager;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;
public class MainApp extends Application {
    private static final Logger LOGGER = LoggingUtils.getLogger(MainApp.class);

    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();

        try {
            LOGGER.info("Connecting to VBay server...");
            client.connect("localhost", 3618);

            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            Respond<?> response = client.sendMessage(request);

            if (response != null && response.isStatus()) {
                LOGGER.info("Connected to VBay server.");
            } else if (response != null) {
                LOGGER.warning(() -> "Server verification failed: " + response.getMessage());
            } else {
                LOGGER.warning("Server verification failed: empty response");
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Could not connect to server.", exception);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initSocketClient();
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        SceneManager.setStage(primaryStage);
        primaryStage.setScene(SceneManager.createStyledScene("/jfx/scene/auth/Login.fxml"));
        primaryStage.setTitle("VBay");
        primaryStage.setMinWidth(430);
        primaryStage.setMinHeight(720);
        primaryStage.show();
        SceneManager.enterImmersiveMode();
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("VBay client shutting down.");
        try {
            SocketClient.getClient().disconnect();
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Error while disconnecting.", exception);
        }
        super.stop();
    }


    public static void main(String[] args) {
        LoggingUtils.configure("client");
        //ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.warning("App shutting down. Emergency shutdown detected.");
            try {
                SocketClient client = SocketClient.getClient();
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception exception) {
                LOGGER.log(Level.WARNING, "Error during emergency shutdown.", exception);
            }
        }));
        //Start FXApplication-->start()
        launch(args);
        
    }
}


