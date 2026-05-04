package com.vbay;

import java.io.IOException;
import com.vbay.network.SocketClient;
import com.vbay.shared.status.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene.SceneManager;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
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
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        SceneManager.setStage(primaryStage);
        primaryStage.setScene(SceneManager.createStyledScene("/jfx/scene/account/login.fxml"));
        primaryStage.setTitle("VBay");
        primaryStage.setMinWidth(430);
        primaryStage.setMinHeight(720);
        primaryStage.show();
        SceneManager.enterImmersiveMode();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("VBAY shut down ");
        try {
            SocketClient.getClient().disconnect();
        } catch (IOException exception) {
            System.err.println("Error while disconnecting: " + exception.getMessage());
        }
        super.stop();
    }


    public static void main(String[] args) {
        //ShutDownHook
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
        //Start FXApplication-->start()
        launch(args);
        
    }
}


