package com.vbay.ui;
import atlantafx.base.theme.PrimerLight;
import java.util.Objects;

import com.vbay.ui.scene.SceneManager;

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
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Parent loader = FXMLLoader.load(
            Objects.requireNonNull(getClass().getResource("/jfx/scene/Authentication.fxml")));
        Scene scene = new Scene(loader);
        primaryStage.setScene(scene);
        primaryStage.show();

        SceneManager.setStage(primaryStage);
    }
}
