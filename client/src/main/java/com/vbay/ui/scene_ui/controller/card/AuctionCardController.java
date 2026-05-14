package com.vbay.ui.scene_ui.controller.card;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.LongConsumer;

import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
import com.vbay.ui.util.ProductImageLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class AuctionCardController {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMM d, yyyy, HH:mm", Locale.US);

    @FXML
    private VBox cardRoot;
    @FXML
    private ImageView productImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label bidStepLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ProgressBar progressBar;

    private Auction listAuction;
    private LongConsumer onSelected;
    private Timeline timeUpdater;

    @FXML
    private void initialize() {
        cardRoot.setFocusTraversable(true);
        cardRoot.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                stopTimeUpdater();
            }
        });
    }

    public void setAuction(Auction listAuction) {
        stopTimeUpdater();
        this.listAuction = listAuction;
        Product product = listAuction.getProduct();
        titleLabel.setText(listAuction.getTitle());
        priceLabel.setText("Current Bid  " + (listAuction.getWinnerUserId() == null ? "-" : formatCurrency(listAuction.getCurrentPrice())));
        startingPriceLabel.setText("Starting Price  " + formatCurrency(listAuction.getStartingPrice()));
        bidStepLabel.setText("Step  " + formatCurrency(listAuction.getMinimumBidStep()));
        ProductImageLoader.loadCover(productImageView, product.getImagePath());
        startTimeUpdater();
    }

    public void setOnSelected(LongConsumer onSelected) {
        this.onSelected = onSelected;
    }

    @FXML
    private void handleCardClicked(MouseEvent event) {
        notifySelection();
    }

    @FXML
    private void handleCardKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            notifySelection();
            event.consume();
        }
    }

    private void notifySelection() {
        if (listAuction != null && onSelected != null) {
            onSelected.accept(listAuction.getId());
        }
    }

    private void startTimeUpdater() {
        updateTimeState();
        timeUpdater = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> updateTimeState()));
        timeUpdater.setCycleCount(Timeline.INDEFINITE);
        timeUpdater.play();
    }

    private void stopTimeUpdater() {
        if (timeUpdater != null) {
            timeUpdater.stop();
            timeUpdater = null;
        }
    }

    private void updateTimeState() {
        if (listAuction == null) {
            return;
        }
        timeLabel.setText(formatAuctionTime(listAuction));
        progressBar.setProgress(calculateProgress(listAuction));
    }

    private static String formatCurrency(BigDecimal value) {
        return value == null ? "-" : CURRENCY_FORMAT.format(value);
    }

    private static String formatAuctionTime(Auction auction) {
        String status = auction.getStatus();
        if ("CANCELLED".equals(status)) {
            return "Cancelled";
        }
        if ("FAILED".equals(status)) {
            return "Failed";
        }
        if ("ENDED".equals(status)) {
            return "Ended";
        }

        LocalDateTime now = utcNow();
        LocalDateTime startingTime = auction.getStartingTime();
        LocalDateTime endingTime = auction.getEndingTime();
        if (endingTime == null || !now.isBefore(endingTime)) {
            return "Ended";
        }
        if ("SCHEDULED".equals(status)) {
            if (startingTime == null) {
                return "Starts: -";
            }
            Duration remainingUntilStart = Duration.between(now, startingTime);
            if (remainingUntilStart.compareTo(Duration.ofHours(24)) >= 0) {
                return "Starts: " + formatVietnamTime(startingTime);
            }
            return "Starts in " + formatRemainingDuration(remainingUntilStart);
        }
        if (!"ACTIVE".equals(status)) {
            return "-";
        }

        Duration remaining = Duration.between(now, endingTime);
        if (remaining.compareTo(Duration.ofHours(24)) >= 0) {
            return "Ends: " + formatVietnamTime(endingTime);
        }
        return formatRemainingDuration(remaining) + " Remaining";
    }

    private static String formatRemainingDuration(Duration remaining) {
        long seconds = remaining.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private static String formatVietnamTime(LocalDateTime utcTime) {
        return utcTime
            .atZone(UTC_ZONE)
            .withZoneSameInstant(VIETNAM_ZONE)
            .format(DISPLAY_TIME_FORMATTER);
    }

    private static double calculateProgress(Auction auction) {
        if (isClosedStatus(auction.getStatus())) {
            return 0.0;
        }

        LocalDateTime now = utcNow();
        LocalDateTime startingTime = auction.getStartingTime();
        if ("SCHEDULED".equals(auction.getStatus())) {
            return startingTime != null && now.isBefore(startingTime) ? 1.0 : 0.0;
        }

        LocalDateTime endingTime = auction.getEndingTime();
        if (startingTime == null || endingTime == null) {
            return 0.0;
        }
        long totalMillis = Duration.between(startingTime, endingTime).toMillis();
        if (totalMillis <= 0) {
            return 1.0;
        }

        long remainingMillis = Duration.between(now, endingTime).toMillis();
        double progress = (double) remainingMillis / totalMillis;
        return Math.max(0.0, Math.min(1.0, progress));
    }

    private static boolean isClosedStatus(String status) {
        return "ENDED".equals(status) || "FAILED".equals(status) || "SOLD".equals(status) || "CANCELLED".equals(status);
    }

    private static LocalDateTime utcNow() {
        return LocalDateTime.now(UTC_ZONE);
    }
}
