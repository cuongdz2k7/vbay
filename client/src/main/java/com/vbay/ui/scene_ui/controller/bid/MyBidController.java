package com.vbay.ui.scene_ui.controller.bid;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.LongConsumer;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.network.dispatcher.RealtimeEventDispatcher;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Request;
import com.vbay.ui.util.ProductImageLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MyBidController {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final String DEFAULT_IMAGE = "/jfx/image/products/collectibles.png";
    private static final String FILTER_ALL = "All Statuses";
    private static final String FILTER_WINNING = "Winning";
    private static final String FILTER_OUTBID = "Outbid";
    private static final String FILTER_WON = "Won";
    private static final String FILTER_LOST = "Lost";
    private static final String FILTER_CANCELLED = "Canceled";

    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private VBox rowsBox;
    @FXML
    private Label emptyStateLabel;

    private final Map<Long, MyBidListItemPayload> bidItemsByAuctionId = new LinkedHashMap<>();
    private final Map<Long, Label> timeLabels = new HashMap<>();
    private RealtimeEventListener<MyBidListItemPayload> myBidListener;
    private Timeline timeUpdater;
    private LongConsumer onAuctionSelected;
    private boolean disposed;

    @FXML
    private void initialize() {
        disposed = false;
        statusFilterComboBox.setValue(FILTER_ALL);
        statusFilterComboBox.setOnAction(event -> renderRows());
        subscribeUserRoom();
        subscribeRealtime();
        startTimeUpdater();
    }
    ///lúc select acution mới bắt đầu có auctionId....
    public void setOnAuctionSelected(LongConsumer onAuctionSelected) {
        this.onAuctionSelected = onAuctionSelected;
    }

    public void dispose() {
        disposed = true;
        stopTimeUpdater();
        bidItemsByAuctionId.clear();
        timeLabels.clear();
        if (rowsBox != null) {
            rowsBox.getChildren().clear();
        }
        if (statusFilterComboBox != null) {
            statusFilterComboBox.setOnAction(null);
        }
        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(false);
            emptyStateLabel.setManaged(false);
        }
        if (myBidListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.MY_BID_LIST_ITEM_UPDATED,
                myBidListener
            );
            myBidListener = null;
        }
        onAuctionSelected = null;
    }

    public void setInitialItems(Collection<MyBidListItemPayload> items) {
        if (items != null) {
            for (MyBidListItemPayload item : items) {
                mergeBidItem(item);
            }
        }
        renderRows();
    }

    private void subscribeRealtime() {
        if (myBidListener != null) {
            return;
        }
        myBidListener = this::handleMyBidUpdated;
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();
        dispatcher.subscribe(RealtimeEventType.MY_BID_LIST_ITEM_UPDATED, myBidListener);
    }

    private void subscribeUserRoom() {
        Long currentUserId = UserData.getUserId();
        if (currentUserId == null) {
            return;
        }

        Room userRoom = new Room();
        userRoom.setType(RoomType.USER);
        userRoom.setTargetId(currentUserId);
        try {
            SocketClient.getClient().sendMessage(new Request<>(RequestType.SUBSCRIBE_ROOM, userRoom));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void handleMyBidUpdated(RealtimeEvent<MyBidListItemPayload> event) {
        MyBidListItemPayload item = event.getPayload();
        if (item == null) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            if (mergeBidItem(item)) {
                renderRows();
            }
        });
    }

    private boolean mergeBidItem(MyBidListItemPayload item) {
        if (item == null) {
            return false;
        }
        MyBidListItemPayload existing = bidItemsByAuctionId.get(item.getAuctionId());
        if (isStaleItem(item, existing)) {
            return false;
        }
        bidItemsByAuctionId.put(item.getAuctionId(), item);
        return true;
    }

    private boolean isStaleItem(MyBidListItemPayload incoming, MyBidListItemPayload existing) {
        if (existing == null) {
            return false;
        }
        if (incoming.getAuctionVersion() != existing.getAuctionVersion()) {
            return incoming.getAuctionVersion() < existing.getAuctionVersion();
        }
        LocalDateTime incomingUpdatedAt = incoming.getUpdatedAt();
        LocalDateTime existingUpdatedAt = existing.getUpdatedAt();
        return incomingUpdatedAt != null
            && existingUpdatedAt != null
            && incomingUpdatedAt.isBefore(existingUpdatedAt);
    }

    private void renderRows() {
        rowsBox.getChildren().clear();
        timeLabels.clear();
        List<MyBidListItemPayload> visibleItems = filteredItems();

        boolean empty = visibleItems.isEmpty();
        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);

        for (MyBidListItemPayload item : visibleItems) {
            rowsBox.getChildren().add(createRow(item));
        }
    }

    private List<MyBidListItemPayload> filteredItems() {
        String selectedStatus = statusForFilter(statusFilterComboBox.getValue());
        return bidItemsByAuctionId.values().stream()
            .filter(item -> selectedStatus == null || selectedStatus.equals(statusText(item)))
            .sorted(Comparator.comparing(this::sortTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private LocalDateTime sortTime(MyBidListItemPayload item) {
        return item.getUpdatedAt() == null ? item.getBidTime() : item.getUpdatedAt();
    }

    private String statusForFilter(String selectedFilter) {
        return switch (selectedFilter == null ? FILTER_ALL : selectedFilter) {
            case FILTER_WINNING -> "WINNING";
            case FILTER_OUTBID -> "OUTBID";
            case FILTER_WON -> "WON";
            case FILTER_LOST -> "LOST";
            case FILTER_CANCELLED -> "CANCELLED";
            default -> null;
        };
    }

    private Node createRow(MyBidListItemPayload item) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("my-bid-row");
        row.setFocusTraversable(true);
        row.setCursor(Cursor.HAND);
        row.setOnMouseClicked(event -> openAuction(item));
        row.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                openAuction(item);
                event.consume();
            }
        });

        row.getChildren().add(productCell(item));
        row.getChildren().add(priceLabel(formatCurrency(item.getCurrentPrice()), "my-bid-price", "my-bid-col-price"));
        row.getChildren().add(priceLabel(formatCurrency(item.getMyBidAmount()), "my-bid-muted-price", "my-bid-col-price"));
        row.getChildren().add(statusCell(item));
        row.getChildren().add(timeLabel(item));
        row.getChildren().add(priceLabel(sourceText(item), "my-bid-source", "my-bid-col-source"));
        return row;
    }

    private Node productCell(MyBidListItemPayload item) {
        HBox cell = new HBox(16);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.getStyleClass().add("my-bid-col-product");

        VBox imageFrame = new VBox();
        imageFrame.getStyleClass().add("my-bid-product-image-frame");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(42);
        imageView.setFitHeight(42);
        ProductImageLoader.loadCover(imageView, imagePath(item));
        imageFrame.getChildren().add(imageView);

        VBox textBox = new VBox(3);
        textBox.setMaxWidth(220);
        Label title = new Label(safeText(item.getAuctionTitle()));
        title.setWrapText(false);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.setMaxWidth(220);
        title.getStyleClass().add("my-bid-product-title");
        Label meta = new Label("Lot #" + item.getAuctionId());
        meta.getStyleClass().add("my-bid-product-meta");
        textBox.getChildren().addAll(title, meta);

        cell.getChildren().addAll(imageFrame, textBox);
        return cell;
    }

    private Node statusCell(MyBidListItemPayload item) {
        VBox cell = new VBox();
        cell.getStyleClass().add("my-bid-col-status");

        Label status = new Label(statusText(item));
        status.getStyleClass().add(statusStyle(item));
        cell.getChildren().add(status);
        return cell;
    }

    private Label timeLabel(MyBidListItemPayload item) {
        Label label = priceLabel(formatTimeLeft(item), timeStyle(item), "my-bid-col-time");
        timeLabels.put(item.getAuctionId(), label);
        return label;
    }

    private Label priceLabel(String text, String textStyle, String columnStyle) {
        Label label = new Label(text);
        label.getStyleClass().addAll(textStyle, columnStyle);
        return label;
    }

    private String statusText(MyBidListItemPayload item) {
        String status = item.getBidStatus() == null ? null : item.getBidStatus().name();
        return status == null || status.isBlank() ? "-" : status;
    }

    private String statusStyle(MyBidListItemPayload item) {
        return switch (statusText(item)) {
            case "WON" -> "my-bid-status-won";
            case "WINNING" -> "my-bid-status-winning";
            case "OUTBID" -> "my-bid-status-outbid";
            case "LOST" -> "my-bid-status-lost";
            case "CANCELLED" -> "my-bid-status-cancelled";
            default -> "my-bid-status-ended";
        };
    }

    private String sourceText(MyBidListItemPayload item) {
        if (item.getBidSource() == BidSource.AUTO_BID) {
            return "Auto Bid";
        }
        return "Manual";
    }

    private String formatTimeLeft(MyBidListItemPayload item) {
        if (!"ACTIVE".equals(item.getAuctionStatus())) {
            return "Ended";
        }
        LocalDateTime endingTime = item.getEndingTime();
        if (endingTime == null) {
            return "-";
        }
        Duration remaining = Duration.between(LocalDateTime.now(UTC_ZONE), endingTime);
        if (remaining.isNegative() || remaining.isZero()) {
            return "Ended";
        }
        long seconds = remaining.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
        }
        return String.format("%02dm %02ds", minutes, remainingSeconds);
    }

    private String timeStyle(MyBidListItemPayload item) {
        return "Ended".equals(formatTimeLeft(item)) ? "my-bid-time-ended" : "my-bid-time";
    }

    private void updateTimeLabels() {
        for (Map.Entry<Long, Label> entry : timeLabels.entrySet()) {
            MyBidListItemPayload item = bidItemsByAuctionId.get(entry.getKey());
            Label label = entry.getValue();
            if (item == null || label == null) {
                continue;
            }
            label.setText(formatTimeLeft(item));
            label.getStyleClass().removeAll("my-bid-time", "my-bid-time-ended");
            label.getStyleClass().add(timeStyle(item));
        }
    }

    private void openAuction(MyBidListItemPayload item) {
        if (onAuctionSelected != null) {
            onAuctionSelected.accept(item.getAuctionId());
        }
    }

    private String imagePath(MyBidListItemPayload item) {
        return item.getThumbnailUrl() == null || item.getThumbnailUrl().isBlank()
            ? DEFAULT_IMAGE
            : item.getThumbnailUrl();
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatCurrency(BigDecimal value) {
        return value == null ? "-" : CURRENCY_FORMAT.format(value);
    }

    private void startTimeUpdater() {
        updateTimeLabels();
        timeUpdater = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> updateTimeLabels()));
        timeUpdater.setCycleCount(Timeline.INDEFINITE);
        timeUpdater.play();
    }

    private void stopTimeUpdater() {
        if (timeUpdater != null) {
            timeUpdater.stop();
            timeUpdater = null;
        }
    }
}
