package com.vbay;

import java.io.IOException;
import java.util.Objects;

import com.vbay.network.SocketClient;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.ui.scene.SceneManager;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApp extends Application{


    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();
        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);

            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            client.sendMessage(request);
            System.out.println("Connected to VBay server.");
        } catch (IOException e) {
            System.err.println("Could not connect to server");
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Error while disconnecting: " + e.getMessage());
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

    public static void main(String[] args) {
//        initSocketClient(); // connect to server before launching program
        launch(args);
    }

}
