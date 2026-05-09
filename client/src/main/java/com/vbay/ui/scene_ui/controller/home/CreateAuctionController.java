package com.vbay.ui.scene_ui.controller.home;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateAuctionController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> conditionComboBox;

    @FXML
    private TextField startingBidField;

    @FXML
    private TextField minBidStepField;

    @FXML
    private TextField reservePriceField;

    @FXML
    private TextField buyNowPriceField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea imagesArea;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField endTimeField;

    @FXML
    private void initialize() {
        // Initialize categories if needed
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll(
                "Electronics", "Collectibles", "Art", 
                "Jewelry & Watches", "Sporting Goods", "Others"
            );
        }

        // Initialize conditions
        if (conditionComboBox != null) {
            conditionComboBox.getItems().addAll("NEW", "USED");
            conditionComboBox.setValue("NEW");
        }
    }

    @FXML
    private void handleCreate() {
        // Logic to create auction
//        System.out.println("Create auction triggered");
//        System.out.println("Title: " + titleField.getText());
//        System.out.println("Category: " + categoryComboBox.getValue());
//        System.out.println("Condition: " + conditionComboBox.getValue());
//        System.out.println("Starting Bid: " + startingBidField.getText());
//        System.out.println("Min Bid Step: " + minBidStepField.getText());
//        System.out.println("Reserve Price: " + reservePriceField.getText());
//        System.out.println("Buy Now Price: " + buyNowPriceField.getText());
//        System.out.println("Description: " + descriptionArea.getText());
//        System.out.println("Images: " + imagesArea.getText());
//        System.out.println("Start Date: " + startDatePicker.getValue());
//        System.out.println("Start Time: " + startTimeField.getText());
//        System.out.println("End Date: " + endDatePicker.getValue());
//        System.out.println("End Time: " + endTimeField.getText());
    }

    @FXML
    private void handleCancel() {
        try {
            com.vbay.ui.scene_ui.SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
