package com.vbay;

import java.io.IOException;
import java.util.Objects;

import com.vbay.network.SocketClient;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene.SceneManager;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();
        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);

            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            Respond<?> response = client.sendMessage(request);
            if (response != null && response.isStatus()) {
                System.out.println("Connected to VBay server.");
            } else if (response != null) {
                System.err.println("Server verification failed: " + response.getMessage());
            } else {
                System.err.println("Server verification failed: empty response");
            }
        } catch (IOException exception) {
            System.err.println("Could not connect to server");
            exception.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initSocketClient();
        //default theme -> Light
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Parent loader = FXMLLoader.load(
            Objects.requireNonNull(getClass().getResource("/jfx/scene/login.fxml")));
        SceneManager.setStage(primaryStage);
        var scene = SceneManager.createStyledScene(loader);
        primaryStage.setScene(scene);
        primaryStage.setTitle("VBay");
        primaryStage.setMinWidth(430);
        primaryStage.setMinHeight(720);
        primaryStage.show();
    }
    //Stop
    @Override
    public void stop() throws Exception {
        System.out.println("VBAY SHUT DOWN");
        try {
            SocketClient.getClient().disconnect();
        } catch (IOException exception) {
            System.err.println("Error while disconnecting: " + exception.getMessage());
        }
        super.stop();
    }

    public static void main(String[] args) {
        // Add shutdown hook for sudden termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("App Shutting Down - Emergency shutdown detected");
            try {
                SocketClient client = SocketClient.getClient();
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception e) {
                System.err.println("Error during emergency shutdown: " + e.getMessage());
            }
        }));

        launch(args);
    }
}
