package com.vbay.ui.scene_ui.controller.bid;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.network.dispatcher.RealtimeEventDispatcher;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.realtime.AuctionStateChangeReason;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneDataReceiver;
import com.vbay.ui.util.MoneyInput;
import com.vbay.ui.util.ProductImageLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


/*
PlaceBid

Broadcast public cho auction:{auctionId}:
AUCTION_STATE_UPDATED: currentPrice, winnerUserId, nextMinimumBid...
BID_HISTORY_ITEM_ADDED
Broadcast personal cho user:{newBidderId}:
MY_BID_LIST_ITEM_UPDATED với bidStatus = WINNING
Nếu có previousWinningUserId và khác new bidder:
Broadcast personal cho user:{previousWinningUserId}:
MY_BID_LIST_ITEM_UPDATED với bidStatus = OUTBID
currentPrice cũng là giá mới, để MyBid row của người bị outbid update luôn.


*/

public class BidController implements SceneDataReceiver<Auction> {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMM d, yyyy, HH:mm", Locale.US);
    private static final String ACTIVE_TAB_STYLE_CLASS = "active-tab";
    private static final String ACTIVE_THUMBNAIL_STYLE_CLASS = "active-thumb";
    private static final String[] HISTORY_BIDDERS = {
        "Mia Tran",
        "Alex Carter",
        "Noah Pham"
    };

    @FXML
    private ImageView productImageView;
    @FXML
    private ImageView thumbnailOneImageView;
    @FXML
    private ImageView thumbnailTwoImageView;
    @FXML
    private ImageView thumbnailThreeImageView;
    @FXML
    private ImageView thumbnailFourImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label longDescriptionLabel;
    @FXML
    private Label currentBidLabel;
    @FXML
    private Label buyNowLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label stepLabel;
    @FXML
    private Label nextBidLabel;
    @FXML
    private Label reserveStatusLabel;
    @FXML
    private Label timePrefixLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField bidAmountField;
    @FXML
    private Button placeBidButton;
    @FXML
    private Button buyNowButton;
    @FXML
    private VBox bidInfoPanel;
    @FXML
    private Label bidInfoStatusLabel;
    @FXML
    private Label bidInfoLockedLabel;
    @FXML
    private Label bidInfoBuyNowDeltaLabel;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label auctionIdLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label historyBidderOneLabel;
    @FXML
    private Label historyAmountOneLabel;
    @FXML
    private Label historyBidderTwoLabel;
    @FXML
    private Label historyAmountTwoLabel;
    @FXML
    private Label historyBidderThreeLabel;
    @FXML
    private Label historyAmountThreeLabel;
    @FXML
    private VBox thumbnailOneFrame;
    @FXML
    private VBox thumbnailTwoFrame;
    @FXML
    private VBox thumbnailThreeFrame;
    @FXML
    private VBox thumbnailFourFrame;
    @FXML
    private Button productInfoTabButton;
    @FXML
    private Button auctionDetailsTabButton;
    @FXML
    private Button bidHistoryTabButton;
    @FXML
    private Button shippingTabButton;
    @FXML
    private HBox productInfoContent;
    @FXML
    private VBox historySection;

    private Auction currentAuction;
    private BigDecimal nextMinimumBid = BigDecimal.ZERO;
    private List<String> currentImageUrls = List.of();
    private final List<BidHistoryItemPayload> recentBidHistory = new ArrayList<>();
    private Runnable onBack;
    private Timeline timeUpdater;
    private RealtimeEventListener<AuctionStatePayload> auctionStateListener;
    private RealtimeEventListener<BidHistoryItemPayload> bidHistoryListener;
    private Long subscribedAuctionId;

    ///không gộp vì lúc initialize thì chưa có auction data
    @FXML
    private void initialize() {
        MoneyInput.install(bidAmountField);
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        selectTab(productInfoTabButton, true);
        setNodeVisibility(bidInfoPanel, false);
    }

    @Override
    public void setSceneData(Auction data) {
        if (data == null) {
            return;
        }

        disposeRealtime();
        currentAuction = data;
        renderAuction(data);
        subscribeAuctionRoom(data.getId());
        subscribeAuctionRealtimeListeners();
    }

