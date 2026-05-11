package com.vbay.ui.scene_ui.controller.bid;

import java.math.BigDecimal;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.SceneDataReceiver;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.MoneyInput;
import com.vbay.ui.util.ProductImageLoader;

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
    private static final String ACTIVE_TAB_STYLE_CLASS = "active-tab";
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
    private Label lotLabel;
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
    private Label timeLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField bidAmountField;
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

        currentAuction = data;
        Product product = data.getProduct();
        BigDecimal currentPrice = valueOrZero(data.getCurrentPrice());
        BigDecimal startingPrice = valueOrZero(data.getStartingPrice());
        BigDecimal bidStep = valueOrZero(data.getMinimumBidStep());
        BigDecimal buyNowPrice = data.getBuyNowPrice();
        nextMinimumBid = currentPrice.add(bidStep);

        lotLabel.setText("LOT " + data.getId());
        titleLabel.setText(data.getTitle());
        descriptionLabel.setText(descriptionFor(data));
        longDescriptionLabel.setText(descriptionFor(data));
        currentBidLabel.setText(formatCurrency(currentPrice));
        buyNowLabel.setText(buyNowPrice == null ? "-" : formatCurrency(buyNowPrice));
        startingPriceLabel.setText(formatCurrency(startingPrice));
        stepLabel.setText(formatCurrency(bidStep));
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        timeLabel.setText(formatRemainingTime(data.getEndingTime(), product.getTimeLeft()));
        progressBar.setProgress(product.getProgress());
        bidAmountField.setText(MoneyInput.toInputText(nextMinimumBid));
        productNameLabel.setText(product.getTitle());
        auctionIdLabel.setText("#" + data.getId());
        statusLabel.setText(data.getStatus() == null || data.getStatus().isBlank() ? "-" : data.getStatus());

        ProductImageLoader.loadCover(productImageView, product.getImagePath());
        ProductImageLoader.loadCover(thumbnailOneImageView, product.getImagePath());
        ProductImageLoader.loadCover(thumbnailTwoImageView, product.getImagePath());
        ProductImageLoader.loadCover(thumbnailThreeImageView, product.getImagePath());
        ProductImageLoader.loadCover(thumbnailFourImageView, product.getImagePath());
        populateBidHistory(data);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception exception) {
            showMessage(Alert.AlertType.ERROR, "Navigation failed", "Could not return to the home scene.");
        }
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

    private String formatRemainingTime(LocalDateTime endingTime, String fallback) {
        if (endingTime == null) {
            return fallback == null || fallback.isBlank() ? "-" : fallback;
        }

        Duration remaining = Duration.between(LocalDateTime.now(), endingTime);
        if (remaining.isNegative() || remaining.isZero()) {
            return "Ended";
        }

        long totalMinutes = remaining.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }

    private void populateBidHistory(Auction data) {
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

    private void setHistoryRow(Label bidderLabel, Label amountLabel, String bidder, BigDecimal amount) {
        bidderLabel.setText(bidder);
        amountLabel.setText(formatCurrency(amount));
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
