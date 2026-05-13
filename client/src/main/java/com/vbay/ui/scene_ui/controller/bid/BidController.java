package com.vbay.ui.scene_ui.controller.bid;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneDataReceiver;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
            SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Navigation failed", "Could not return to the home scene.");
        }
    }


    //From "Live Auction Detailed" to " Place Bid" Scene
    @FXML
    private void handleOpenPlaceBid(ActionEvent event) {
        if (currentProduct == null) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing product", "No asset is loaded for bidding.");
            return;
        }

        try {
            SceneManager.switchScene("/jfx/scene/bid/PlaceBid.fxml", currentProduct);
        } catch (Exception exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Navigation failed", "Could not open the place bid scene.");
        }
    }

    @FXML
    public void handleOpenByNow(ActionEvent event){
        NotificationManager.show(
            NotificationManager.NotificationType.SUCCESS,
            "Buy Now successful",
            "Congratulations! You have successfully purchased the product."
        );
    }
}
