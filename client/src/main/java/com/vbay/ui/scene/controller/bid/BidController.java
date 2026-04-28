package com.vbay.ui.scene.controller.bid;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene.SceneDataReceiver;
import com.vbay.ui.scene.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

public class BidController implements SceneDataReceiver<Product> {
    @FXML
    private ImageView productImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ProgressBar progressBar;

    private Product currentProduct;

    @Override
    public void setSceneData(Product data) {
        if (data == null) {
            return;
        }

        currentProduct = data;
        titleLabel.setText(data.getTitle());
        priceLabel.setText(data.getPrice());
        timeLabel.setText(data.getTimeLeft());
        descriptionLabel.setText(data.getDescription());
        progressBar.setProgress(data.getProgress());
        productImageView.setImage(ProductImageLoader.load(data.getImagePath()));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/account/home.fxml");
        } catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("VBay");
            alert.setHeaderText("Navigation failed");
            alert.setContentText("Could not return to the home scene.");
            alert.showAndWait();
        }
    }


    //From "Live Auction Detailed" to " Place Bid" Scene
    @FXML
    private void handleOpenPlaceBid(ActionEvent event) {
        if (currentProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("VBay");
            alert.setHeaderText("Missing product");
            alert.setContentText("No asset is loaded for bidding.");
            alert.showAndWait();
            return;
        }

        try {
            SceneManager.switchScene("/jfx/scene/bid/placeBid.fxml", currentProduct);
        } catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("VBay");
            alert.setHeaderText("Navigation failed");
            alert.setContentText("Could not open the place bid scene.");
            alert.showAndWait();
        }
    }
}
