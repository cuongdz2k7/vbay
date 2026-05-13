package com.vbay.ui.scene_ui.controller.bid;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.SceneDataReceiver;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.vbay.ui.scene_ui.NotificationManager;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class PlaceBidController implements SceneDataReceiver<Product> {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final String[] HISTORY_BIDDERS = {
        "Mia Tran",
        "Alex Carter",
        "Noah Pham"
    };

    @FXML
    private ImageView productImageView;
    @FXML
    private Label assetCodeLabel;
    @FXML
    private Label assetNameLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label currentBidLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label stepLabel;
    @FXML
    private Label nextBidLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField bidAmountField;
    //Three Highest Bidder
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

    private Product currentProduct;
    private BigDecimal nextMinimumBid = BigDecimal.ZERO;

    @Override
    public void setSceneData(Product data) {
        if (data == null) {
            return;
        }

        currentProduct = data;
        nextMinimumBid = parseCurrency(data.getPrice()).add(parseCurrency(data.getBidStep()));

        assetCodeLabel.setText(buildAssetCode(data.getTitle()));
        assetNameLabel.setText(data.getTitle());
        titleLabel.setText(data.getTitle());
        currentBidLabel.setText(data.getPrice());
        startingPriceLabel.setText(data.getStartingPrice());
        stepLabel.setText(data.getBidStep());
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        timeLabel.setText(data.getTimeLeft());
        progressPercentLabel.setText(String.format(Locale.US, "%.0f%% auction activity", data.getProgress() * 100));
        progressBar.setProgress(data.getProgress());
        bidAmountField.setText(formatCurrency(nextMinimumBid));
        productImageView.setImage(ProductImageLoader.load(data.getImagePath()));
        populateBidHistory(data);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception exception) {
            showMessage(NotificationManager.NotificationType.ERROR, "Navigation failed", "Could not return to the home scene.");
        }
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentProduct == null) {
            showMessage(NotificationManager.NotificationType.WARNING, "Missing product", "No asset is loaded for bidding.");
            return;
        }

        String rawBid = bidAmountField.getText().trim();
        if (rawBid.isBlank()) {
            showMessage(NotificationManager.NotificationType.WARNING, "Missing amount", "Enter a bid amount before placing a bid.");
            bidAmountField.requestFocus();
            return;
        }

        BigDecimal enteredBid;
        try {
            enteredBid = parseCurrency(rawBid);
        } catch (NumberFormatException exception) {
            showMessage(NotificationManager.NotificationType.WARNING, "Invalid amount", "Bid amount must be a valid currency value.");
            bidAmountField.requestFocus();
            return;
        }

        if (enteredBid.compareTo(nextMinimumBid) < 0) {
            showMessage(
                NotificationManager.NotificationType.WARNING,
                "Bid too low",
                "Minimum valid bid for this asset is " + formatCurrency(nextMinimumBid) + "."
            );
            bidAmountField.requestFocus();
            return;
        }

        showMessage(
            NotificationManager.NotificationType.SUCCESS,
            "Bid placed",
            "Your bid of " + formatCurrency(enteredBid) + " has been placed for " + currentProduct.getTitle() + "."
        );
    }

    @FXML
    private void handleProxyBid(ActionEvent event) {
        showMessage(NotificationManager.NotificationType.INFO, "Proxy bid", "Proxy bidding flow is not connected yet.");
    }

    @FXML
    private void handleWatchAsset(ActionEvent event) {
        showMessage(NotificationManager.NotificationType.INFO, "Watch asset", "Watchlist flow is not connected yet.");
    }

    //Creating Code of Product ~ MASANPHAM
    private static String buildAssetCode(String title) {
        String compact = title == null ? "LOT-UNSET" : title.replaceAll("[^A-Za-z0-9]+", "-").toUpperCase(Locale.US);
        compact = compact.replaceAll("^-+|-+$", "");
        if (compact.length() > 18) {
            compact = compact.substring(0, 18);
        }
        return compact.isBlank() ? "LOT-UNSET" : compact;
    }

    //Converting String --> Decimal (Caculating Money)
    private static BigDecimal parseCurrency(String value) {
        String normalized = value.replaceAll("[^\\d.]", "");
        if (normalized.isBlank()) {
            throw new NumberFormatException("Empty currency value");
        }
        return new BigDecimal(normalized);
    }

    //Exchaning Decimal (Displaying Money)
    private static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    // 3 Highest Bid
    private void populateBidHistory(Product data) {
        BigDecimal currentBid = parseCurrency(data.getPrice());
        BigDecimal step = parseCurrency(data.getBidStep());
        BigDecimal startingBid = parseCurrency(data.getStartingPrice());
        //Bidder 1
        setHistoryRow(
            historyBidderOneLabel, 
            historyAmountOneLabel, 
            HISTORY_BIDDERS[0], 
            currentBid);
        //Bidder 2
        setHistoryRow(
            historyBidderTwoLabel,
            historyAmountTwoLabel,
            HISTORY_BIDDERS[1],
            currentBid.subtract(step).max(startingBid));
        //Bidder 3
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

    private void showMessage(NotificationManager.NotificationType type, String title, String content) {
        NotificationManager.show(type, title, content);
    }
}
