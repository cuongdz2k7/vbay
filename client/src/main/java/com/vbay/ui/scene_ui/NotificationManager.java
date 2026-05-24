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
    private static final double NOTIFICATION_WIDTH = 390;
    private static final Duration DISPLAY_DURATION = Duration.seconds(4);

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

            VBox notification = new VBox(7);
            notification.getStyleClass().addAll("notification-container", type.getStyleClass());
            notification.setMinWidth(NOTIFICATION_WIDTH);
            notification.setMaxWidth(NOTIFICATION_WIDTH);
            
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("notification-title");

            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);

            Region progress = new Region();
            progress.getStyleClass().add("notification-progress");
            progress.setMinWidth(0);
            progress.setPrefWidth(NOTIFICATION_WIDTH - 28);
            progress.setMaxWidth(NOTIFICATION_WIDTH - 28);

            StackPane progressTrack = new StackPane(progress);
            progressTrack.getStyleClass().add("notification-progress-track");
            progressTrack.setAlignment(Pos.CENTER_LEFT);
            progressTrack.setMaxWidth(Double.MAX_VALUE);

            notification.getChildren().addAll(titleLabel, messageLabel, progressTrack);

            notificationContainer.getChildren().add(0, notification);

            // Slide-in animation
            notification.setTranslateX(400);
            Timeline slideIn = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(notification.translateXProperty(), 0))
            );
            slideIn.play();

            Timeline timeline = new Timeline(
                new KeyFrame(DISPLAY_DURATION, new KeyValue(progress.maxWidthProperty(), 0))
            );

            timeline.setOnFinished(event -> dismiss(notification));

            timeline.play();
            
            notification.setOnMouseClicked(event -> {
                timeline.stop();
                dismiss(notification);
            });
        });
    }

    private static void dismiss(VBox notification) {
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.millis(160),
                new KeyValue(notification.opacityProperty(), 0),
                new KeyValue(notification.translateXProperty(), 40))
        );
        fadeOut.setOnFinished(event -> notificationContainer.getChildren().remove(notification));
        fadeOut.play();
    }
}
