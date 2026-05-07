package com.vbay.ui.scene_ui.controller.bid;

import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.SceneDataReceiver;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.ProductImageLoader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
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
            SceneManager.switchScene("/jfx/scene/bid/PlaceBid.fxml", currentProduct);
        } catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("VBay");
            alert.setHeaderText("Navigation failed");
            alert.setContentText("Could not open the place bid scene.");
            alert.showAndWait();
        }
    }
    @FXML
    public void handleOpenByNow(ActionEvent event){
        Alert notification =new Alert(Alert.AlertType.INFORMATION);
        notification.setTitle("Vbay");
        notification.setHeaderText("By Now succeed");
        notification.setContentText("You Already bought the products,\n Congratulation");
        notification.showAndWait();
    }
}
