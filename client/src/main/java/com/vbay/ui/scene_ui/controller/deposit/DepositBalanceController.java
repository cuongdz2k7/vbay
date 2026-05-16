package com.vbay.ui.scene_ui.controller.deposit;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.dto.userDTO.DepositBalanceRequest;
import com.vbay.shared.dto.userDTO.UserBalanceResponse;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.util.MoneyInput;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DepositBalanceController {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private Label balanceLabel;
    @FXML
    private TextField amountField;

    private Runnable onBack;
    private RealtimeEventListener<UserBalanceUpdatedPayload> balanceListener;
    private boolean disposed;

    @FXML
    private void initialize() {
        disposed = false;
        MoneyInput.install(amountField);
        updateBalanceDisplay(UserData.getAvailableBalance());
        subscribeBalanceUpdates();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Runnable backAction = onBack;
        dispose();
        if (backAction != null) {
            backAction.run();
        }
    }

    @FXML
    private void handleConfirmDeposit(ActionEvent event) {
        BigDecimal amount;
        try {
            amount = MoneyInput.parseRequired(amountField, "Deposit amount");
        } catch (IllegalArgumentException exception) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid deposit amount", exception.getMessage());
            amountField.requestFocus();
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid deposit amount", "Deposit amount must be greater than 0.");
            amountField.requestFocus();
            return;
        }

        try {
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.DEPOSIT_BALANCE, new DepositBalanceRequest(amount))
            );
            if (response == null || !response.isStatus()) {
                NotificationManager.show(NotificationManager.NotificationType.ERROR, "Deposit failed", response != null ? response.getMessage() : "No response from server.");
                return;
            }

            UserBalanceResponse balanceResponse = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), UserBalanceResponse.class);
            if (balanceResponse != null) {
                UserData.setBalances(balanceResponse.getAvailableBalance(), balanceResponse.getHoldBalance());
                updateBalanceDisplay(balanceResponse.getAvailableBalance());
            }
            NotificationManager.show(NotificationManager.NotificationType.INFO, "Deposit complete", "Deposit request for $" + amount.toPlainString() + " was confirmed.");
            amountField.clear();
        } catch (Exception exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Deposit failed", exception.getMessage());
        }
    }

    public void dispose() {
        disposed = true;
        if (balanceListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.USER_BALANCE_UPDATED,
                balanceListener
            );
            balanceListener = null;
        }
        if (amountField != null) {
            amountField.clear();
        }
        onBack = null;
    }

    private void subscribeBalanceUpdates() {
        balanceListener = event -> {
            UserBalanceUpdatedPayload payload = event.getPayload();
            Long userId = UserData.getUserId();
            if (payload == null || userId == null || payload.getUserId() != userId) {
                return;
            }
            Platform.runLater(() -> {
                if (disposed || balanceLabel == null) {
                    return;
                }
                UserData.setBalances(payload.getAvailableBalance(), payload.getHoldBalance());
                updateBalanceDisplay(payload.getAvailableBalance());
            });
        };
        SocketClient.getClient().getRealtimeEventDispatcher().subscribe(
            RealtimeEventType.USER_BALANCE_UPDATED,
            balanceListener
        );
    }

    private void updateBalanceDisplay(BigDecimal availableBalance) {
        BigDecimal balance = availableBalance == null ? BigDecimal.ZERO : availableBalance;
        balanceLabel.setText(CURRENCY_FORMAT.format(balance));
    }

}
