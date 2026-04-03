package com.vbay.ui.scene;

import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static final String APP_STYLESHEET = "/jfx/css/app.css";
    private static Stage currentStage;

    public static void setStage(Stage new_stage) {
        currentStage = new_stage;
    }

    public static void switchScene(String fxmlPath) throws Exception { // Path tuyệt đối, ví dụ: "/jfx/scene/login.fxml"
        Parent root = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
        currentStage.setScene(createStyledScene(root));
        currentStage.show();
    }

    public static Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            Objects.requireNonNull(SceneManager.class.getResource(APP_STYLESHEET)).toExternalForm()
        );
        return scene;
    }

    public static Stage getStage() {
        return currentStage;
    }
}
