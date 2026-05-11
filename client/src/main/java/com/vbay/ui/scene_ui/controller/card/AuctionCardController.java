package com.vbay.ui.scene_ui.controller.card;

import java.util.function.Consumer;

import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
import com.vbay.ui.util.ProductImageLoader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class AuctionCardController {
    @FXML
    private VBox cardRoot;
    @FXML
    private ImageView productImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label bidStepLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ProgressBar progressBar;

    private Auction auction;
    private Consumer<Auction> onSelected;

    @FXML
    private void initialize() {
        cardRoot.setFocusTraversable(true);
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
        Product product = auction.getProduct();
        titleLabel.setText(auction.getTitle());
        priceLabel.setText("Current Bid  " + product.getPrice());
        startingPriceLabel.setText("Starting Price  " + product.getStartingPrice());
        bidStepLabel.setText("Step  " + product.getBidStep());
        timeLabel.setText(product.getTimeLeft());
        progressBar.setProgress(product.getProgress());
        ProductImageLoader.loadCover(productImageView, product.getImagePath());
    }

    public void setOnSelected(Consumer<Auction> onSelected) {
        this.onSelected = onSelected;
    }

    @FXML
    private void handleCardClicked(MouseEvent event) {
        notifySelection();
    }

    @FXML
    private void handleCardKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            notifySelection();
            event.consume();
        }
    }

    private void notifySelection() {
        if (auction != null && onSelected != null) {
            onSelected.accept(auction);
        }
    }
}
