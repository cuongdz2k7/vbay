package com.vbay.ui.scene_ui.controller.bid;

import java.math.BigDecimal;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.vbay.network.SocketClient;
import com.vbay.network.dispatcher.RealtimeEventDispatcher;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionEndedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStateUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BidController implements SceneDataReceiver<Auction> {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final String ACTIVE_TAB_STYLE_CLASS = "active-tab";
    private static final String ACTIVE_THUMBNAIL_STYLE_CLASS = "active-thumb";
    private static final BigDecimal AVAILABLE_BALANCE = new BigDecimal("42500.00");
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
    private RealtimeEventListener<AuctionStateUpdatedPayload> auctionStateListener;
    private RealtimeEventListener<BidHistoryItemPayload> bidHistoryListener;
    private RealtimeEventListener<AuctionEndedPayload> auctionEndedListener;
    private Long subscribedAuctionId;

    @FXML
    private void initialize() {
        MoneyInput.install(bidAmountField);
        selectTab(productInfoTabButton, true);
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
        nextMinimumBid = currentPrice.add(bidStep);

        titleLabel.setText(data.getTitle());
        descriptionLabel.setText(descriptionFor(data));
        longDescriptionLabel.setText(descriptionFor(data));
        currentBidLabel.setText(formatCurrency(currentPrice));
        buyNowLabel.setText(buyNowPrice == null ? "-" : formatCurrency(buyNowPrice));
        startingPriceLabel.setText(formatCurrency(startingPrice));
        stepLabel.setText(formatCurrency(bidStep));
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        startTimeUpdater();
        bidAmountField.setText(MoneyInput.toInputText(nextMinimumBid));
        productNameLabel.setText(product.getTitle());
        auctionIdLabel.setText("#" + data.getId());
        statusLabel.setText(data.getStatus() == null || data.getStatus().isBlank() ? "-" : data.getStatus());
        setBidControlsEnabled(!isClosedStatus(data.getStatus()));

        loadGalleryImages(product);
        populateBidHistory(data);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        disposeRealtime();
        stopTimeUpdater();
        if (onBack != null) {
            onBack.run();
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void dispose() {
        disposeRealtime();
        stopTimeUpdater();
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentAuction == null) {
            showMessage(Alert.AlertType.WARNING, "Missing auction", "No auction is loaded for bidding.");
            return;
        }

        BigDecimal enteredBid;
        try {
            enteredBid = MoneyInput.parseRequired(bidAmountField, "Bid amount");
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, "Invalid amount", exception.getMessage());
            bidAmountField.requestFocus();
            return;
        }

        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice != null && enteredBid.compareTo(buyNowPrice) >= 0) {
            if (enteredBid.compareTo(AVAILABLE_BALANCE) > 0) {
                showInsufficientBalance(enteredBid);
                return;
            }
            if (confirmBuyNowFromBid(buyNowPrice)) {
                performBuyNow();
            }
            return;
        }

        if (enteredBid.compareTo(nextMinimumBid) < 0) {
            showMessage(
                Alert.AlertType.WARNING,
                "Bid too low",
                "Minimum valid bid for this auction is " + formatCurrency(nextMinimumBid) + "."
            );
            bidAmountField.requestFocus();
            return;
        }

        if (enteredBid.compareTo(AVAILABLE_BALANCE) > 0) {
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
        showMessage(Alert.AlertType.INFORMATION, "Proxy bid", "Proxy bidding flow is not connected yet.");
    }

    @FXML
    private void handleWatchAsset(ActionEvent event) {
        showMessage(Alert.AlertType.INFORMATION, "Watch asset", "Watchlist flow is not connected yet.");
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

    private String descriptionFor(Auction auction) {
        if (auction.getDescription() != null && !auction.getDescription().isBlank()) {
            return auction.getDescription();
        }
        Product product = auction.getProduct();
        return product.getDescription() == null || product.getDescription().isBlank()
            ? "No description is available for this lot."
            : product.getDescription();
    }

    private static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private void subscribeAuctionRealtimeListeners() {
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();

        auctionStateListener = event -> {
            AuctionStateUpdatedPayload payload = event.getPayload();
            if (payload == null || currentAuction == null || payload.getAuctionId() != currentAuction.getId()) {
                return;
            }
            Platform.runLater(() -> applyAuctionStateUpdate(payload));
        };
        bidHistoryListener = event -> {
            BidHistoryItemPayload payload = event.getPayload();
            if (payload == null || currentAuction == null || payload.getAuctionId() != currentAuction.getId()) {
                return;
            }
            Platform.runLater(() -> applyBidHistoryItem(payload));
        };
        auctionEndedListener = event -> {
            AuctionEndedPayload payload = event.getPayload();
            if (payload == null || currentAuction == null || payload.getAuctionId() != currentAuction.getId()) {
                return;
            }
            Platform.runLater(() -> applyAuctionEnded(payload));
        };

        dispatcher.subscribe(RealtimeEventType.AUCTION_STATE_UPDATED, auctionStateListener);
        dispatcher.subscribe(RealtimeEventType.BID_HISTORY_ITEM_ADDED, bidHistoryListener);
        dispatcher.subscribe(RealtimeEventType.AUCTION_ENDED, auctionEndedListener);
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
        if (auctionEndedListener != null) {
            dispatcher.unsubscribe(RealtimeEventType.AUCTION_ENDED, auctionEndedListener);
            auctionEndedListener = null;
        }
        unsubscribeAuctionRoom();
    }

    private void applyAuctionStateUpdate(AuctionStateUpdatedPayload payload) {
        BigDecimal currentPrice = valueOrZero(payload.getCurrentPrice());
        nextMinimumBid = payload.getNextMinimumBid() == null
            ? currentPrice.add(valueOrZero(currentAuction.getMinimumBidStep()))
            : payload.getNextMinimumBid();
        currentAuction = copyAuction(
            payload.getAuctionVersion(),
            payload.getStatus() == null ? currentAuction.getStatus() : payload.getStatus(),
            currentPrice,
            payload.getStartingTime() == null ? currentAuction.getStartingTime() : payload.getStartingTime(),
            payload.getEndingTime() == null ? currentAuction.getEndingTime() : payload.getEndingTime()
        );

        currentBidLabel.setText(formatCurrency(currentPrice));
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        bidAmountField.setText(MoneyInput.toInputText(nextMinimumBid));
        statusLabel.setText(currentAuction.getStatus());
        updateTimeState();
    }

    private void applyBidHistoryItem(BidHistoryItemPayload payload) {
        recentBidHistory.add(0, payload);
        while (recentBidHistory.size() > 3) {
            recentBidHistory.remove(recentBidHistory.size() - 1);
        }
        renderRecentBidHistory();
    }

    private void applyAuctionEnded(AuctionEndedPayload payload) {
        BigDecimal finalPrice = valueOrZero(payload.getFinalPrice());
        currentAuction = copyAuction(
            payload.getAuctionVersion(),
            payload.getStatus() == null ? "ENDED" : payload.getStatus(),
            finalPrice,
            currentAuction.getStartingTime(),
            currentAuction.getEndingTime()
        );
        currentBidLabel.setText(formatCurrency(finalPrice));
        statusLabel.setText(currentAuction.getStatus());
        nextBidLabel.setText("-");
        setBidControlsEnabled(false);
        updateTimeState();
    }

    private Auction copyAuction(
            long version,
            String status,
            BigDecimal currentPrice,
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

    private boolean isClosedStatus(String status) {
        return "ENDED".equals(status) || "FAILED".equals(status) || "SOLD".equals(status) || "CANCELLED".equals(status);
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
        progressBar.setProgress(calculateProgress(currentAuction.getStartingTime(), currentAuction.getEndingTime()));
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
            setTimeLabels("Starts in", formatDuration(Duration.between(now, startingTime)));
            return;
        }
        setTimeLabels("Ends in", formatDuration(Duration.between(now, endingTime)));
    }

    private void setTimeLabels(String prefix, String value) {
        boolean hasPrefix = prefix != null && !prefix.isBlank();
        timePrefixLabel.setText(hasPrefix ? prefix : "");
        timePrefixLabel.setManaged(hasPrefix);
        timePrefixLabel.setVisible(hasPrefix);
        timeLabel.setText(value);
    }

    private String formatDuration(Duration remaining) {
        long totalMinutes = remaining.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }

    private double calculateProgress(LocalDateTime startingTime, LocalDateTime endingTime) {
        if (startingTime == null || endingTime == null) {
            return 0.0;
        }

        long totalMillis = Duration.between(startingTime, endingTime).toMillis();
        if (totalMillis <= 0) {
            return 1.0;
        }

        long remainingMillis = Duration.between(utcNow(), endingTime).toMillis();
        double progress = (double) remainingMillis / totalMillis;
        return Math.max(0.0, Math.min(1.0, progress));
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
                showMessage(Alert.AlertType.ERROR, "Bid failed", response != null ? response.getMessage() : "No response from server.");
                return;
            }
            showMessage(Alert.AlertType.INFORMATION, "Bid placed", "Your bid was placed successfully.");
        } catch (IOException exception) {
            showMessage(Alert.AlertType.ERROR, "Bid failed", exception.getMessage());
        }
    }

    private void performBuyNow() {
        if (currentAuction == null) {
            showMessage(Alert.AlertType.WARNING, "Missing auction", "No auction is loaded.");
            return;
        }
        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice == null) {
            showMessage(Alert.AlertType.WARNING, "Buy Now unavailable", "This auction does not have a Buy Now price.");
            return;
        }
        if (buyNowPrice.compareTo(AVAILABLE_BALANCE) > 0) {
            showInsufficientBalance(buyNowPrice);
            return;
        }

        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.BUY_NOW, new BuyNowRequest(currentAuction.getId()))
            );
            if (response == null || !response.isStatus()) {
                showMessage(Alert.AlertType.ERROR, "Buy Now failed", response != null ? response.getMessage() : "No response from server.");
                return;
            }
            showMessage(Alert.AlertType.INFORMATION, "Buy Now complete", "You bought " + currentAuction.getTitle() + ".");
        } catch (IOException exception) {
            showMessage(Alert.AlertType.ERROR, "Buy Now failed", exception.getMessage());
        }
    }

    private void showInsufficientBalance(BigDecimal amount) {
        showMessage(
            Alert.AlertType.WARNING,
            "Insufficient balance",
            "Available balance is " + formatCurrency(AVAILABLE_BALANCE) + ", but this action requires " + formatCurrency(amount) + "."
        );
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
