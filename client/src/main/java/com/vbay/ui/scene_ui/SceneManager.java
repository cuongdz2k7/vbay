package com.vbay.ui.scene_ui;

import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage currentStage;
    private static final String THEME_CSS = Objects.requireNonNull(
        SceneManager.class.getResource("/jfx/css/Theme.css")
    ).toExternalForm();
    private static final String NOTIFICATION_CSS = Objects.requireNonNull(
        SceneManager.class.getResource("/jfx/css/Notification.css")
    ).toExternalForm();

    // Persistent Floating Background Music Overlay Fields
    private static HBox floatingMusicPill;
    private static Button musicMuteBtn;
    private static SVGPath musicMuteIcon;
    private static Label musicStatusLabel;

    private static final String SVG_SPEAKER_ON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private static final String SVG_SPEAKER_MUTED = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.21.05-.42.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

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
        StackPane root = new StackPane(view.root);
        Scene scene = new Scene(root);
        scene.getStylesheets().setAll(THEME_CSS, view.stylesheetPath, NOTIFICATION_CSS);
        ensureMusicOverlay(root);
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
        
        if (scene == null) {
            StackPane root = new StackPane(view.root);
            scene = new Scene(root);
            scene.getStylesheets().setAll(THEME_CSS, view.stylesheetPath, NOTIFICATION_CSS);
            ensureMusicOverlay(root);
            installGlobalShortcuts(scene);
            currentStage.setScene(scene);
        } else {
            if (scene.getRoot() instanceof StackPane root) {
                // The first child is the scene content
                root.getChildren().set(0, view.root);
                ensureMusicOverlay(root);
                // Keep other children (like notifications)
            } else {
                StackPane root = new StackPane(view.root);
                scene.setRoot(root);
                ensureMusicOverlay(root);
            }
            
            scene.getStylesheets().setAll(THEME_CSS, view.stylesheetPath, NOTIFICATION_CSS);
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

    private static void ensureMusicOverlay(StackPane root) {
        // No-op to completely hide and disable the floating music controller widget
    }

    private static void createFloatingMusicPill() {
        floatingMusicPill = new HBox();
        floatingMusicPill.setAlignment(Pos.CENTER);
        floatingMusicPill.setSpacing(8);
        floatingMusicPill.getStyleClass().add("floating-music-chip");
        floatingMusicPill.setMaxWidth(Region.USE_PREF_SIZE);
        floatingMusicPill.setMaxHeight(Region.USE_PREF_SIZE);
        
        musicMuteIcon = new SVGPath();
        musicMuteIcon.getStyleClass().add("music-icon-shape");
        
        musicMuteBtn = new Button();
        musicMuteBtn.setGraphic(musicMuteIcon);
        musicMuteBtn.getStyleClass().add("music-mute-btn-mini");
        
        musicStatusLabel = new Label();
        musicStatusLabel.getStyleClass().add("music-status-mini");
        
        floatingMusicPill.getChildren().addAll(musicMuteBtn, musicStatusLabel);
        
        StackPane.setAlignment(floatingMusicPill, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(floatingMusicPill, new Insets(0, 24, 24, 0));
        
        // Update initially
        updateMusicUI();
        
        musicMuteBtn.setOnAction(event -> {
            boolean nextMuteState = !com.vbay.ui.util.MusicManager.isMuted();
            com.vbay.ui.util.MusicManager.setMuted(nextMuteState);
            updateMusicUI();
        });
    }
    
    private static void updateMusicUI() {
        if (musicMuteIcon == null || musicStatusLabel == null) return;
        boolean muted = com.vbay.ui.util.MusicManager.isMuted();
        musicMuteIcon.setContent(muted ? SVG_SPEAKER_MUTED : SVG_SPEAKER_ON);
        if (muted) {
            musicStatusLabel.setText("OFF");
        } else {
            musicStatusLabel.setText("MTP");
        }
    }
}
