package com.vbay.ui.scene_ui;

import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
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
    // Switch Scene without any inheriting data
    public static void switchScene(String fxmlPath) throws Exception {
        applyViewToStage(loadView(fxmlPath, null));
    }
    // With inheritance
    public static void switchScene(String fxmlPath, Object sceneData) throws Exception {
        applyViewToStage(loadView(fxmlPath, sceneData));
    }

    public static Scene createStyledScene(String fxmlPath) throws Exception {
        return createStyledScene(fxmlPath, null);
    }

    public static Scene createStyledScene(String fxmlPath, Object sceneData) throws Exception {
        LoadedView view = loadView(fxmlPath, sceneData);
        Scene scene = new Scene(view.root);
        String notificationCss = SceneManager.class.getResource("/jfx/css/Notification.css").toExternalForm();
        scene.getStylesheets().setAll(view.stylesheetPath, notificationCss);
        installGlobalShortcuts(scene);
        return scene;
    }

    private static String resolveStylesheetPath(String fxmlPath) {
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1);
        int extensionIndex = fileName.lastIndexOf('.');
        String baseName = extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
        String cssFileName = baseName + ".css";
        return "/jfx/css/" + cssFileName;
    }

    public static Stage getStage() {
        return currentStage;
    }

    private static void applyViewToStage(LoadedView view) {
        if (currentStage == null) {
            throw new IllegalStateException("Stage has not been initialized.");
        }

        Scene scene = currentStage.getScene();
        String notificationCss = SceneManager.class.getResource("/jfx/css/Notification.css").toExternalForm();
        
        if (scene == null) {
            StackPane root = new StackPane(view.root);
            scene = new Scene(root);
            scene.getStylesheets().setAll(view.stylesheetPath, notificationCss);
            installGlobalShortcuts(scene);
            currentStage.setScene(scene);
        } else {
            if (scene.getRoot() instanceof StackPane root) {
                // The first child is the scene content
                root.getChildren().set(0, view.root);
                // Keep other children (like notifications)
            } else {
                StackPane root = new StackPane(view.root);
                scene.setRoot(root);
            }
            
            // Atomically set exactly the two required stylesheets
            scene.getStylesheets().setAll(view.stylesheetPath, notificationCss);
        }

        if (!currentStage.isShowing()) {
            currentStage.show();
        }
    }

    private static LoadedView loadView(String fxmlPath, Object sceneData) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
        Parent root = loader.load();
        applySceneData(loader.getController(), sceneData);
        String stylesheetPath = Objects.requireNonNull(
            SceneManager.class.getResource(resolveStylesheetPath(fxmlPath))
        ).toExternalForm();
        return new LoadedView(root, stylesheetPath);
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

    private record LoadedView(Parent root, String stylesheetPath) { }
}
