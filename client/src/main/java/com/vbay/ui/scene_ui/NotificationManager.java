package com.vbay.ui.scene_ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class NotificationManager {

    public enum NotificationType {
        SUCCESS("notification-success"),
        INFO("notification-info"),
        WARNING("notification-warning"),
        ERROR("notification-error");

        private final String styleClass;

        NotificationType(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    private static VBox notificationContainer;

    private static void ensureContainer() {
        if (notificationContainer == null) {
            notificationContainer = new VBox(10);
            notificationContainer.setPadding(new Insets(20));
            notificationContainer.setAlignment(Pos.TOP_RIGHT);
            notificationContainer.setPickOnBounds(false); // Allow clicking through to underlying UI
            notificationContainer.setMaxWidth(450);
            notificationContainer.setMouseTransparent(false);
        }

        Scene scene = SceneManager.getStage().getScene();
        if (scene != null && scene.getRoot() instanceof StackPane root) {
            if (!root.getChildren().contains(notificationContainer)) {
                root.getChildren().add(notificationContainer);
                StackPane.setAlignment(notificationContainer, Pos.TOP_RIGHT);
            }
        } else if (scene != null) {
            Pane oldRoot = (Pane) scene.getRoot();
            StackPane newRoot = new StackPane(oldRoot, notificationContainer);
            scene.setRoot(newRoot);
            StackPane.setAlignment(notificationContainer, Pos.TOP_RIGHT);
        }
    }

    public static void show(NotificationType type, String title, String message) {
        Platform.runLater(() -> {
            ensureContainer();

            VBox notification = new VBox(5);
            notification.getStyleClass().addAll("notification-container", type.getStyleClass());
            
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("notification-title");

            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);

            Region progress = new Region();
            progress.getStyleClass().add("notification-progress");
            progress.setMinWidth(0);
            progress.setPrefWidth(400); // Max width of container

            notification.getChildren().addAll(titleLabel, messageLabel, progress);

            notificationContainer.getChildren().add(0, notification);

            // Slide-in animation
            notification.setTranslateX(400);
            Timeline slideIn = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(notification.translateXProperty(), 0))
            );
            slideIn.play();

            // Animation for progress bar
            Timeline timeline = new Timeline();
            // Start from full width and go to 0
            KeyValue keyValue = new KeyValue(progress.prefWidthProperty(), 0);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(3), keyValue);
            timeline.getKeyFrames().add(keyFrame);

            timeline.setOnFinished(event -> {
                Timeline fadeOut = new Timeline();
                fadeOut.getKeyFrames().add(new KeyFrame(Duration.millis(300), 
                    new KeyValue(notification.opacityProperty(), 0)));
                fadeOut.setOnFinished(e -> notificationContainer.getChildren().remove(notification));
                fadeOut.play();
            });

            timeline.play();
            
            // Allow clicking to dismiss
            notification.setOnMouseClicked(event -> {
                timeline.stop();
                notificationContainer.getChildren().remove(notification);
            });
        });
    }
}
