package com.vbay.ui.scene_ui.controller.auth;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Desktop;

import com.vbay.network.UserData;
import com.vbay.network.SocketClient;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.SVGPath;

public class LoginController {
    private static final Logger LOGGER = LoggingUtils.getLogger(LoginController.class);

    private static final String EYE_OPEN_ICON =
        "M1.5 0C0.6716 0 0 0.6716 0 1.5v9.002C0.6314 11.4642 1.6154 12.2766 2.8147 12.8393C4.1372 13.4602 5.9548 13.998 8 13.998s3.8628-0.5378 5.1853-1.1587C14.3846 12.2766 15.3686 11.4642 16 10.502V1.5C16 0.6716 15.3284 0 14.5 0H1.5ZM8 3.5a3.5 3.5 0 1 1 0 7.001A3.5 3.5 0 0 1 8 3.5Zm0 1.5a2 2 0 1 0 0 4.001A2 2 0 0 0 8 5Z";

    private static final String EYE_OFF_ICON =
        "M1.2 0.2-0.9 0.9 2.4 2.9C1.4 3.8 0.6 4.9 0 6.2c0.7 1.6 1.9 2.9 3.4 3.9l-2.1 2.1 0.9 0.9 13-13-0.9-0.9-2.3 2.3C10.8 0.5 9.5 0 8 0 4.5 0 1.5 2.5 0 6.2c0.7 1.6 1.9 2.9 3.4 3.9l-2.2 2.2 0.9 0.9 13-13-0.9-0.9-2.3 2.3zM8 1.3c1.1 0 2.1 0.3 3 0.8l-1.5 1.5c-0.4-0.2-0.9-0.3-1.5-0.3-1.7 0-3 1.3-3 3 0 0.5 0.1 1 0.3 1.5L4.1 9C3 8.2 2.1 7.2 1.5 6.1 2.8 3.2 5.2 1.3 8 1.3zm4.9 2.4c0.7 0.7 1.3 1.5 1.7 2.4-1.3 2.9-3.7 4.8-6.6 4.8-1.1 0-2.1-0.3-3-0.8l1.5-1.5c0.4 0.2 0.9 0.3 1.5 0.3 1.7 0 3-1.3 3-3 0-0.5-0.1-1-0.3-1.5l1.2-1.2z";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private SVGPath passwordToggleIcon;

    private boolean passwordVisible;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordToggleIcon.setContent(EYE_OPEN_ICON);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank()) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing username", "Please enter your username.");
            usernameField.requestFocus();
            return;
        }

        if (password.isBlank()) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Missing password", "Please enter your password.");
            passwordField.requestFocus();
            return;
        }
        //Sever Side
        try {
            loginUser(username, password);
            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Login Successful", "Welcome back! Redirecting to home...");
        } catch (Exception exception) {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Login failed",
                exception.getMessage()
            );
            return; // Stop switch scene improperly
        }
        //UI Sides
        try{
            SceneManager.switchScene("/jfx/scene/Home.fxml");
            SceneManager.enterImmersiveMode();
        }catch ( Exception exception){
                NotificationManager.show(NotificationManager.NotificationType.ERROR, "Navigation Failed",exception.getMessage() != null ? exception.getMessage() : "Could not open the home screen.");
        }
    }
    //Method Login
    private void loginUser(String username, String password) throws IOException {
        //Send request
        Request<LoginRequest> request = new Request<>(
            RequestType.LOGIN,
            new LoginRequest(username, "", password, "")
        );
        //Return response
        Respond<?> response = SocketClient.getClient().sendMessage(request);
        if (response == null) {
            throw new IOException("No response from server.");
        }
        if (!response.isStatus()) {
            throw new IOException(response.getMessage() != null ? response. getMessage() : "Login failed.");
        }
        LoginResponse loginResponse = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), LoginResponse.class);
        if (loginResponse != null) {
            UserData.setLoginResponse(loginResponse);
        }
        LOGGER.info(() -> "Client login successful: " + username);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try{
            String youtubeUrl = "https://youtu.be/dQw4w9WgXcQ?si=KnJkzoJwTT9Y6Qgy";
            Desktop.getDesktop().browse(new URI(youtubeUrl));
        }catch(Exception exception){
            LOGGER.log(Level.WARNING, "Could not open forgot-password link.", exception);
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/auth/Register.fxml");
        } catch (Exception exception) {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the register screen."
            );
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;

        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordToggleIcon.setContent(passwordVisible ? EYE_OFF_ICON : EYE_OPEN_ICON);

        if (passwordVisible) {
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }
}
