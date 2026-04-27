package com.vbay.ui.scene.controller.bid;

import java.util.Locale;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene.SceneDataReceiver;
import com.vbay.ui.scene.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class AssetOverviewController implements SceneDataReceiver<Product> {
    @FXML
    private ImageView productImageView;
    @FXML
    private Label assetCodeLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label currentBidLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label stepLabel;
    @FXML
    private Label timeLeftLabel;
    @FXML
    private Label activityLabel;

    private Product currentProduct;

    @Override
    public void setSceneData(Product data) {
        if (data == null) {
            return;
        }

        currentProduct = data;
        assetCodeLabel.setText(buildAssetCode(data.getTitle()));
        titleLabel.setText(data.getTitle());
        summaryLabel.setText(data.getDescription());
        currentBidLabel.setText(data.getPrice());
        startingPriceLabel.setText(data.getStartingPrice());
        stepLabel.setText(data.getBidStep());
        timeLeftLabel.setText(data.getTimeLeft());
        activityLabel.setText(String.format(Locale.US, "%.0f%% activity", data.getProgress() * 100));
        productImageView.setImage(ProductImageLoader.load(data.getImagePath()));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/bid/placeBid.fxml", currentProduct);
        } catch (Exception exception) {
            showMessage(Alert.AlertType.ERROR, "Navigation failed", "Could not return to the place bid scene.");
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

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
