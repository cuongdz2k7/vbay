package com.vbay.ui.scene;

import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage currentStage;

    public static void setStage(Stage newStage) {
        currentStage = newStage;
    }

    public static void switchScene(String fxmlPath) throws Exception {
        currentStage.setScene(createStyledScene(fxmlPath));
        currentStage.show();
    }

    public static Scene createStyledScene(String fxmlPath) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(
            SceneManager.class.getResource(resolveStylesheetPath(fxmlPath))).toExternalForm());
        return scene;
    }

    private static String resolveStylesheetPath(String fxmlPath) {
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1);
        String cssFileName = fileName.replace(".fxml", ".css");
        return "/jfx/css/" + cssFileName;
    }

    public static Stage getStage() {
        return currentStage;
    }
}
