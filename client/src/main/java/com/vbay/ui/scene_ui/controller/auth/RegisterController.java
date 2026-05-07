package com.vbay.ui.scene_ui.controller.auth;

import java.io.IOException;
import java.util.regex.Pattern;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.authDTO.RegisterRequest;
import com.vbay.shared.status.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Missing full name", "Please enter your full name.");
            fullNameField.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            showMessage(Alert.AlertType.WARNING, "Invalid full name", "Full name must be at least 2 characters.");
            fullNameField.requestFocus();
            return;
        }

        if (email.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Missing email", "Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showMessage(Alert.AlertType.WARNING, "Invalid email", "Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        if (password.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Missing password", "Please create a password.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 8) {
            showMessage(Alert.AlertType.WARNING, "Weak password", "Password must be at least 8 characters.");
            passwordField.requestFocus();
            return;
        }

        if (confirmPassword.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Missing confirmation", "Please confirm your password.");
            confirmPasswordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage(Alert.AlertType.WARNING, "Password mismatch", "Password and confirmation do not match.");
            confirmPasswordField.requestFocus();
            return;
        }

        try {
            registerUser(fullName, email, password);
            showMessage(
                Alert.AlertType.INFORMATION,
                "Registration complete",
                "Your account has been created. Please log in."
            );
            goToLogin();
        } catch (IOException exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Register failed",
                exception.getMessage()
            );
        }
    }

    private void registerUser(String fullName, String email, String password) throws IOException {
        Request<RegisterRequest> request = new Request<>(
            RequestType.REGISTER,
            new RegisterRequest(fullName, email, password, "")
        );

        Respond<?> response = SocketClient.getClient().sendMessage(request);
        if (response == null) {
            throw new IOException("No response from server.");
        }
        if (!response.isStatus()) {
            throw new IOException(response.getMessage() != null ? response.getMessage() : "Registration failed.");
        }
    }

    @FXML
    private void handleBackToHome(ActionEvent event) {
        goToLogin();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        goToLogin();
    }

    private void goToLogin() {
        try {
            SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Navigation failed",
                "Could not open the login screen."
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
