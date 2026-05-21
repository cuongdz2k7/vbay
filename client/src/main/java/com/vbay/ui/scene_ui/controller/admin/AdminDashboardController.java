package com.vbay.ui.scene_ui.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.adminDTO.AdminUserDTO;
import com.vbay.shared.dto.adminDTO.AdminUserListResponse;
import com.vbay.shared.dto.adminDTO.BanUserRequest;
import com.vbay.shared.dto.adminDTO.UnbanUserRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminDashboardController {
    private static final Logger LOGGER = LoggingUtils.getLogger(AdminDashboardController.class);

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label roleLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label bannedUsersLabel;
    @FXML
    private VBox userListContainer;
    @FXML
    private VBox emptyState;

    @FXML
    private void initialize() {
        String username = UserData.getUsername();
        adminNameLabel.setText(username == null || username.isBlank() ? "Admin" : username);
        roleLabel.setText(UserData.getPosition() == null ? "ADMIN" : UserData.getPosition().name());
        loadUsers();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUsers();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            logoutUser();
            SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Admin logout failed", exception);
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Logout failed",
                exception.getMessage() != null ? exception.getMessage() : "Could not log out."
            );
        }
    }

    private void loadUsers() {
        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.GET_ADMIN_USER_LIST, null)
            );
            if (response == null || !response.isStatus()) {
                NotificationManager.show(
                    NotificationManager.NotificationType.ERROR,
                    "Users unavailable",
                    response != null ? response.getMessage() : "No response from server."
                );
                return;
            }

            AdminUserListResponse userListResponse = JsonUtils.fromJson(
                JsonUtils.toJson(response.getData()),
                AdminUserListResponse.class
            );
            List<AdminUserDTO> users = userListResponse == null || userListResponse.getUsers() == null
                ? List.of()
                : userListResponse.getUsers();
            renderUsers(users);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Loading admin users failed", exception);
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Users unavailable",
                exception.getMessage() != null ? exception.getMessage() : "Could not load users."
            );
        }
    }

    private void renderUsers(List<AdminUserDTO> users) {
        userListContainer.getChildren().clear();
        totalUsersLabel.setText(String.valueOf(users.size()));
        activeUsersLabel.setText(String.valueOf(users.stream().filter(user -> user.getStatus() == UserStatus.ACTIVE).count()));
        bannedUsersLabel.setText(String.valueOf(users.stream().filter(user -> user.getStatus() == UserStatus.BANNED).count()));

        emptyState.setVisible(users.isEmpty());
        emptyState.setManaged(users.isEmpty());

        for (AdminUserDTO user : users) {
            userListContainer.getChildren().add(createUserRow(user));
        }
    }

    private HBox createUserRow(AdminUserDTO user) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(0);
        row.getStyleClass().add("admin-table-row");

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().addAll("admin-row-text", "admin-col-user");

        Label roleLabel = new Label(user.getPosition() == null ? "" : user.getPosition().name());
        roleLabel.getStyleClass().addAll("admin-row-text", "admin-col-role");

        Label statusLabel = new Label(user.getStatus() == null ? "" : user.getStatus().name());
        statusLabel.getStyleClass().addAll("admin-status-pill", statusStyleClass(user.getStatus()));
        HBox statusCell = new HBox(statusLabel);
        statusCell.setAlignment(Pos.CENTER_LEFT);
        statusCell.getStyleClass().add("admin-col-status");

        Label createdAtLabel = new Label(user.getTimeInit() == null ? "" : user.getTimeInit());
        createdAtLabel.getStyleClass().addAll("admin-row-text", "admin-col-created");

        Button actionButton = createStatusActionButton(user);
        HBox actionCell = new HBox(actionButton);
        actionCell.setAlignment(Pos.CENTER_LEFT);
        actionCell.getStyleClass().add("admin-col-action");

        row.getChildren().addAll(usernameLabel, roleLabel, statusCell, createdAtLabel, actionCell);
        return row;
    }

    private Button createStatusActionButton(AdminUserDTO user) {
        Button button = new Button();
        button.getStyleClass().add("admin-table-action-btn");

        if (isCurrentUser(user)) {
            button.setText("Current");
            button.setDisable(true);
            return button;
        }
        if (user.getPosition() == Position.ADMIN) {
            button.setText("Admin");
            button.setDisable(true);
            return button;
        }
        if (user.getStatus() == UserStatus.BANNED) {
            button.setText("Unban");
            button.getStyleClass().add("admin-unban-btn");
            button.setOnAction(event -> updateUserStatus(RequestType.UNBAN_USER, new UnbanUserRequest(user.getId())));
            return button;
        }
        if (user.getStatus() == UserStatus.DELETED) {
            button.setText("Deleted");
            button.setDisable(true);
            return button;
        }

        button.setText("Ban");
        button.getStyleClass().add("admin-ban-btn");
        button.setOnAction(event -> updateUserStatus(RequestType.BAN_USER, new BanUserRequest(user.getId())));
        return button;
    }

    private void updateUserStatus(RequestType requestType, Object payload) {
        try {
            Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(requestType, payload));
            if (response == null || !response.isStatus()) {
                NotificationManager.show(
                    NotificationManager.NotificationType.ERROR,
                    "Update failed",
                    response != null ? response.getMessage() : "No response from server."
                );
                return;
            }

            NotificationManager.show(
                NotificationManager.NotificationType.INFO,
                "User updated",
                response.getMessage() != null ? response.getMessage() : "User status was updated."
            );
            loadUsers();
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Updating user status failed", exception);
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Update failed",
                exception.getMessage() != null ? exception.getMessage() : "Could not update user status."
            );
        }
    }

    private boolean isCurrentUser(AdminUserDTO user) {
        Long currentUserId = UserData.getUserId();
        return currentUserId != null && user.getId() == currentUserId;
    }

    private String statusStyleClass(UserStatus status) {
        if (status == UserStatus.ACTIVE) {
            return "admin-status-active";
        }
        if (status == UserStatus.BANNED) {
            return "admin-status-banned";
        }
        if (status == UserStatus.SUSPENDED) {
            return "admin-status-suspended";
        }
        return "admin-status-muted";
    }

    private void logoutUser() throws IOException {
        Request<Void> request = new Request<>(RequestType.LOGOUT, null);
        Respond<?> response = SocketClient.getClient().sendMessage(request);
        if (response == null) {
            throw new IOException("No response from server.");
        }
        if (!response.isStatus()) {
            throw new IOException(response.getMessage() != null ? response.getMessage() : "Logout failed.");
        }
        UserData.clear();
    }
}
