package com.vbay;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("VBay client is running.");
        StackPane root = new StackPane(label);
        root.setPadding(new Insets(24));

        stage.setTitle("VBay");
        stage.setScene(new Scene(root, 480, 240));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
