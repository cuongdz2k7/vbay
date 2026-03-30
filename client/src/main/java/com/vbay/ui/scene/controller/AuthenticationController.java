package com.vbay.ui.scene.controller;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.LoginRequest;
import com.vbay.shared.dto.RegisterRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import java.io.IOException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AuthenticationController {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3618;

    private enum AuthMode {
        LOGIN,
        REGISTER
    }

    @FXML
    private Text titleText;

    @FXML
    private Text switchText;

    @FXML
    private Text emailLabel;

    @FXML
    private Text phoneLabel;

    @FXML
    private Text passwordLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button primaryButton;

    @FXML
    private Button secondaryButton;

    @FXML
    private Label statusLabel;

    private AuthMode currentMode = AuthMode.LOGIN;

    @FXML
    public void initialize() {
        switchMode(AuthMode.LOGIN);
    }

    @FXML
    private void handlePrimaryAction(ActionEvent event) {
        submit();
    }

    @FXML
    private void handleSecondaryAction(ActionEvent event) {
        AuthMode nextMode = currentMode == AuthMode.LOGIN ? AuthMode.REGISTER : AuthMode.LOGIN;
        switchMode(nextMode);
    }

    private void submit() {
        String validationMessage = validateInput();
        if (validationMessage != null) {
            showStatus(validationMessage, true);
            return;
        }

        Task<Respond<?>> authTask = new Task<>() {
            @Override
            protected Respond<?> call() throws Exception {
                return sendAuthenticationRequest();
            }
        };

        authTask.setOnRunning(event -> setLoading(true));
        authTask.setOnSucceeded(event -> {
            setLoading(false);
            handleResponse(authTask.getValue());
        });
        authTask.setOnFailed(event -> {
            setLoading(false);
            Throwable error = authTask.getException();
            String message = error == null ? "Khong the xu ly yeu cau." : error.getMessage();
            showStatus(message, true);
        });

        Thread thread = new Thread(authTask, "auth-request-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private Respond<?> sendAuthenticationRequest() throws IOException {
        SocketClient client = SocketClient.getClient();
        if (!client.isConnected()) {
            client.connect(SERVER_HOST, SERVER_PORT);
        }

        Request<?> request = currentMode == AuthMode.LOGIN
            ? new Request<>(RequestType.LOGIN, buildLoginRequest())
            : new Request<>(RequestType.REGISTER, buildRegisterRequest());

        return client.sendMessage(request);
    }

    private LoginRequest buildLoginRequest() {
        String identity = normalize(usernameField.getText());
        String email = null;
        String phoneNumber = null;
        String username = null;

        if (identity.contains("@")) {
            email = identity;
        } else if (identity.matches("\\d{9,15}")) {
            phoneNumber = identity;
        } else {
            username = identity;
        }

        return new LoginRequest(username, email, normalize(passwordField.getText()), phoneNumber);
    }

    private RegisterRequest buildRegisterRequest() {
        return new RegisterRequest(
            normalize(usernameField.getText()),
            normalize(emailField.getText()),
            normalize(passwordField.getText()),
            normalize(phoneField.getText())
        );
    }

    private void handleResponse(Respond<?> response) {
        if (response == null) {
            showStatus("Server da phan hoi nhung chua dung dinh dang auth.", true);
            return;
        }

        if (response.isStatus()) {
            String successMessage = response.getMessage() == null || response.getMessage().isBlank()
                ? defaultSuccessMessage()
                : response.getMessage();
            showStatus(successMessage, false);
            clearPassword();
            return;
        }

        String errorMessage = response.getMessage() == null || response.getMessage().isBlank()
            ? "Dang nhap/ dang ky that bai."
            : response.getMessage();
        showStatus(errorMessage, true);
    }

    private String validateInput() {
        if (isBlank(usernameField.getText())) {
            return currentMode == AuthMode.LOGIN
                ? "Nhap ten dang nhap, email hoac so dien thoai."
                : "Ten dang nhap khong duoc de trong.";
        }

        if (isBlank(passwordField.getText())) {
            return "Mat khau khong duoc de trong.";
        }

        if (currentMode == AuthMode.REGISTER) {
            if (isBlank(emailField.getText())) {
                return "Email khong duoc de trong.";
            }

            if (!normalize(emailField.getText()).matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return "Email chua dung dinh dang.";
            }

            if (isBlank(phoneField.getText())) {
                return "So dien thoai khong duoc de trong.";
            }

            if (!normalize(phoneField.getText()).matches("\\d{9,15}")) {
                return "So dien thoai chi nen gom 9-15 chu so.";
            }

            if (normalize(passwordField.getText()).length() < 6) {
                return "Mat khau nen co it nhat 6 ky tu.";
            }
        }

        return null;
    }

    private void switchMode(AuthMode mode) {
        currentMode = mode;

        boolean registerMode = mode == AuthMode.REGISTER;
        setManagedVisibility(emailLabel, registerMode);
        setManagedVisibility(emailField, registerMode);
        setManagedVisibility(phoneLabel, registerMode);
        setManagedVisibility(phoneField, registerMode);

        titleText.setText(registerMode ? "Tao tai khoan" : "Chao mung!");
        usernameField.setPromptText(registerMode ? "Nhap ten dang nhap" : "Nhap ten dang nhap, email hoac so dien thoai");
        passwordLabel.setLayoutY(registerMode ? 379.0 : 211.0);
        passwordField.setLayoutY(registerMode ? 391.0 : 223.0);
        primaryButton.setLayoutY(registerMode ? 446.0 : 278.0);
        switchText.setLayoutY(registerMode ? 508.0 : 364.0);
        secondaryButton.setLayoutY(registerMode ? 518.0 : 374.0);
        statusLabel.setLayoutY(registerMode ? 482.0 : 332.0);
        statusLabel.setText("");

        primaryButton.setText(registerMode ? "Dang ky" : "Dang nhap");
        secondaryButton.setText(registerMode ? "Quay lai dang nhap" : "Dang ky");
        switchText.setText(registerMode ? "Da co tai khoan?" : "Chua co tai khoan?");

        if (!registerMode) {
            emailField.clear();
            phoneField.clear();
        }
    }

    private void setLoading(boolean loading) {
        usernameField.setDisable(loading);
        emailField.setDisable(loading);
        phoneField.setDisable(loading);
        passwordField.setDisable(loading);
        primaryButton.setDisable(loading);
        secondaryButton.setDisable(loading);
        if (loading) {
            showStatus("Dang gui yeu cau...", false);
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
    }

    private void setManagedVisibility(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void clearPassword() {
        passwordField.clear();
    }

    private String defaultSuccessMessage() {
        return currentMode == AuthMode.LOGIN ? "Dang nhap thanh cong." : "Dang ky thanh cong.";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return normalize(value).isBlank();
    }
}
