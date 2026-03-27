package com.vbay.ui;

import com.vbay.MainApp;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {

    public static void launchApplication(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent loader = FXMLLoader.load(
            Objects.requireNonNull(getClass().getResource("/jfx/scene/Authentication.fxml")));
        Scene stage = new Scene(loader, 350, 500);
        primaryStage.setScene(stage);
        primaryStage.show();
    }

}
