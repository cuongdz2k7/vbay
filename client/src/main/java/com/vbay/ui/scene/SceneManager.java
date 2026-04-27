package com.vbay.ui.scene;

import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage currentStage;

    public static void setStage(Stage newStage) {
        currentStage = newStage;
        if (currentStage != null) {
            currentStage.setFullScreenExitHint("");
            currentStage.fullScreenProperty().addListener((observable, wasFullScreen, isFullScreen) -> {
                if (!isFullScreen) {
                    currentStage.setMaximized(true);
                }
            });
        }
    }

    public static void switchScene(String fxmlPath) throws Exception {
        currentStage.setScene(createStyledScene(fxmlPath));
        currentStage.show();
        enterImmersiveMode();
    }

    public static void switchScene(String fxmlPath, Object sceneData) throws Exception {
        currentStage.setScene(createStyledScene(fxmlPath, sceneData));
        currentStage.show();
        enterImmersiveMode();
    }

    public static Scene createStyledScene(String fxmlPath) throws Exception {
        return createStyledScene(fxmlPath, null);
    }

    public static Scene createStyledScene(String fxmlPath, Object sceneData) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
        Parent root = loader.load();
        applySceneData(loader.getController(), sceneData);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(
            SceneManager.class.getResource(resolveStylesheetPath(fxmlPath))).toExternalForm());
        installGlobalShortcuts(scene);
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

    public static void enterImmersiveMode() {
        if (currentStage == null) {
            return;
        }

        currentStage.setMaximized(true);
        currentStage.setFullScreen(true);
    }

    public static void showOverviewMode() {
        if (currentStage == null) {
            return;
        }

        currentStage.setFullScreen(false);
        currentStage.setMaximized(true);
    }

    public static void showWindowedMode(double width, double height) {
        if (currentStage == null) {
            return;
        }

        currentStage.setFullScreen(false);
        currentStage.setMaximized(false);
        currentStage.setWidth(width);
        currentStage.setHeight(height);
        currentStage.centerOnScreen();
    }

    @SuppressWarnings("unchecked")
    private static void applySceneData(Object controller, Object sceneData) {
        if (sceneData == null || !(controller instanceof SceneDataReceiver<?>)) {
            return;
        }

        SceneDataReceiver<Object> receiver = (SceneDataReceiver<Object>) controller;
        receiver.setSceneData(sceneData);
    }

    private static void installGlobalShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                toggleFullScreen();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE && currentStage != null && currentStage.isFullScreen()) {
                showOverviewMode();
                event.consume();
            }
        });
    }

    private static void toggleFullScreen() {
        if (currentStage == null) {
            return;
        }

        if (currentStage.isFullScreen()) {
            showOverviewMode();
        } else {
            enterImmersiveMode();
        }
    }
}
