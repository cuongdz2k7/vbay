package com.vbay.ui.scene_ui.controller.bid;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import javafx.util.Duration;
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
import com.vbay.shared.dto.realtimeDTO.payload.AutobidUpdatedPayload;
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
import com.vbay.ui.util.BidPriceChart;
import com.vbay.shared.dto.auctionDTO.AuctionDetailRequest;
import com.vbay.shared.Utils.JsonUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.scene.shape.Circle;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;


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
    private Label currentBidTitleLabel;
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
    private Button proxyBidButton;
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
    private StackPane thumbnailOneFrame;
    @FXML
    private StackPane thumbnailTwoFrame;
    @FXML
    private StackPane thumbnailThreeFrame;
    @FXML
    private StackPane thumbnailFourFrame;
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
    @FXML
    private StackPane proxyBidOverlay;
    @FXML
    private Label sellerUsernameLabel;
    @FXML
    private Label sellerEmailLabel;
    @FXML
    private Label proxyBidTitleLabel;
    @FXML
    private Label proxyCurrentBidLabel;
    @FXML
    private Label proxyMinimumTextLabel;
    @FXML
    private Label proxyMinimumLabel;
    @FXML
    private HBox proxyCurrentMaxRow;
    @FXML
    private Label proxyCurrentMaxLabel;
    @FXML
    private TextField proxyBidAmountField;
    @FXML
    private Label proxyHintLabel;
    @FXML
    private Button proxyConfirmButton;

    @FXML
    private GridPane analyticsPanel;
    @FXML
    private Circle pulseCircle;
    @FXML
    private StackPane chartCanvasContainer;
    @FXML
    private ScrollPane historyScrollPane;
    @FXML
    private VBox historyListContainer;

    private BidPriceChart priceChart;
    private boolean isWatching = false;

    private Auction currentAuction;
    private BigDecimal nextMinimumBid = BigDecimal.ZERO;
    private List<String> currentImageUrls = List.of();
    private final List<BidHistoryItemPayload> recentBidHistory = new ArrayList<>();
    private Runnable onBack;
    private Timeline timeUpdater;
    private RealtimeEventListener<AuctionStatePayload> auctionStateListener;
    private RealtimeEventListener<BidHistoryItemPayload> bidHistoryListener;
    private RealtimeEventListener<AutobidUpdatedPayload> autobidUpdatedListener;
    private Long subscribedAuctionId;
    private Long viewerAutobidId;
    private BigDecimal viewerMaxBidAmount;
    private String viewerAutobidStatus;
    private boolean viewerAutobidWinning;
    private boolean viewerShowActiveMaxBid;
    private LocalDateTime viewerBidStateUpdatedAt;
    private ProxyBidMode proxyBidMode = ProxyBidMode.REGISTER;

    private enum ProxyBidMode {
        REGISTER,
        INCREASE
    }

    ///không gộp vì lúc initialize thì chưa có auction data
    @FXML
    private void initialize() {
        MoneyInput.install(bidAmountField);
        MoneyInput.install(proxyBidAmountField);
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        applyRoundedClip(productImageView, 560, 352, 14);
        applyRoundedClip(thumbnailOneImageView, 128, 82, 10);
        applyRoundedClip(thumbnailTwoImageView, 128, 82, 10);
        applyRoundedClip(thumbnailThreeImageView, 128, 82, 10);
        applyRoundedClip(thumbnailFourImageView, 128, 82, 10);
        selectTab(productInfoTabButton, true);
        setNodeVisibility(bidInfoPanel, false);
        setNodeVisibility(proxyBidOverlay, false);

        // Initialize priceChart and add to container
        priceChart = new BidPriceChart();
        chartCanvasContainer.getChildren().add(priceChart);

        // Start pulsing status animation
        startPulseAnimation();
    }

    private void applyRoundedClip(ImageView imageView, double width, double height, double arc) {
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        imageView.setClip(clip);
    }

    @Override
    public void setSceneData(Auction data) {
        if (data == null) {
            return;
        }

        disposeRealtime();
        currentAuction = data;
        applyViewerBidState(data);
        renderAuction(data);

        // Fetch bid history from server first
        fetchBidHistory(data.getId());

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
        titleLabel.setWrapText(true);
        if (sellerUsernameLabel != null) {
            String sellerUser = data.getSellerUsername();
            sellerUsernameLabel.setText("Seller: " + (sellerUser == null || sellerUser.isBlank() ? "Seller" : sellerUser));
        }
        if (sellerEmailLabel != null) {
            String sellerMail = data.getSellerEmail();
            sellerEmailLabel.setText(sellerMail == null || sellerMail.isBlank() ? "Email not available" : sellerMail);
        }
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
        clearViewerBidState();
        setNodeVisibility(proxyBidOverlay, false);

        isWatching = false;
        if (analyticsPanel != null) {
            analyticsPanel.setVisible(false);
            analyticsPanel.setManaged(false);
        }

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
                performBuyNow(false);
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
        performBuyNow(true);
    }

    @FXML
    private void handleProxyBid(ActionEvent event) {
        if (!canCurrentUserBid(currentAuction)) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Proxy bid unavailable", proxyUnavailableMessage());
            return;
        }
        if (hasActiveWinningAutobid()) {
            openIncreaseProxyBidModal();
        } else {
            openRegisterProxyBidModal();
        }
    }

    @FXML
    private void handleProxyBidCancel(ActionEvent event) {
        closeProxyBidModal();
    }

    @FXML
    private void handleProxyBidConfirm(ActionEvent event) {
        if (currentAuction == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing auction", "No auction is loaded.");
            return;
        }

        BigDecimal maxBidAmount;
        try {
            maxBidAmount = MoneyInput.parseRequired(proxyBidAmountField, "Max proxy bid");
        } catch (IllegalArgumentException exception) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid amount", exception.getMessage());
            proxyBidAmountField.requestFocus();
            return;
        }

        if (!validateProxyBidAmount(maxBidAmount)) {
            proxyBidAmountField.requestFocus();
            return;
        }

        if (proxyBidMode == ProxyBidMode.INCREASE) {
            sendIncreaseProxyBid(maxBidAmount);
        } else {
            sendRegisterProxyBid(maxBidAmount);
        }
    }

    @FXML
    private void handleWatchAsset(ActionEvent event) {
        if (currentAuction == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "No auction", "No active auction to watch.");
            return;
        }

        isWatching = !isWatching;
        if (isWatching) {
            fetchBidHistory(currentAuction.getId());
            priceChart.setBids(recentBidHistory);
            renderAnalyticsBidHistory();

            analyticsPanel.setVisible(true);
            analyticsPanel.setManaged(true);
            analyticsPanel.setOpacity(0.0);
            analyticsPanel.setTranslateY(-30);

            FadeTransition fade = new FadeTransition(Duration.millis(500), analyticsPanel);
            fade.setToValue(1.0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(500), analyticsPanel);
            slide.setToY(0);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.play();
        } else {
            FadeTransition fade = new FadeTransition(Duration.millis(350), analyticsPanel);
            fade.setToValue(0.0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(350), analyticsPanel);
            slide.setToY(-30);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.setOnFinished(e -> {
                analyticsPanel.setVisible(false);
                analyticsPanel.setManaged(false);
            });
            pt.play();
        }
    }

    private void startPulseAnimation() {
        if (pulseCircle == null) return;

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1), pulseCircle);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.4);
        scale.setToY(1.4);
        scale.setCycleCount(Animation.INDEFINITE);
        scale.setAutoReverse(true);

        FadeTransition fade = new FadeTransition(Duration.seconds(1), pulseCircle);
        fade.setFromValue(1.0);
        fade.setToValue(0.4);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);

        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.play();
    }

    private void renderAnalyticsBidHistory() {
        historyListContainer.getChildren().clear();
        for (int i = 0; i < recentBidHistory.size(); i++) {
            BidHistoryItemPayload bid = recentBidHistory.get(i);
            boolean isTop = (i == 0);
            historyListContainer.getChildren().add(createBidHistoryRowNode(bid, isTop));
        }
    }

    private Node createBidHistoryRowNode(BidHistoryItemPayload bid, boolean isTop) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);

        row.getStyleClass().add("history-row-item");
        if (isTop) {
            row.getStyleClass().add("top-bid-row");
        }

        VBox userBox = new VBox();
        userBox.setSpacing(2);

        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setSpacing(6);

        String displayName = bid.getBidderDisplayName();
        if (bid.getBidderId() != null && bid.getBidderId().equals(UserData.getUserId())) {
            displayName = UserData.getUsername();
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = bid.getBidderId() != null ? "User #" + bid.getBidderId() : "Bidder";
        }
        Label nameLabel = new Label(maskUsername(displayName));
        nameLabel.getStyleClass().add("bid-history-username");
        if (isTop) {
            nameLabel.getStyleClass().add("bid-history-username-top");
        }
        nameBox.getChildren().add(nameLabel);

        if (isTop) {
            Label topBadge = new Label("TOP");
            topBadge.getStyleClass().add("top-badge-label");
            nameBox.getChildren().add(topBadge);
        }

        Label timeLabel = new Label(formatBidTime(bid.getBidTime()));
        timeLabel.getStyleClass().add("bid-history-time");

        userBox.getChildren().addAll(nameBox, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox amountBox = new HBox();
        amountBox.setAlignment(Pos.CENTER_LEFT);
        amountBox.setSpacing(6);

        Label amountLabel = new Label(formatCurrency(valueOrZero(bid.getBidAmount())));
        amountLabel.getStyleClass().add("bid-history-amount");
        if (isTop) {
            amountLabel.getStyleClass().add("bid-history-amount-top");
        }
        amountBox.getChildren().add(amountLabel);

        if ("AUTO_BID".equals(bid.getBidSource())) {
            Label autoTag = new Label("(Auto)");
            autoTag.getStyleClass().add("autobid-tag-label");
            amountBox.getChildren().add(autoTag);
        }

        row.getChildren().addAll(userBox, spacer, amountBox);
        return row;
    }

    private String maskUsername(String name) {
        if (name == null || name.isBlank()) return "Anonymous";
        if (name.length() <= 2) return name.charAt(0) + "***";
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }

    private String formatBidTime(LocalDateTime bidTime) {
        if (bidTime == null) return "Just now";
        java.time.Duration duration = java.time.Duration.between(bidTime, utcNow());
        long seconds = Math.max(0, duration.getSeconds());
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h ago";
        } else {
            return bidTime.format(DateTimeFormatter.ofPattern("HH:mm, MMM d", Locale.US));
        }
    }

    private void fetchBidHistory(long auctionId) {
        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.GET_BID_HISTORY, new AuctionDetailRequest(auctionId))
            );
            if (response != null && response.isStatus()) {
                BidHistoryItemPayload[] bids = JsonUtils.fromJson(
                    JsonUtils.toJson(response.getData()),
                    BidHistoryItemPayload[].class
                );
                recentBidHistory.clear();
                if (bids != null) {
                    for (BidHistoryItemPayload bid : bids) {
                        recentBidHistory.add(bid);
                    }
                }

                renderRecentBidHistory();

                if (isWatching) {
                    priceChart.setBids(recentBidHistory);
                    renderAnalyticsBidHistory();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
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
            payload.isAntiSnipeExtended(),
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
        while (recentBidHistory.size() > 15) {
            recentBidHistory.remove(recentBidHistory.size() - 1);
        }
        renderRecentBidHistory();

        // Sync with Watch Asset Analytics Panel
        if (isWatching) {
            priceChart.addBid(payload);
            renderAnalyticsBidHistory();
            Platform.runLater(() -> historyScrollPane.setVvalue(0.0));
        }
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
        autobidUpdatedListener = this::handleAutobidUpdatedEvent;

        dispatcher.subscribe(RealtimeEventType.AUCTION_STATE_UPDATED, auctionStateListener);
        dispatcher.subscribe(RealtimeEventType.BID_HISTORY_ITEM_ADDED, bidHistoryListener);
        dispatcher.subscribe(RealtimeEventType.AUTOBID_UPDATED, autobidUpdatedListener);
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
        if (autobidUpdatedListener != null) {
            dispatcher.unsubscribe(RealtimeEventType.AUTOBID_UPDATED, autobidUpdatedListener);
            autobidUpdatedListener = null;
        }
        unsubscribeAuctionRoom();
    }

    private Auction copyAuction(
            long version,
            String status,
            BigDecimal currentPrice,
            Long winnerUserId,
            Boolean reserveMet,
            boolean antiSnipeExtended,
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
            antiSnipeExtended || currentAuction.isAntiSnipeExtended(),
            startingTime,
            endingTime,
            currentAuction.getProduct(),
            viewerAutobidId,
            viewerMaxBidAmount,
            viewerAutobidStatus,
            viewerAutobidWinning,
            viewerShowActiveMaxBid,
            viewerBidStateUpdatedAt
        );
    }

    private void setBidControlsEnabled(boolean enabled) {
        bidAmountField.setDisable(!enabled);
        placeBidButton.setDisable(!enabled);
        buyNowButton.setDisable(!enabled || currentAuction == null || currentAuction.getBuyNowPrice() == null);
        proxyBidButton.setDisable(!enabled);
        updateProxyBidButton();
    }

    private void updateBidPrompt() {
        bidAmountField.setPromptText("Bid " + formatCurrency(nextMinimumBid) + " or more");
    }

    private void updateCurrentBidLabel(Long winnerUserId, BigDecimal currentPrice) {
        currentBidLabel.setText(winnerUserId == null ? "-" : formatCurrency(currentPrice));
        if (currentBidTitleLabel != null) {
            boolean isEnded = false;
            if (currentAuction != null) {
                if (isClosedStatus(currentAuction.getStatus())) {
                    isEnded = true;
                } else {
                    LocalDateTime endingTime = currentAuction.getEndingTime();
                    if (endingTime != null && !utcNow().isBefore(endingTime)) {
                        isEnded = true;
                    }
                }
            }
            if (isEnded) {
                currentBidTitleLabel.setText("FINAL PRICE");
            } else {
                currentBidTitleLabel.setText("CURRENT BID");
            }
        }
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
            if (viewerShowActiveMaxBid && viewerMaxBidAmount != null) {
                bidInfoLockedLabel.setText("Your max bid: " + formatCurrency(viewerMaxBidAmount));
            } else {
                bidInfoLockedLabel.setText("Current commitment: " + formatCurrency(currentCommitment));
            }
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
        timeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeState()));
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
        if (currentBidTitleLabel != null) {
            boolean isEnded = isClosedStatus(currentAuction.getStatus());
            if (!isEnded) {
                LocalDateTime endingTime = currentAuction.getEndingTime();
                if (endingTime != null && !utcNow().isBefore(endingTime)) {
                    isEnded = true;
                }
            }
            if (isEnded) {
                currentBidTitleLabel.setText("FINAL PRICE");
            } else {
                currentBidTitleLabel.setText("CURRENT BID");
            }
        }
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
            java.time.Duration remainingUntilStart = java.time.Duration.between(now, startingTime);
            if (remainingUntilStart.compareTo(java.time.Duration.ofHours(24)) >= 0) {
                setTimeLabels("Starts:", formatVietnamTime(startingTime));
            } else {
                setTimeLabels("Starts in", formatRemainingDuration(remainingUntilStart));
            }
            return;
        }

        java.time.Duration remainingUntilEnd = java.time.Duration.between(now, endingTime);
        if (remainingUntilEnd.compareTo(java.time.Duration.ofHours(24)) >= 0) {
            setTimeLabels(auction.isAntiSnipeExtended() ? "Extended:" : "Ends:", formatVietnamTime(endingTime));
        } else {
            setTimeLabels(auction.isAntiSnipeExtended() ? "Extended" : "Ends in", formatRemainingDuration(remainingUntilEnd));
        }
    }

    private void setTimeLabels(String prefix, String value) {
        boolean hasPrefix = prefix != null && !prefix.isBlank();
        timePrefixLabel.setText(hasPrefix ? prefix : "");
        timePrefixLabel.setManaged(hasPrefix);
        timePrefixLabel.setVisible(hasPrefix);
        timeLabel.setText(value);
    }

    private String formatRemainingDuration(java.time.Duration remaining) {
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

        long totalMillis = java.time.Duration.between(startingTime, endingTime).toMillis();
        if (totalMillis <= 0) {
            return 1.0;
        }

        long remainingMillis = java.time.Duration.between(now, endingTime).toMillis();
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

    private void configureThumbnail(StackPane frame, ImageView imageView, int imageIndex) {
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

    private void setActiveThumbnail(StackPane frame, boolean active) {
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

    private void setNodeVisibility(Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void applyViewerBidState(Auction auction) {
        viewerAutobidId = auction.getViewerAutobidId();
        viewerMaxBidAmount = auction.getViewerMaxBidAmount();
        viewerAutobidStatus = auction.getViewerAutobidStatus();
        viewerAutobidWinning = auction.isViewerAutobidWinning();
        viewerShowActiveMaxBid = auction.isViewerShowActiveMaxBid();
        viewerBidStateUpdatedAt = auction.getViewerBidStateUpdatedAt();
    }

    private void clearViewerBidState() {
        viewerAutobidId = null;
        viewerMaxBidAmount = null;
        viewerAutobidStatus = null;
        viewerAutobidWinning = false;
        viewerShowActiveMaxBid = false;
        viewerBidStateUpdatedAt = null;
        proxyBidMode = ProxyBidMode.REGISTER;
    }

    private void handleAutobidUpdatedEvent(RealtimeEvent<AutobidUpdatedPayload> event) {
        AutobidUpdatedPayload payload = event.getPayload();
        Long currentUserId = UserData.getUserId();
        if (payload == null
                || currentAuction == null
                || currentUserId == null
                || payload.getAuctionId() != currentAuction.getId()
                || payload.getUserId() != currentUserId.longValue()
                || isStaleAutobidPayload(payload)) {
            return;
        }

        Platform.runLater(() -> applyAutobidUpdate(payload));
    }

    private boolean isStaleAutobidPayload(AutobidUpdatedPayload payload) {
        LocalDateTime updatedAt = payload.getUpdatedAt();
        return updatedAt != null
            && viewerBidStateUpdatedAt != null
            && !updatedAt.isAfter(viewerBidStateUpdatedAt);
    }

    private void applyAutobidUpdate(AutobidUpdatedPayload payload) {
        if (currentAuction == null || isStaleAutobidPayload(payload)) {
            return;
        }
        viewerAutobidId = payload.getAutobidId();
        viewerMaxBidAmount = payload.getMaxBidAmount();
        viewerAutobidStatus = payload.getAutobidStatus();
        viewerAutobidWinning = payload.isWinning();
        viewerShowActiveMaxBid = payload.isShowActiveMaxBid();
        viewerBidStateUpdatedAt = payload.getUpdatedAt();
        updateBidInfo(currentAuction);
        updateProxyBidButton();
    }

    private void openRegisterProxyBidModal() {
        if (currentAuction == null) {
            return;
        }
        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice != null && nextMinimumBid.compareTo(buyNowPrice) >= 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Proxy bid unavailable",
                "The next valid proxy bid has reached the Buy Now price."
            );
            return;
        }

        proxyBidMode = ProxyBidMode.REGISTER;
        proxyBidTitleLabel.setText("Set Proxy Bid");
        proxyConfirmButton.setText("SET PROXY BID");
        proxyCurrentBidLabel.setText(formatCurrency(valueOrZero(currentAuction.getCurrentPrice())));
        proxyMinimumTextLabel.setText("Minimum proxy bid");
        proxyMinimumLabel.setText(formatCurrency(nextMinimumBid));
        proxyBidAmountField.setPromptText("Not lower than " + formatCurrency(nextMinimumBid));
        proxyHintLabel.setText("We will bid automatically up to your max. Your max bid stays private.");
        setNodeVisibility(proxyCurrentMaxRow, false);
        showProxyBidModal();
    }

    private void openIncreaseProxyBidModal() {
        if (currentAuction == null || viewerMaxBidAmount == null) {
            return;
        }
        proxyBidMode = ProxyBidMode.INCREASE;
        proxyBidTitleLabel.setText("Increase Proxy Bid");
        proxyConfirmButton.setText("INCREASE PROXY BID");
        proxyCurrentBidLabel.setText(formatCurrency(valueOrZero(currentAuction.getCurrentPrice())));
        proxyMinimumTextLabel.setText("New max must be higher than");
        proxyMinimumLabel.setText(formatCurrency(viewerMaxBidAmount));
        proxyCurrentMaxLabel.setText(formatCurrency(viewerMaxBidAmount));
        proxyBidAmountField.setPromptText("Higher than " + formatCurrency(viewerMaxBidAmount));
        proxyHintLabel.setText("Only the extra amount above your current max will be held.");
        setNodeVisibility(proxyCurrentMaxRow, true);
        showProxyBidModal();
    }

    private void showProxyBidModal() {
        proxyBidAmountField.clear();
        setNodeVisibility(proxyBidOverlay, true);
        Platform.runLater(proxyBidAmountField::requestFocus);
    }

    private void closeProxyBidModal() {
        proxyBidAmountField.clear();
        setNodeVisibility(proxyBidOverlay, false);
    }

    private boolean validateProxyBidAmount(BigDecimal maxBidAmount) {
        if (maxBidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid amount", "Max proxy bid must be greater than zero.");
            return false;
        }

        BigDecimal buyNowPrice = currentAuction.getBuyNowPrice();
        if (buyNowPrice != null && maxBidAmount.compareTo(buyNowPrice) >= 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Proxy bid too high",
                "Max proxy bid must be lower than the Buy Now price " + formatCurrency(buyNowPrice) + "."
            );
            return false;
        }

        if (proxyBidMode == ProxyBidMode.INCREASE) {
            return validateIncreaseProxyBidAmount(maxBidAmount);
        }
        return validateRegisterProxyBidAmount(maxBidAmount);
    }

    private boolean validateRegisterProxyBidAmount(BigDecimal maxBidAmount) {
        if (maxBidAmount.compareTo(nextMinimumBid) < 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Proxy bid too low",
                "Minimum proxy bid for this auction is " + formatCurrency(nextMinimumBid) + "."
            );
            return false;
        }
        if (maxBidAmount.compareTo(buyingPowerForCurrentAuction()) > 0) {
            showInsufficientBalance(maxBidAmount);
            return false;
        }
        return true;
    }

    private boolean validateIncreaseProxyBidAmount(BigDecimal maxBidAmount) {
        if (viewerMaxBidAmount == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Proxy bid unavailable", "No active proxy bid was found.");
            return false;
        }
        if (maxBidAmount.compareTo(viewerMaxBidAmount) <= 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Proxy bid too low",
                "New max proxy bid must be higher than " + formatCurrency(viewerMaxBidAmount) + "."
            );
            return false;
        }
        BigDecimal requiredDelta = maxBidAmount.subtract(viewerMaxBidAmount);
        if (requiredDelta.compareTo(availableBalance()) > 0) {
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING,
                "Insufficient balance",
                "Increasing this proxy bid requires " + formatCurrency(requiredDelta)
                    + ", but your available balance is " + formatCurrency(availableBalance()) + "."
            );
            return false;
        }
        return true;
    }

    private void sendRegisterProxyBid(BigDecimal maxBidAmount) {
        sendProxyBidRequest(RequestType.AUTO_BID, maxBidAmount, "Proxy bid set", "Your proxy bid was submitted.");
    }

    private void sendIncreaseProxyBid(BigDecimal maxBidAmount) {
        sendProxyBidRequest(
            RequestType.INCREASE_AUTOBID_MAX,
            maxBidAmount,
            "Proxy bid increased",
            "Your max proxy bid was submitted."
        );
    }

    private void sendProxyBidRequest(
            RequestType requestType,
            BigDecimal maxBidAmount,
            String successTitle,
            String successMessage) {
        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(requestType, new ProxyBidRequest(currentAuction.getId(), maxBidAmount))
            );
            if (response == null || !response.isStatus()) {
                NotificationManager.show(
                    NotificationManager.NotificationType.ERROR,
                    "Proxy bid failed",
                    response != null ? response.getMessage() : "No response from server."
                );
                return;
            }
            closeProxyBidModal();
            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, successTitle, successMessage);
        } catch (IOException exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Proxy bid failed", exception.getMessage());
        }
    }

    private boolean hasActiveWinningAutobid() {
        return viewerShowActiveMaxBid
            && viewerAutobidWinning
            && viewerMaxBidAmount != null
            && "WINNING".equals(viewerAutobidStatus);
    }

    private void updateProxyBidButton() {
        if (proxyBidButton == null) {
            return;
        }
        proxyBidButton.setText(hasActiveWinningAutobid() ? "INCREASE PROXY BID" : "SET PROXY BID");
    }

    private String proxyUnavailableMessage() {
        Long currentUserId = UserData.getUserId();
        if (currentAuction != null && currentUserId != null && currentUserId.longValue() == currentAuction.getSellerId()) {
            return "You cannot proxy bid on your own auction.";
        }
        return "This auction cannot receive proxy bids right now.";
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

    private void performBuyNow(boolean requireConfirmation) {
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
        if (requireConfirmation && !confirmBuyNowAction(buyNowPrice)) {
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

    private boolean confirmBuyNowAction(BigDecimal buyNowPrice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Buy Now");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        Label title = new Label("Are you sure?");
        title.getStyleClass().add("buy-now-title");

        Label message = new Label(
            "Do you want to buy \"" + currentAuction.getTitle() + "\" now for "
                + formatCurrency(buyNowPrice)
                + "? This will complete the auction immediately."
        );
        message.setWrapText(true);
        message.setMaxWidth(420);
        message.getStyleClass().add("buy-now-copy");

        Label price = new Label("Buy now price: " + formatCurrency(buyNowPrice));
        price.getStyleClass().add("buy-now-price");

        VBox content = new VBox(10, title, message, price);
        content.getStyleClass().add("buy-now-content");

        ButtonType buyNowButtonType = new ButtonType("Buy Now", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(buyNowButtonType, ButtonType.CANCEL);
        alert.getDialogPane().setContent(content);
        styleBuyNowDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buyNowButtonType;
    }

    private void styleBuyNowDialog(Alert alert) {
        var dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("buy-now-confirm-dialog");
        dialogPane.setPrefWidth(480);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        var stylesheet = getClass().getResource("/jfx/css/Bid.css");
        if (stylesheet != null) {
            dialogPane.getStylesheets().add(stylesheet.toExternalForm());
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

    private static final class ProxyBidRequest {
        private final long auctionId;
        private final BigDecimal maxBidAmount;

        private ProxyBidRequest(long auctionId, BigDecimal maxBidAmount) {
            this.auctionId = auctionId;
            this.maxBidAmount = maxBidAmount;
        }

        public long getAuctionId() {
            return auctionId;
        }

        public BigDecimal getMaxBidAmount() {
            return maxBidAmount;
        }
    }
}
    
