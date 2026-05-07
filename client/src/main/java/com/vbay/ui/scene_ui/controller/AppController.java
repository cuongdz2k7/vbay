package com.vbay.ui.scene_ui.controller;

import com.vbay.ui.scene_ui.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AppController {

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/login.fxml");
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Logout failed",
                "Could not return to the login screen."
            );
        }
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