    private void renderAuction(Auction data) {
        Product product = data.getProduct();
        BigDecimal currentPrice = valueOrZero(data.getCurrentPrice());
        BigDecimal startingPrice = valueOrZero(data.getStartingPrice());
        BigDecimal bidStep = valueOrZero(data.getMinimumBidStep());
        BigDecimal buyNowPrice = data.getBuyNowPrice();
        nextMinimumBid = calculateNextMinimumBid(currentPrice, bidStep, buyNowPrice, data.getWinnerUserId());

        titleLabel.setText(data.getTitle());
        descriptionLabel.setText(categoryName(product.getCategoryId()));
        longDescriptionLabel.setText(descriptionFor(data));
        updateCurrentBidLabel(data.getWinnerUserId(), currentPrice);
        buyNowLabel.setText(buyNowPrice == null ? "-" : formatCurrency(buyNowPrice));
        startingPriceLabel.setText(formatCurrency(startingPrice));
        stepLabel.setText(formatCurrency(bidStep));
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        updateBidPrompt();
        updateReserveStatus(data);
        updateBidInfo(data);
        startTimeUpdater();
        bidAmountField.clear();
        productNameLabel.setText(product.getTitle());
        auctionIdLabel.setText("#" + data.getId());
        statusLabel.setText(data.getStatus() == null || data.getStatus().isBlank() ? "-" : data.getStatus());
        setBidControlsEnabled(canCurrentUserBid(data));

        loadGalleryImages(product);
        populateBidHistory(data);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Runnable backAction = onBack;
        dispose();
        if (backAction != null) {
            backAction.run();
        }
        else {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Back action is not configured."
            );
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void dispose() {
        disposeRealtime();
        stopTimeUpdater();
        recentBidHistory.clear();
        currentImageUrls = List.of();
        currentAuction = null;
        nextMinimumBid = BigDecimal.ZERO;
        onBack = null;
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentAuction == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing auction", "No auction is loaded for bidding.");
            return;
        }

        BigDecimal enteredBid;
        try {
            enteredBid = MoneyInput.parseRequired(bidAmountField, "Bid amount");
        } catch (IllegalArgumentException exception) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid amount", exception.getMessage());
            bidAmountField.requestFocus();
            return;
        }

        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice != null && enteredBid.compareTo(buyNowPrice) >= 0) {
            if (enteredBid.compareTo(buyingPowerForCurrentAuction()) > 0) {
                showInsufficientBalance(enteredBid);
                return;
            }
            if (confirmBuyNowFromBid(buyNowPrice)) {
                performBuyNow();
            }
            return;
        }

        if (enteredBid.compareTo(nextMinimumBid) < 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Bid too low",
                "Minimum valid bid for this auction is " + formatCurrency(nextMinimumBid) + "."
            );
            bidAmountField.requestFocus();
            return;
        }

        if (enteredBid.compareTo(buyingPowerForCurrentAuction()) > 0) {
            showInsufficientBalance(enteredBid);
            return;
        }

        sendPlaceBid(enteredBid);
    }

    @FXML
    private void handleBuyNow(ActionEvent event) {
        performBuyNow();
    }

    @FXML
    private void handleProxyBid(ActionEvent event) {
        NotificationManager.show(NotificationManager.NotificationType.INFO, "Proxy bid", "Proxy bidding flow is not connected yet.");
    }

    @FXML
    private void handleWatchAsset(ActionEvent event) {
        NotificationManager.show(NotificationManager.NotificationType.INFO, "Watch asset", "Watchlist flow is not connected yet.");
    }

    @FXML
    private void handleThumbnailOneSelected() {
        selectGalleryImage(0);
    }

    @FXML
    private void handleThumbnailTwoSelected() {
        selectGalleryImage(1);
    }

    @FXML
    private void handleThumbnailThreeSelected() {
        selectGalleryImage(2);
    }

    @FXML
    private void handleThumbnailFourSelected() {
        selectGalleryImage(3);
    }

    @FXML
    private void handleProductInformationTab(ActionEvent event) {
        selectTab(productInfoTabButton, true);
    }

    @FXML
    private void handleAuctionDetailsTab(ActionEvent event) {
        selectTab(auctionDetailsTabButton, true);
    }

    @FXML
    private void handleBidHistoryTab(ActionEvent event) {
        selectTab(bidHistoryTabButton, false);
    }

    @FXML
    private void handleShippingTab(ActionEvent event) {
        selectTab(shippingTabButton, true);
    }

    private void subscribeAuctionRoom(long auctionId) {
        try {
            Room room = new Room();
            room.setType(RoomType.AUCTION);
            room.setTargetId(auctionId);
            SocketClient.getClient().sendMessage(new Request<>(RequestType.SUBSCRIBE_ROOM, room));
            subscribedAuctionId = auctionId;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void unsubscribeAuctionRoom() {
        if (subscribedAuctionId == null) {
            return;
        }
        try {
            Room room = new Room();
            room.setType(RoomType.AUCTION);
            room.setTargetId(subscribedAuctionId);
            SocketClient.getClient().sendMessage(new Request<>(RequestType.UNSUBSCRIBE_ROOM, room));
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            subscribedAuctionId = null;
        }
    }

    private void applyAuctionStateUpdate(AuctionStatePayload payload) {
        if (currentAuction == null || payload.getAuctionVersion() <= currentAuction.getVersion()) {
            return;
        }
        BigDecimal currentPrice = valueOrZero(payload.getCurrentPrice());
        boolean ended = isAuctionEnded(payload);
        nextMinimumBid = ended
            ? BigDecimal.ZERO
            : calculateNextMinimumBid(
                currentPrice,
                valueOrZero(currentAuction.getMinimumBidStep()),
                currentAuction.getBuyNowPrice(),
                payload.getWinnerUserId()
            );
        currentAuction = copyAuction(
            payload.getAuctionVersion(),
            payload.getStatus() == null ? currentAuction.getStatus() : payload.getStatus(),
            currentPrice,
            payload.getWinnerUserId(),
            payload.getReserveMet() == null ? currentAuction.getReserveMet() : payload.getReserveMet(),
            payload.getStartingTime() == null ? currentAuction.getStartingTime() : payload.getStartingTime(),
            payload.getEndingTime() == null ? currentAuction.getEndingTime() : payload.getEndingTime()
        );

        updateCurrentBidLabel(currentAuction.getWinnerUserId(), currentPrice);
        nextBidLabel.setText(ended ? "-" : formatCurrency(nextMinimumBid));
        updateBidPrompt();
        updateReserveStatus(currentAuction);
        updateBidInfo(currentAuction);
        statusLabel.setText(currentAuction.getStatus());
        setBidControlsEnabled(!ended && canCurrentUserBid(currentAuction));
        updateTimeState();
    }

    private BigDecimal calculateNextMinimumBid(
            BigDecimal currentPrice,
            BigDecimal minimumBidStep,
            BigDecimal buyNowPrice,
            Long winnerUserId) {
        BigDecimal nextBid = winnerUserId == null
            ? currentPrice
            : currentPrice.add(minimumBidStep);

        if (buyNowPrice != null && nextBid.compareTo(buyNowPrice) > 0) {
            return buyNowPrice;
        }
        return nextBid;
    }

    private boolean isAuctionEnded(AuctionStatePayload payload) {
        return payload.getStateChangeReason() == AuctionStateChangeReason.BUY_NOW
            || payload.getStateChangeReason() == AuctionStateChangeReason.TIME_EXPIRED_ENDED
            || payload.getStateChangeReason() == AuctionStateChangeReason.TIME_EXPIRED_FAILED
            || isClosedStatus(payload.getStatus());
    }

    private void handleAuctionStateEvent(RealtimeEvent<AuctionStatePayload> event) {
        AuctionStatePayload payload = event.getPayload();
        if (payload == null || currentAuction == null || payload.getAuctionId() != currentAuction.getId()) {
            return;
        }
        if (payload.getAuctionVersion() <= currentAuction.getVersion()) {
            return;
        }
        Platform.runLater(() -> applyAuctionStateUpdate(payload));
    }

    private void applyBidHistoryItem(BidHistoryItemPayload payload) {
        if (currentAuction == null || payload.getAuctionVersion() < currentAuction.getVersion()) {
            return;
        }
        recentBidHistory.add(0, payload);
        while (recentBidHistory.size() > 10) {
            recentBidHistory.remove(recentBidHistory.size() - 1);
        }
        renderRecentBidHistory();
    }

    private void handleBidHistoryEvent(RealtimeEvent<BidHistoryItemPayload> event) {
        BidHistoryItemPayload payload = event.getPayload();
        if (payload == null || currentAuction == null || payload.getAuctionId() != currentAuction.getId()) {
            return;
        }
        Platform.runLater(() -> applyBidHistoryItem(payload));
    }

    private void subscribeAuctionRealtimeListeners() {
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();

        auctionStateListener = this::handleAuctionStateEvent;
        bidHistoryListener = this::handleBidHistoryEvent;

        dispatcher.subscribe(RealtimeEventType.AUCTION_STATE_UPDATED, auctionStateListener);
        dispatcher.subscribe(RealtimeEventType.BID_HISTORY_ITEM_ADDED, bidHistoryListener);
    }


    private String descriptionFor(Auction auction) {
        if (auction.getDescription() != null && !auction.getDescription().isBlank()) {
            return auction.getDescription();
        }
        Product product = auction.getProduct();
        return product.getDescription() == null || product.getDescription().isBlank()
            ? "No description is available for this lot."
            : product.getDescription();
    }

    private String categoryName(long categoryId) {
        return switch ((int) categoryId) {
            case 1 -> "Electronics";
            case 2 -> "Collectibles";
            case 3 -> "Arts";
            case 4 -> "Jewelry & Watches";
            default -> "Category #" + categoryId;
        };
    }

    private static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void disposeRealtime() {
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();
        if (auctionStateListener != null) {
            dispatcher.unsubscribe(RealtimeEventType.AUCTION_STATE_UPDATED, auctionStateListener);
            auctionStateListener = null;
        }
        if (bidHistoryListener != null) {
            dispatcher.unsubscribe(RealtimeEventType.BID_HISTORY_ITEM_ADDED, bidHistoryListener);
            bidHistoryListener = null;
        }
        unsubscribeAuctionRoom();
    }

    private Auction copyAuction(
            long version,
            String status,
            BigDecimal currentPrice,
            Long winnerUserId,
            Boolean reserveMet,
            LocalDateTime startingTime,
            LocalDateTime endingTime) {
        return new Auction(
            currentAuction.getId(),
            version,
            currentAuction.getSellerId(),
            currentAuction.getTitle(),
            currentAuction.getDescription(),
            status,
            currentAuction.getStartingPrice(),
            currentPrice,
            currentAuction.getMinimumBidStep(),
            currentAuction.getBuyNowPrice(),
            winnerUserId,
            reserveMet,
            startingTime,
            endingTime,
            currentAuction.getProduct()
        );
    }

    private void setBidControlsEnabled(boolean enabled) {
        bidAmountField.setDisable(!enabled);
        placeBidButton.setDisable(!enabled);
        buyNowButton.setDisable(!enabled || currentAuction == null || currentAuction.getBuyNowPrice() == null);
    }

    private void updateBidPrompt() {
        bidAmountField.setPromptText("Bid " + formatCurrency(nextMinimumBid) + " or more");
    }

    private void updateCurrentBidLabel(Long winnerUserId, BigDecimal currentPrice) {
        currentBidLabel.setText(winnerUserId == null ? "-" : formatCurrency(currentPrice));
    }

    private void updateBidInfo(Auction auction) {
        if (auction == null || bidInfoPanel == null) {
            return;
        }
        Long currentUserId = UserData.getUserId();
        Long winnerUserId = auction.getWinnerUserId();
        if (currentUserId == null
                || winnerUserId == null
                || currentUserId.longValue() == auction.getSellerId()
                || isAuctionUnavailableForBidInfo(auction)) {
            setNodeVisibility(bidInfoPanel, false);
            return;
        }

        boolean leading = currentUserId.longValue() == winnerUserId.longValue();
        setNodeVisibility(bidInfoPanel, true);
        bidInfoStatusLabel.getStyleClass().removeAll("bid-info-leading", "bid-info-outbid");
        if (leading) {
            bidInfoStatusLabel.setText("YOU ARE LEADING");
            bidInfoStatusLabel.getStyleClass().add("bid-info-leading");
            BigDecimal currentCommitment = valueOrZero(auction.getCurrentPrice());
            bidInfoLockedLabel.setText("Current commitment: " + formatCurrency(currentCommitment));
            setNodeVisibility(bidInfoLockedLabel, true);
            BigDecimal buyNowDelta = valueOrZero(auction.getBuyNowPrice()).subtract(currentCommitment).max(BigDecimal.ZERO);
            bidInfoBuyNowDeltaLabel.setText("Buy now requires: +" + formatCurrency(buyNowDelta));
            setNodeVisibility(bidInfoBuyNowDeltaLabel, auction.getBuyNowPrice() != null);
        } else {
            bidInfoStatusLabel.setText("You have been outbid");
            bidInfoStatusLabel.getStyleClass().add("bid-info-outbid");
            bidInfoLockedLabel.setText("Bid " + formatCurrency(nextMinimumBid) + " or more");
            setNodeVisibility(bidInfoLockedLabel, true);
            setNodeVisibility(bidInfoBuyNowDeltaLabel, false);
        }
    }

    private boolean isAuctionUnavailableForBidInfo(Auction auction) {
        if (auction == null || isClosedStatus(auction.getStatus())) {
            return true;
        }
        LocalDateTime endingTime = auction.getEndingTime();
        return endingTime != null && !utcNow().isBefore(endingTime);
    }

    private void updateReserveStatus(Auction auction) {
        reserveStatusLabel.getStyleClass().removeAll(
            "reserve-status-met",
            "reserve-status-not-met",
            "reserve-status-neutral"
        );
        if (auction == null || isNotStarted(auction)) {
            reserveStatusLabel.setText("-");
            reserveStatusLabel.getStyleClass().add("reserve-status-neutral");
            return;
        }
        Boolean reserveMet = auction.getReserveMet();
        if (reserveMet == null) {
            reserveStatusLabel.setText("-");
            reserveStatusLabel.getStyleClass().add("reserve-status-neutral");
        } else if (reserveMet) {
            reserveStatusLabel.setText("Reserve met");
            reserveStatusLabel.getStyleClass().add("reserve-status-met");
        } else {
            reserveStatusLabel.setText("Reserve not met");
            reserveStatusLabel.getStyleClass().add("reserve-status-not-met");
        }
    }

    private boolean isNotStarted(Auction auction) {
        LocalDateTime startingTime = auction.getStartingTime();
        return "SCHEDULED".equals(auction.getStatus())
            && startingTime != null
            && utcNow().isBefore(startingTime);
    }

    private boolean canCurrentUserBid(Auction auction) {
        Long currentUserId = UserData.getUserId();
        return auction != null
            && currentUserId != null
            && "ACTIVE".equals(auction.getStatus())
            && currentUserId.longValue() != auction.getSellerId();
    }

    private void startTimeUpdater() {
        stopTimeUpdater();
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
        if (currentAuction == null) {
            return;
        }
        updateAuctionTimeLabels(currentAuction);
        progressBar.setProgress(calculateProgress(currentAuction));
    }

    private void updateAuctionTimeLabels(Auction auction) {
        String status = auction.getStatus();
        if ("CANCELLED".equals(status)) {
            setTimeLabels("", "Cancelled");
            return;
        }
        if ("FAILED".equals(status)) {
            setTimeLabels("", "Failed");
            return;
        }
        if ("ENDED".equals(status)) {
            setTimeLabels("", "Ended");
            return;
        }

        LocalDateTime now = utcNow();
        LocalDateTime startingTime = auction.getStartingTime();
        LocalDateTime endingTime = auction.getEndingTime();
        if (endingTime == null || !now.isBefore(endingTime)) {
            setTimeLabels("", "Ended");
            return;
        }
        if ("SCHEDULED".equals(status) && startingTime != null && now.isBefore(startingTime)) {
            Duration remainingUntilStart = Duration.between(now, startingTime);
            if (remainingUntilStart.compareTo(Duration.ofHours(24)) >= 0) {
                setTimeLabels("Starts:", formatVietnamTime(startingTime));
            } else {
                setTimeLabels("Starts in", formatRemainingDuration(remainingUntilStart));
            }
            return;
        }

        Duration remainingUntilEnd = Duration.between(now, endingTime);
        if (remainingUntilEnd.compareTo(Duration.ofHours(24)) >= 0) {
            setTimeLabels("Ends:", formatVietnamTime(endingTime));
        } else {
            setTimeLabels("Ends in", formatRemainingDuration(remainingUntilEnd));
        }
    }

    private void setTimeLabels(String prefix, String value) {
        boolean hasPrefix = prefix != null && !prefix.isBlank();
        timePrefixLabel.setText(hasPrefix ? prefix : "");
        timePrefixLabel.setManaged(hasPrefix);
        timePrefixLabel.setVisible(hasPrefix);
        timeLabel.setText(value);
    }

    private String formatRemainingDuration(Duration remaining) {
        long seconds = Math.max(0, remaining.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private String formatVietnamTime(LocalDateTime utcTime) {
        return utcTime
            .atZone(UTC_ZONE)
            .withZoneSameInstant(VIETNAM_ZONE)
            .format(DISPLAY_TIME_FORMATTER);
    }

    private double calculateProgress(Auction auction) {
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

    private boolean isClosedStatus(String status) {
        return "ENDED".equals(status) || "FAILED".equals(status) || "SOLD".equals(status) || "CANCELLED".equals(status);
    }

    private LocalDateTime utcNow() {
        return LocalDateTime.now(UTC_ZONE);
    }

    private void populateBidHistory(Auction data) {
        recentBidHistory.clear();
        BigDecimal currentBid = valueOrZero(data.getCurrentPrice());
        BigDecimal step = valueOrZero(data.getMinimumBidStep());
        BigDecimal startingBid = valueOrZero(data.getStartingPrice());

        setHistoryRow(historyBidderOneLabel, historyAmountOneLabel, HISTORY_BIDDERS[0], currentBid);
        setHistoryRow(historyBidderTwoLabel, historyAmountTwoLabel, HISTORY_BIDDERS[1], currentBid.subtract(step).max(startingBid));
        setHistoryRow(
            historyBidderThreeLabel,
            historyAmountThreeLabel,
            HISTORY_BIDDERS[2],
            currentBid.subtract(step.multiply(BigDecimal.valueOf(2))).max(startingBid)
        );
    }

    private void renderRecentBidHistory() {
        setHistoryRow(
            historyBidderOneLabel,
            historyAmountOneLabel,
            bidderName(recentBidHistory, 0),
            bidAmount(recentBidHistory, 0)
        );
        setHistoryRow(
            historyBidderTwoLabel,
            historyAmountTwoLabel,
            bidderName(recentBidHistory, 1),
            bidAmount(recentBidHistory, 1)
        );
        setHistoryRow(
            historyBidderThreeLabel,
            historyAmountThreeLabel,
            bidderName(recentBidHistory, 2),
            bidAmount(recentBidHistory, 2)
        );
    }

    private String bidderName(List<BidHistoryItemPayload> items, int index) {
        if (index >= items.size()) {
            return "-";
        }
        BidHistoryItemPayload item = items.get(index);
        if (item.getBidderDisplayName() != null && !item.getBidderDisplayName().isBlank()) {
            return item.getBidderDisplayName();
        }
        return item.getBidderId() == null ? "Bidder" : "User #" + item.getBidderId();
    }

    private BigDecimal bidAmount(List<BidHistoryItemPayload> items, int index) {
        if (index >= items.size()) {
            return BigDecimal.ZERO;
        }
        return valueOrZero(items.get(index).getBidAmount());
    }

    private void setHistoryRow(Label bidderLabel, Label amountLabel, String bidder, BigDecimal amount) {
        bidderLabel.setText(bidder);
        amountLabel.setText(formatCurrency(amount));
    }

    private void loadGalleryImages(Product product) {
        List<String> imageUrls = new ArrayList<>();
        if (product.getImageUrls() != null) {
            imageUrls.addAll(product.getImageUrls().stream()
                .filter(url -> url != null && !url.isBlank())
                .toList());
        }
        if (imageUrls.isEmpty() && product.getImagePath() != null && !product.getImagePath().isBlank()) {
            imageUrls.add(product.getImagePath());
        }

        currentImageUrls = List.copyOf(imageUrls);
        selectGalleryImage(0);
        configureThumbnail(thumbnailOneFrame, thumbnailOneImageView, 0);
        configureThumbnail(thumbnailTwoFrame, thumbnailTwoImageView, 1);
        configureThumbnail(thumbnailThreeFrame, thumbnailThreeImageView, 2);
        configureThumbnail(thumbnailFourFrame, thumbnailFourImageView, 3);
    }

    private void configureThumbnail(VBox frame, ImageView imageView, int imageIndex) {
        boolean hasImage = imageIndex < currentImageUrls.size();
        setNodeVisibility(frame, hasImage);
        if (hasImage) {
            ProductImageLoader.loadCover(imageView, currentImageUrls.get(imageIndex));
        }
    }

    private void selectGalleryImage(int imageIndex) {
        if (currentImageUrls.isEmpty() || imageIndex < 0 || imageIndex >= currentImageUrls.size()) {
            return;
        }
        ProductImageLoader.loadCover(productImageView, currentImageUrls.get(imageIndex));
        setActiveThumbnail(thumbnailOneFrame, imageIndex == 0);
        setActiveThumbnail(thumbnailTwoFrame, imageIndex == 1);
        setActiveThumbnail(thumbnailThreeFrame, imageIndex == 2);
        setActiveThumbnail(thumbnailFourFrame, imageIndex == 3);
    }

    private void setActiveThumbnail(VBox frame, boolean active) {
        if (frame == null) {
            return;
        }
        frame.getStyleClass().remove(ACTIVE_THUMBNAIL_STYLE_CLASS);
        if (active) {
            frame.getStyleClass().add(ACTIVE_THUMBNAIL_STYLE_CLASS);
        }
    }

    private void selectTab(Button activeTab, boolean showProductInfo) {
        setActiveTab(productInfoTabButton, activeTab == productInfoTabButton);
        setActiveTab(auctionDetailsTabButton, activeTab == auctionDetailsTabButton);
        setActiveTab(bidHistoryTabButton, activeTab == bidHistoryTabButton);
        setActiveTab(shippingTabButton, activeTab == shippingTabButton);
        setNodeVisibility(productInfoContent, showProductInfo);
        setNodeVisibility(historySection, !showProductInfo);
    }

    private void setActiveTab(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove(ACTIVE_TAB_STYLE_CLASS);
        if (active) {
            button.getStyleClass().add(ACTIVE_TAB_STYLE_CLASS);
        }
    }

    private void setNodeVisibility(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private boolean confirmBuyNowFromBid(BigDecimal buyNowPrice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("VBay");
        alert.setHeaderText("Bid reaches Buy Now price");
        alert.setContentText("Your bid is at least the Buy Now price (" + formatCurrency(buyNowPrice) + "). Do you want to buy the product now?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private void sendPlaceBid(BigDecimal bidAmount) {
        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.PLACE_BID, new PlaceBidRequest(currentAuction.getId(), bidAmount))
            );
            if (response == null || !response.isStatus()) {
                NotificationManager.show(NotificationManager.NotificationType.ERROR, "Bid failed", response != null ? response.getMessage() : "No response from server.");
                return;
            }
            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Bid placed", "Your bid was placed successfully.");
        } catch (IOException exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Bid failed", exception.getMessage());
        }
    }

    private void performBuyNow() {
        if (currentAuction == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing auction", "No auction is loaded.");
            return;
        }
        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Buy Now unavailable", "This auction does not have a Buy Now price.");
            return;
        }
        if (buyNowPrice.compareTo(buyingPowerForCurrentAuction()) > 0) {
            showInsufficientBalance(buyNowPrice);
            return;
        }

        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.BUY_NOW, new BuyNowRequest(currentAuction.getId()))
            );
            if (response == null || !response.isStatus()) {
                NotificationManager.show(NotificationManager.NotificationType.ERROR, "Buy Now failed", response != null ? response.getMessage() : "No response from server.");
                return;
            }
            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Buy Now complete", "You bought " + currentAuction.getTitle() + ".");
        } catch (IOException exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Buy Now failed", exception.getMessage());
        }
    }

    private void showInsufficientBalance(BigDecimal amount) {
        NotificationManager.show(
            NotificationManager.NotificationType.WARNING,
            "Insufficient balance",
            "Buying power for this auction is " + formatCurrency(buyingPowerForCurrentAuction())
                + ", but this action requires " + formatCurrency(amount) + "."
        );
    }

    private BigDecimal availableBalance() {
        return valueOrZero(UserData.getAvailableBalance());
    }

    private BigDecimal buyingPowerForCurrentAuction() {
        BigDecimal buyingPower = availableBalance();
        if (currentAuction == null) {
            return buyingPower;
        }
        Long currentUserId = UserData.getUserId();
        Long winnerUserId = currentAuction.getWinnerUserId();
        if (currentUserId != null && winnerUserId != null && currentUserId.longValue() == winnerUserId.longValue()) {
            buyingPower = buyingPower.add(valueOrZero(currentAuction.getCurrentPrice()));
        }
        return buyingPower;
    }
}
    
