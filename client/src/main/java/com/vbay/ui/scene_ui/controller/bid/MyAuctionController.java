package com.vbay.ui.scene_ui.controller.bid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.vbay.ui.model.Auction;
import com.vbay.ui.scene_ui.controller.card.AuctionCardController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class MyAuctionController {

    @FXML
    private Button tabAll;
    @FXML
    private Button tabOngoing;
    @FXML
    private Button tabScheduled;
    @FXML
    private Button tabCompleted;
    @FXML
    private Button tabFailed;
    @FXML
    private FlowPane productFlow;
    @FXML
    private Label emptyStateLabel;

    private List<Auction> allAuctions = new ArrayList<>();
    private String selectedTab = "All";
    private Consumer<Auction> onAuctionSelected;

    @FXML
    private void initialize() {
        setActiveTabStyle(tabAll);
    }

    public void setOnAuctionSelected(Consumer<Auction> onAuctionSelected) {
        this.onAuctionSelected = onAuctionSelected;
    }

    public void setInitialItems(Collection<Auction> items) {
        this.allAuctions.clear();
        if (items != null) {
            this.allAuctions.addAll(items);
        }
        renderRows();
    }

    public void dispose() {
        allAuctions.clear();
        onAuctionSelected = null;
    }

    @FXML
    private void handleTabSelection(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        selectedTab = clickedButton.getText();

        resetTabStyles();
        setActiveTabStyle(clickedButton);

        renderRows();
    }

    private void resetTabStyles() {
        tabAll.getStyleClass().removeAll("active-tab");
        tabOngoing.getStyleClass().removeAll("active-tab");
        tabScheduled.getStyleClass().removeAll("active-tab");
        tabCompleted.getStyleClass().removeAll("active-tab");
        tabFailed.getStyleClass().removeAll("active-tab");
    }

    private void setActiveTabStyle(Button button) {
        if (!button.getStyleClass().contains("active-tab")) {
            button.getStyleClass().add("active-tab");
        }
    }

    private void renderRows() {
        productFlow.getChildren().clear();
        List<Auction> filtered = getFilteredAuctions();

        boolean empty = filtered.isEmpty();
        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);

        for (Auction item : filtered) {
            productFlow.getChildren().add(createAuctionCard(item));
        }
    }

    private List<Auction> getFilteredAuctions() {
        return allAuctions.stream()
            .filter(item -> {
                if ("All".equals(selectedTab)) {
                    return true;
                }
                String status = item.getStatus();
                if ("Ongoing".equals(selectedTab)) {
                    return "ACTIVE".equals(status);
                }
                if ("Scheduled".equals(selectedTab)) {
                    return "SCHEDULED".equals(status);
                }
                if ("Completed".equals(selectedTab)) {
                    return "ENDED".equals(status) || "SOLD".equals(status);
                }
                if ("Cancelled/Failed".equals(selectedTab)) {
                    return "FAILED".equals(status) || "CANCELLED".equals(status);
                }
                return true;
            })
            .toList();
    }

    private Node createAuctionCard(Auction listAuction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jfx/scene/AuctionCard.fxml"));
            Node card = loader.load();
            AuctionCardController controller = loader.getController();
            controller.setAuction(listAuction);
            controller.setOnSelected(id -> {
                if (onAuctionSelected != null) {
                    onAuctionSelected.accept(listAuction);
                }
            });
            return card;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load auction card view.", exception);
        }
    }
}
