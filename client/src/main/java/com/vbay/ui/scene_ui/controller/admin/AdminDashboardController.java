package com.vbay.ui.scene_ui.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.adminDTO.AdminAuctionActionRequest;
import com.vbay.shared.dto.adminDTO.AdminAuctionItem;
import com.vbay.shared.dto.adminDTO.AdminAuctionListResponse;
import com.vbay.shared.dto.adminDTO.AdminLockUserRequest;
import com.vbay.shared.dto.adminDTO.AdminUserActionRequest;
import com.vbay.shared.dto.adminDTO.AdminUserItem;
import com.vbay.shared.dto.adminDTO.AdminUserListResponse;
import com.vbay.shared.dto.adminDTO.AdminDepositItem;
import com.vbay.shared.dto.adminDTO.AdminDepositListResponse;
import com.vbay.shared.dto.adminDTO.AdminDepositActionRequest;
import com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.auction.AuctionStatus;
import com.vbay.shared.enums.auth.UserStatus;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminDashboardController {
    private static final Logger LOGGER = LoggingUtils.getLogger(AdminDashboardController.class);

    @FXML private Button userManagementBtn;
    @FXML private Button auctionManagementBtn;
    @FXML private Button depositManagementBtn;
    @FXML private Label accountNameLabel;
    @FXML private VBox userManagementView;
    @FXML private VBox auctionManagementView;
    @FXML private VBox depositManagementView;

    @FXML private TableView<AdminUserItem> usersTable;
    @FXML private TableColumn<AdminUserItem, Long> userIdCol;
    @FXML private TableColumn<AdminUserItem, String> usernameCol;
    @FXML private TableColumn<AdminUserItem, String> userEmailCol;
    @FXML private TableColumn<AdminUserItem, UserStatus> userStatusCol;
    @FXML private TableColumn<AdminUserItem, Integer> userWarnCol;
    @FXML private TableColumn<AdminUserItem, String> userLockCol;
    @FXML private TableColumn<AdminUserItem, Void> userActionCol;

    @FXML private TableView<AdminAuctionItem> auctionsTable;
    @FXML private TableColumn<AdminAuctionItem, Long> auctionIdCol;
    @FXML private TableColumn<AdminAuctionItem, Long> auctionSellerCol;
    @FXML private TableColumn<AdminAuctionItem, String> auctionTitleCol;
    @FXML private TableColumn<AdminAuctionItem, AuctionStatus> auctionStatusCol;
    @FXML private TableColumn<AdminAuctionItem, String> auctionPriceCol;
    @FXML private TableColumn<AdminAuctionItem, String> auctionEndCol;
    @FXML private TableColumn<AdminAuctionItem, Void> auctionActionCol;

    @FXML private TableView<AdminDepositItem> depositsTable;
    @FXML private TableColumn<AdminDepositItem, Long> depositIdCol;
    @FXML private TableColumn<AdminDepositItem, Long> depositUserIdCol;
    @FXML private TableColumn<AdminDepositItem, String> depositUsernameCol;
    @FXML private TableColumn<AdminDepositItem, java.math.BigDecimal> depositAmountCol;
    @FXML private TableColumn<AdminDepositItem, com.vbay.shared.enums.payment.DepositRequestStatus> depositStatusCol;
    @FXML private TableColumn<AdminDepositItem, String> depositTimeCol;
    @FXML private TableColumn<AdminDepositItem, Void> depositActionCol;

    private final ObservableList<AdminUserItem> usersData = FXCollections.observableArrayList();
    private final ObservableList<AdminAuctionItem> auctionsData = FXCollections.observableArrayList();
    private final ObservableList<AdminDepositItem> depositsData = FXCollections.observableArrayList();

    private com.vbay.network.dispatcher.RealtimeEventListener<DepositRequestPayload> depositRequestedListener;

    @FXML
    private void initialize() {
        String username = UserData.getUsername();
        if (username != null && !username.isEmpty()) {
            username = username.substring(0, 1).toUpperCase() + username.substring(1);
            accountNameLabel.setText(username);
        } else {
            accountNameLabel.setText("Admin");
        }

        setupUsersTable();
        setupAuctionsTable();
        setupDepositsTable();
        subscribeRealtime();

        showUserManagement(null);
    }

    private void setActiveNav(Button activeBtn) {
        userManagementBtn.getStyleClass().remove("active-nav");
        auctionManagementBtn.getStyleClass().remove("active-nav");
        if (depositManagementBtn != null) {
            depositManagementBtn.getStyleClass().remove("active-nav");
        }
        
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active-nav")) {
            activeBtn.getStyleClass().add("active-nav");
        }
        
        userManagementBtn.applyCss();
        auctionManagementBtn.applyCss();
        if (depositManagementBtn != null) {
            depositManagementBtn.applyCss();
        }
    }

    @FXML
    private void showUserManagement(ActionEvent event) {
        userManagementView.setVisible(true);
        userManagementView.setOpacity(1.0);
        auctionManagementView.setVisible(false);
        if (depositManagementView != null) {
            depositManagementView.setVisible(false);
        }
        setActiveNav(userManagementBtn);
        refreshUsers();
    }

    @FXML
    private void showAuctionManagement(ActionEvent event) {
        userManagementView.setVisible(false);
        auctionManagementView.setVisible(true);
        auctionManagementView.setOpacity(1.0);
        if (depositManagementView != null) {
            depositManagementView.setVisible(false);
        }
        setActiveNav(auctionManagementBtn);
        refreshAuctions();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            unsubscribeRealtime();
            SocketClient.getClient().sendMessage(new Request<>(RequestType.LOGOUT, null));
            UserData.clear();
            SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to logout", e);
        }
    }

    private void unsubscribeRealtime() {
        try {
            com.vbay.shared.dto.realtimeDTO.Room room = new com.vbay.shared.dto.realtimeDTO.Room();
            room.setType(com.vbay.shared.enums.realtime.RoomType.AUCTION_LIST);
            SocketClient.getClient().sendMessage(new Request<>(RequestType.UNSUBSCRIBE_ROOM, room));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to unsubscribe from auction lobby room", e);
        }

        if (depositRequestedListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                com.vbay.shared.enums.realtime.RealtimeEventType.ADMIN_DEPOSIT_REQUESTED,
                depositRequestedListener
            );
        }
    }

    @FXML
    private void refreshUsers() {
        new Thread(() -> {
            try {
                Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(RequestType.ADMIN_GET_ALL_USERS, null));
                if (response.isStatus()) {
                    AdminUserListResponse listResp = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), AdminUserListResponse.class);
                    if (listResp.getUsers() != null) {
                        listResp.getUsers().sort(java.util.Comparator.comparingLong(AdminUserItem::getUserId));
                    }
                    Platform.runLater(() -> usersData.setAll(listResp.getUsers()));
                } else {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Could not fetch users"));
            }
        }).start();
    }

    @FXML
    private void refreshAuctions() {
        new Thread(() -> {
            try {
                Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(RequestType.ADMIN_GET_ALL_AUCTIONS, null));
                if (response.isStatus()) {
                    AdminAuctionListResponse listResp = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), AdminAuctionListResponse.class);
                    if (listResp.getAuctions() != null) {
                        listResp.getAuctions().sort(java.util.Comparator.comparingLong(AdminAuctionItem::getAuctionId));
                    }
                    Platform.runLater(() -> auctionsData.setAll(listResp.getAuctions()));
                } else {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Could not fetch auctions"));
            }
        }).start();
    }

    private String formatVietnamTime(String utcStr) {
        if (utcStr == null || utcStr.trim().isEmpty() || "null".equalsIgnoreCase(utcStr)) {
            return "";
        }
        try {
            // Server LocalDateTime.toString() returns ISO format like: 2026-05-20T00:07:14
            java.time.LocalDateTime utcTime = java.time.LocalDateTime.parse(utcStr);
            java.time.ZonedDateTime utcZoned = utcTime.atZone(java.time.ZoneOffset.UTC);
            java.time.ZonedDateTime vnZoned = utcZoned.withZoneSameInstant(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
            return vnZoned.format(formatter) + " (ICT)";
        } catch (Exception e) {
            return utcStr;
        }
    }

    private void setupUsersTable() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userWarnCol.setCellValueFactory(new PropertyValueFactory<>("warningCount"));
        
        userStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        userStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(UserStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toString());
                    label.getStyleClass().add("status-badge");
                    switch (item) {
                        case ACTIVE -> label.getStyleClass().add("active");
                        case BANNED, DELETED -> label.getStyleClass().add("banned");
                        case LOCKED -> label.getStyleClass().add("locked");
                        default -> label.getStyleClass().add("ended");
                    }
                    setGraphic(label);
                }
            }
        });
        
        userLockCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatVietnamTime(cellData.getValue().getLockUntil())));

        userActionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminUserItem user = getTableRow().getItem();
                HBox box = new HBox(5);
                
                if (user.getStatus() != UserStatus.BANNED && user.getStatus() != UserStatus.DELETED) {
                    Button banBtn = new Button("Ban");
                    banBtn.getStyleClass().addAll("table-action-btn", "ban-btn");
                    banBtn.setOnAction(e -> handleUserAction(RequestType.ADMIN_BAN_USER, user.getUserId(), "Ban Reason:"));
                    
                    Button kickBtn = new Button("Kick");
                    kickBtn.getStyleClass().addAll("table-action-btn", "kick-btn");
                    kickBtn.setOnAction(e -> handleUserAction(RequestType.ADMIN_KICK_USER, user.getUserId(), "Kick Reason:"));
                    
                    Button warnBtn = new Button("Warn");
                    warnBtn.getStyleClass().addAll("table-action-btn", "warn-btn");
                    warnBtn.setOnAction(e -> handleUserAction(RequestType.ADMIN_WARN_USER, user.getUserId(), "Warning Reason:"));
                    
                    Button lockBtn = new Button("Lock");
                    lockBtn.getStyleClass().addAll("table-action-btn", "lock-btn");
                    lockBtn.setOnAction(e -> handleLockUser(user.getUserId()));
                    
                    box.getChildren().addAll(kickBtn, warnBtn, lockBtn, banBtn);
                }
                
                setGraphic(box);
            }
        });

        usersTable.setItems(usersData);
    }

    private void setupAuctionsTable() {
        auctionIdCol.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        auctionSellerCol.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        auctionTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        auctionStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        auctionStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(AuctionStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status.name());
                    label.getStyleClass().add("status-badge");
                    switch (status) {
                        case ACTIVE, SCHEDULED -> label.getStyleClass().add("active");
                        case STOPPED -> label.getStyleClass().add("stopped");
                        case CANCELLED, FAILED -> label.getStyleClass().add("banned");
                        default -> label.getStyleClass().add("ended");
                    }
                    setGraphic(label);
                }
            }
        });
        
        auctionPriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrentPrice().toString()));
        auctionEndCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndingTime().toString()));
        
        auctionActionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminAuctionItem auction = getTableRow().getItem();
                HBox box = new HBox(5);
                
                if (auction.getStatus() == AuctionStatus.ACTIVE || auction.getStatus() == AuctionStatus.SCHEDULED) {
                    Button stopBtn = new Button("Stop");
                    stopBtn.getStyleClass().addAll("table-action-btn", "warning");
                    stopBtn.setOnAction(e -> handleAuctionAction(RequestType.ADMIN_STOP_AUCTION, auction.getAuctionId(), "Stop Reason:"));
                    box.getChildren().add(stopBtn);
                } else if (auction.getStatus() == AuctionStatus.STOPPED) {
                    Button continueBtn = new Button("Continue");
                    continueBtn.getStyleClass().addAll("table-action-btn", "active");
                    continueBtn.setOnAction(e -> handleAuctionAction(RequestType.ADMIN_CONTINUE_AUCTION, auction.getAuctionId(), "Continue Reason:"));
                    box.getChildren().add(continueBtn);
                }
                
                if (auction.getStatus() != AuctionStatus.CANCELLED && auction.getStatus() != AuctionStatus.ENDED) {
                    Button delBtn = new Button("Delete");
                    delBtn.getStyleClass().addAll("table-action-btn", "danger");
                    delBtn.setOnAction(e -> handleAuctionAction(RequestType.ADMIN_DELETE_AUCTION, auction.getAuctionId(), "Delete Reason:"));
                    box.getChildren().add(delBtn);
                }

                setGraphic(box);
            }
        });

        auctionsTable.setItems(auctionsData);
    }
    //Helper css styles (Popup dialogs)
    private void styleDialog(javafx.scene.control.Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/jfx/css/AdminDashboard.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        // Create custom red triangle with white question mark icon
        javafx.scene.shape.SVGPath triangle = new javafx.scene.shape.SVGPath();
        triangle.setContent("M12 2L2 22h20L12 2z");
        triangle.setFill(javafx.scene.paint.Color.web("#e53935"));
        triangle.setStroke(javafx.scene.paint.Color.TRANSPARENT);
        triangle.setScaleX(1.8);
        triangle.setScaleY(1.8);

        Label qLabel = new Label("?");
        qLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        qLabel.setTranslateY(2); // Shift question mark down slightly to center inside triangle

        javafx.scene.layout.StackPane customIcon = new javafx.scene.layout.StackPane(triangle, qLabel);
        customIcon.setPrefWidth(40);
        customIcon.setPrefHeight(40);
        dialog.setGraphic(customIcon);
        
        // Set the stage style to TRANSPARENT before showing
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        
        // Configure Scene and Window once the dialog starts showing (ensuring scene is fully initialized)
        dialog.setOnShowing(event -> {
            javafx.scene.Scene scene = dialog.getDialogPane().getScene();
            if (scene != null) {
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
                if (stage != null) {
                    // Mouse drag handlers so users can move the frameless window naturally
                    final double[] xOffset = new double[1];
                    final double[] yOffset = new double[1];
                    dialog.getDialogPane().setOnMousePressed(me -> {
                        xOffset[0] = me.getSceneX();
                        yOffset[0] = me.getSceneY();
                    });
                    dialog.getDialogPane().setOnMouseDragged(me -> {
                        stage.setX(me.getScreenX() - xOffset[0]);
                        stage.setY(me.getScreenY() - yOffset[0]);
                    });
                }
            }
        });
    }

    private void handleUserAction(RequestType type, long targetUserId, String promptText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Admin Action");
        dialog.setHeaderText(promptText);
        dialog.setContentText("Reason:");
        styleDialog(dialog);
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            new Thread(() -> {
                try {
                    AdminUserActionRequest req = new AdminUserActionRequest(targetUserId, result.get());
                    Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(type, req));
                    if (response.isStatus()) {
                        Platform.runLater(() -> {
                            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Success", "Action completed");
                            refreshUsers();
                        });
                    } else {
                        Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Failed to send request"));
                }
            }).start();
        }
    }

    private void handleLockUser(long targetUserId) {
        TextInputDialog durationDialog = new TextInputDialog("30");
        durationDialog.setTitle("Lock User");
        durationDialog.setHeaderText("Enter lock duration in minutes:");
        styleDialog(durationDialog);
        Optional<String> durationResult = durationDialog.showAndWait();
        if (durationResult.isEmpty()) return;
        
        int minutes;
        try {
            minutes = Integer.parseInt(durationResult.get());
        } catch (NumberFormatException e) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Invalid Input", "Please enter a valid number");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Lock User");
        reasonDialog.setHeaderText("Lock Reason:");
        styleDialog(reasonDialog);
        Optional<String> reasonResult = reasonDialog.showAndWait();
        
        if (reasonResult.isPresent()) {
            new Thread(() -> {
                try {
                    AdminLockUserRequest req = new AdminLockUserRequest(targetUserId, minutes, reasonResult.get());
                    Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(RequestType.ADMIN_LOCK_USER, req));
                    if (response.isStatus()) {
                        Platform.runLater(() -> {
                            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Success", "User locked");
                            refreshUsers();
                        });
                    } else {
                        Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Failed to send request"));
                }
            }).start();
        }
    }

    private void handleAuctionAction(RequestType type, long auctionId, String promptText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Admin Action");
        dialog.setHeaderText(promptText);
        dialog.setContentText("Reason:");
        styleDialog(dialog);
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            new Thread(() -> {
                try {
                    AdminAuctionActionRequest req = new AdminAuctionActionRequest(auctionId, result.get());
                    Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(type, req));
                    if (response.isStatus()) {
                        Platform.runLater(() -> {
                            NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Success", "Action completed");
                            refreshAuctions();
                        });
                    } else {
                        Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Failed to send request"));
                }
            }).start();
        }
    }

    private void setupDepositsTable() {
        depositIdCol.setCellValueFactory(new PropertyValueFactory<>("depositId"));
        depositUserIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        depositUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        depositAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        depositTimeCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        depositStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        depositStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(com.vbay.shared.enums.payment.DepositRequestStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status.name());
                    label.getStyleClass().add("status-badge");
                    if (status == com.vbay.shared.enums.payment.DepositRequestStatus.PENDING) {
                        label.getStyleClass().add("locked");
                    } else if (status == com.vbay.shared.enums.payment.DepositRequestStatus.APPROVED) {
                        label.getStyleClass().add("active");
                    } else {
                        label.getStyleClass().add("banned");
                    }
                    setGraphic(label);
                }
            }
        });

        depositActionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminDepositItem deposit = getTableRow().getItem();
                HBox box = new HBox(5);

                if (deposit.getStatus() == com.vbay.shared.enums.payment.DepositRequestStatus.PENDING) {
                    Button approveBtn = new Button("Approve");
                    approveBtn.getStyleClass().addAll("table-action-btn", "active");
                    approveBtn.setOnAction(e -> handleDepositAction(RequestType.ADMIN_APPROVE_DEPOSIT, deposit.getDepositId()));

                    Button rejectBtn = new Button("Reject");
                    rejectBtn.getStyleClass().addAll("table-action-btn", "ban-btn");
                    rejectBtn.setOnAction(e -> handleDepositAction(RequestType.ADMIN_REJECT_DEPOSIT, deposit.getDepositId()));

                    box.getChildren().addAll(approveBtn, rejectBtn);
                }

                setGraphic(box);
            }
        });

        depositsTable.setItems(depositsData);
    }

    @FXML
    private void showDepositManagement(ActionEvent event) {
        userManagementView.setVisible(false);
        auctionManagementView.setVisible(false);
        if (depositManagementView != null) {
            depositManagementView.setVisible(true);
            depositManagementView.setOpacity(1.0);
        }
        setActiveNav(depositManagementBtn);
        refreshDeposits();
    }

    @FXML
    private void refreshDeposits() {
        new Thread(() -> {
            try {
                Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(RequestType.ADMIN_GET_PENDING_DEPOSITS, null));
                if (response.isStatus()) {
                    AdminDepositListResponse listResp = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), AdminDepositListResponse.class);
                    Platform.runLater(() -> {
                        depositsData.setAll(listResp.getDeposits());
                    });
                } else {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Could not fetch deposit requests"));
            }
        }).start();
    }

    private void handleDepositAction(RequestType actionType, long depositId) {
        new Thread(() -> {
            try {
                Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(actionType, new AdminDepositActionRequest(depositId)));
                if (response.isStatus()) {
                    Platform.runLater(() -> {
                        NotificationManager.show(NotificationManager.NotificationType.SUCCESS, "Success", response.getMessage());
                        refreshDeposits();
                    });
                } else {
                    Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Error", response.getMessage()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> NotificationManager.show(NotificationManager.NotificationType.ERROR, "Network Error", "Could not perform action"));
            }
        }).start();
    }

    private void subscribeRealtime() {
        try {
            com.vbay.shared.dto.realtimeDTO.Room room = new com.vbay.shared.dto.realtimeDTO.Room();
            room.setType(com.vbay.shared.enums.realtime.RoomType.AUCTION_LIST);
            SocketClient.getClient().sendMessage(new Request<>(RequestType.SUBSCRIBE_ROOM, room));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to subscribe to auction lobby room", e);
        }

        depositRequestedListener = event -> {
            DepositRequestPayload payload = event.getPayload();
            if (payload == null) return;
            Platform.runLater(() -> {
                NotificationManager.show(NotificationManager.NotificationType.INFO, "New Deposit Request", "User " + payload.getUsername() + " requested a deposit of $" + payload.getAmount());
                if (depositManagementView.isVisible()) {
                    refreshDeposits();
                }
            });
        };
        SocketClient.getClient().getRealtimeEventDispatcher().subscribe(
            com.vbay.shared.enums.realtime.RealtimeEventType.ADMIN_DEPOSIT_REQUESTED,
            depositRequestedListener
        );
    }
}
