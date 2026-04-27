package com.vbay.ui.scene.controller.bid;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene.SceneDataReceiver;
import com.vbay.ui.scene.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class PlaceBidController implements SceneDataReceiver<Product> {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private ImageView productImageView;
    @FXML
    private Label assetCodeLabel;
    @FXML
    private Label assetNameLabel;
    @FXML
    private Label assetCurrentBidChipLabel;
    @FXML
    private Label assetStartingPriceChipLabel;
    @FXML
    private Label assetStepChipLabel;
    @FXML
    private Label assetSummaryLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
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
        assetCurrentBidChipLabel.setText("Current Bid  " + data.getPrice());
        assetStartingPriceChipLabel.setText("Starting Price  " + data.getStartingPrice());
        assetStepChipLabel.setText("Step  " + data.getBidStep());
        assetSummaryLabel.setText(data.getDescription());
        titleLabel.setText(data.getTitle());
        descriptionLabel.setText(data.getDescription());
        currentBidLabel.setText(data.getPrice());
        startingPriceLabel.setText(data.getStartingPrice());
        stepLabel.setText(data.getBidStep());
        nextBidLabel.setText(formatCurrency(nextMinimumBid));
        timeLabel.setText(data.getTimeLeft());
        progressPercentLabel.setText(String.format(Locale.US, "%.0f%% auction activity", data.getProgress() * 100));
        progressBar.setProgress(data.getProgress());
        bidAmountField.setText(formatCurrency(nextMinimumBid));
        productImageView.setImage(ProductImageLoader.load(data.getImagePath()));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/account/home.fxml");
        } catch (Exception exception) {
            showMessage(Alert.AlertType.ERROR, "Navigation failed", "Could not return to the home scene.");
        }
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentProduct == null) {
            showMessage(Alert.AlertType.WARNING, "Missing product", "No asset is loaded for bidding.");
            return;
        }

        String rawBid = bidAmountField.getText().trim();
        if (rawBid.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Missing amount", "Enter a bid amount before placing a bid.");
            bidAmountField.requestFocus();
            return;
        }

        BigDecimal enteredBid;
        try {
            enteredBid = parseCurrency(rawBid);
        } catch (NumberFormatException exception) {
            showMessage(Alert.AlertType.WARNING, "Invalid amount", "Bid amount must be a valid currency value.");
            bidAmountField.requestFocus();
            return;
        }

        if (enteredBid.compareTo(nextMinimumBid) < 0) {
            showMessage(
                Alert.AlertType.WARNING,
                "Bid too low",
                "Minimum valid bid for this asset is " + formatCurrency(nextMinimumBid) + "."
            );
            bidAmountField.requestFocus();
            return;
        }

        showMessage(
            Alert.AlertType.INFORMATION,
            "Bid staged",
            "Bid " + formatCurrency(enteredBid) + " queued for " + currentProduct.getTitle() + "."
        );
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
    private void handleOpenAssetOverview(ActionEvent event) {
        if (currentProduct == null) {
            showMessage(Alert.AlertType.WARNING, "Missing product", "No asset is loaded to inspect.");
            return;
        }

        try {
            SceneManager.switchScene("/jfx/scene/bid/assetOverview.fxml", currentProduct);
        } catch (Exception exception) {
            showMessage(Alert.AlertType.ERROR, "Navigation failed", "Could not open the asset overview scene.");
        }
    }

    private static String buildAssetCode(String title) {
        String compact = title == null ? "LOT-UNSET" : title.replaceAll("[^A-Za-z0-9]+", "-").toUpperCase(Locale.US);
        compact = compact.replaceAll("^-+|-+$", "");
        if (compact.length() > 18) {
            compact = compact.substring(0, 18);
        }
        return compact.isBlank() ? "LOT-UNSET" : compact;
    }

    private static BigDecimal parseCurrency(String value) {
        String normalized = value.replaceAll("[^\\d.]", "");
        if (normalized.isBlank()) {
            throw new NumberFormatException("Empty currency value");
        }
        return new BigDecimal(normalized);
    }

    private static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
