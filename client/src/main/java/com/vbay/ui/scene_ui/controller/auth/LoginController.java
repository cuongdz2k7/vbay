package com.vbay.ui.scene_ui.controller.auth;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Desktop;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.scene_ui.NotificationManager.NotificationType;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.SVGPath;

public class LoginController {
    private static final Logger LOGGER = LoggingUtils.getLogger(LoginController.class);

    private static final String EYE_OPEN_ICON =
        "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7zm10 5a5 5 0 1 0 0-10 5 5 0 0 0 0 10zm0-2a3 3 0 1 1 0-6 3 3 0 0 1 0 6z";

    private static final String EYE_OFF_ICON =
        "M9.88 9.88a3 3 0 1 0 4.24 4.24M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68M6.61 6.61A13.52 13.52 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61M2 2l20 20";

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
        if (UserData.isKicked()) {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Login failed",
                "You have just been kicked, please relaunch the app again"
            );
            return;
        }

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
        try {
            loginUser(username, password);
            if (UserData.getWarningCount() > 0) {
                NotificationManager.show(NotificationManager.NotificationType.WARNING, "Warning", "You have " + UserData.getWarningCount() + " warning(s). 3 warnings will result in a permanent ban.");
            } 
            else {
                if(Position.ADMIN.equals(UserData.getPosition())){
                    NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Login Successful", "Entering the ADMIN Dashboard");
                }
                else{
                    NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Login Successful", "Welcome back! Redirecting to home...");
                }
            }
        } catch (Exception exception) {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Login failed",
                exception.getMessage()
            );
            return; // Stop switch scene improperly
        }
        //UI Sides
        try {
            if (Position.ADMIN.equals(UserData.getPosition())) {
                SceneManager.switchScene("/jfx/scene/admin/AdminDashboard.fxml");
            } else {
                SceneManager.switchScene("/jfx/scene/Home.fxml");
            }
            SceneManager.enterImmersiveMode();
        } catch (Exception exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Navigation Failed", exception.getMessage() != null ? exception.getMessage() : "Could not open the home screen.");
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
