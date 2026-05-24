package com.vbay.ui.scene_ui.controller.home;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.network.SocketClient;
import com.vbay.network.UserData;
import com.vbay.network.dispatcher.RealtimeEventDispatcher;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.auctionDTO.AuctionDetailRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListResponse;
import com.vbay.shared.dto.auctionDTO.MyBidListResponse;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AutobidUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserWarnedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.ViewerAuctionBidSummaryPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.Auction;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.scene_ui.controller.auction.CreateAuctionController;
import com.vbay.ui.scene_ui.controller.bid.BidController;
import com.vbay.ui.scene_ui.controller.bid.MyBidController;
import com.vbay.ui.scene_ui.controller.bid.MyAuctionController;
import com.vbay.ui.scene_ui.controller.card.AuctionCardController;
import com.vbay.ui.scene_ui.controller.deposit.DepositBalanceController;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


/*
Thêm disposed guard ngăn task cũ đã queue chạy sau khi màn đã rời.
Đây không phải vấn đề map thread-safe, mà là lifecycle của JavaFX controller với event async.
Ví dụ trong DepositBalanceController:

balanceListener = event -> {
    UserBalanceUpdatedPayload payload = event.getPayload();

    Platform.runLater(() -> {
        ClientAuthSession.setBalances(...);
        updateBalanceDisplay(...);
    });
};
Luồng nguy hiểm:

T1: Server gửi USER_BALANCE_UPDATED
T2: listener chạy trên socket/background thread
T3: listener gọi Platform.runLater(...)
    -> JavaFX queue đã có task "update balance label"

T4: User bấm Back
T5: dispose() chạy
    -> unsubscribe listener
    -> amountField.clear()
    -> onBack = null
    -> màn Deposit bị tháo khỏi Home

T6: JavaFX thread chạy task đã queue từ T3
    -> updateBalanceDisplay(...)
    -> đụng vào controller/màn hình đã dispose

Với HomeController sau logout thì nguy hiểm thật vì task cũ có thể chạy sau khi:

ClientAuthSession.clear();
clearRealtimeCaches();
SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
Nếu task cũ chạy sau đó, nó có thể:

ghi lại ClientAuthSession.setBalances(...) dù user đã logout
mutate cache myBidItemsByAuctionId, activeViewAuctions
render lại node của Home đã không còn là màn hiện tại
giữ reference controller lâu hơn cần thiết
Sai session sau logout
Ví dụ user A logout, nhưng trước đó có event balance của user A đã được queue:

Platform.runLater(() -> {
    ClientAuthSession.setBalances(...);
});
Sau logout bạn đã gọi:

ClientAuthSession.clear();
Nhưng task cũ chạy sau đó lại set balance vào ClientAuthSession.

Kết quả có thể là:

login screen nhưng session global lại có balance cũ
user B login sau đó có thể thấy balance/display bị nhiễm từ user A nếu code nào đó đọc session trước khi response login mới set lại
debug rất khó vì lỗi xảy ra theo timing
Cache bị mutate sau khi đã clear
Logout thường làm:

clearRealtimeCaches();
Nhưng task cũ chạy sau đó:

myBidItemsByAuctionId.put(...);
activeViewAuctions.put(...);
Vậy cache tưởng đã sạch nhưng lại có dữ liệu cũ.

Nếu controller chưa bị GC ngay, hoặc vì lý do nào đó reference còn tồn tại, cache cũ vẫn sống. Lần sau nếu code reuse hoặc callback nào đó chạm tới nó, dữ liệu đã không còn đáng tin.

Render vào UI node đã rời scene
Home đã switch sang login rồi, nhưng task cũ gọi:

renderActiveView();
renderMyBidPreview();
balanceLabel.setText(...);
Node đó không còn hiển thị, nhưng object vẫn tồn tại trong memory. Thường không crash ngay, nhưng có thể:

tạo thêm card/node mới vô ích
chạy timeline/tạo controller con phụ nếu render load FXML
làm UI lag do render màn không còn dùng
gây bug lạ nếu node/scene đã null hoặc stylesheet/scene reference không còn hợp lệ ở code khác
Memory leak / giữ controller sống lâu hơn
Lambda trong Platform.runLater giữ reference tới this:

Platform.runLater(() -> {
    renderActiveView(); // this HomeController
});
Chừng nào task đó chưa chạy, JavaFX queue giữ controller lại. Nếu event nhiều, controller cũ bị giữ lâu hơn, kèm theo:

cache maps
nodes
child controllers
image references
listeners/timelines nếu dispose chưa sạch


structure tốt hơn cho frontend:
Mỗi màn mở lên:
1. fetch snapshot mới nhất từ server
2. render snapshot đó
3. subscribe đúng room/event màn đó cần
4. merge realtime event lên snapshot bằng version/updatedAt
5. dispose thì unsubscribe + stop timer + bỏ state màn đó
*/

public class HomeController {
    private static final Logger LOGGER = LoggingUtils.getLogger(HomeController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter SAME_DAY_BID_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);
    private static final DateTimeFormatter SAME_YEAR_BID_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.US);
    private static final DateTimeFormatter OTHER_YEAR_BID_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final String CREATE_AUCTION_VIEW = "/jfx/scene/CreateAuction.fxml";
    private static final String CREATE_AUCTION_CSS = "/jfx/css/CreateAuction.css";
    private static final String BID_VIEW = "/jfx/scene/bid/Bid.fxml";
    private static final String BID_CSS = "/jfx/css/Bid.css";
    private static final String MY_BID_VIEW = "/jfx/scene/bid/MyBid.fxml";
    private static final String MY_BID_CSS = "/jfx/css/MyBid.css";
    private static final String MY_AUCTION_VIEW = "/jfx/scene/bid/MyAuction.fxml";
    private static final String MY_AUCTION_CSS = "/jfx/css/MyAuction.css";
    private static final String DEPOSIT_BALANCE_VIEW = "/jfx/scene/DepositBalance.fxml";

    private static final String VIEW_HOME = "Home";
    private static final String VIEW_COMING_SOON = "Coming Soon";
    private static final String VIEW_LIVE_AUCTION = "Live Auction";
    private static final String VIEW_BID_HISTORY = "Personal Bid";
    private static final String VIEW_MY_AUCTION = "My Auction";

    private static final String CATEGORY_ALL = "All";
    private static final String CATEGORY_ALL_LABEL = "All Categories";
    private static final String CATEGORY_ELECTRONICS = "Electronics";
    private static final String CATEGORY_COLLECTIBLES = "Collectibles";
    private static final String CATEGORY_ARTS = "Arts";
    private static final String CATEGORY_JEWELRY_WATCHES = "Jewelry & Watches";

    @FXML
    private Label balanceLabel;
    @FXML
    private BorderPane homeRoot;
    @FXML
    private Button homeModuleButton;
    @FXML
    private Button comingSoonModuleButton;
    @FXML
    private Button liveAuctionModuleButton;
    @FXML
    private Button bidHistoryModuleButton;
    @FXML
    private Button myAuctionModuleButton;
    @FXML
    private Label sidebarSubtitle;
    @FXML
    private Label accountNameLabel;
    @FXML
    private Label catalogTitle;
    @FXML
    private Label catalogSubtitle;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private VBox homeDashboard;
    @FXML
    private VBox catalogContent;
    @FXML
    private FlowPane comingSoonPreviewFlow;
    @FXML
    private FlowPane liveAuctionPreviewFlow;
    @FXML
    private FlowPane productFlow;
    @FXML
    private VBox myBidPreviewBox;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private HBox paginationBar;

    private final Map<Button, String> moduleButtons = new LinkedHashMap<>();

    private String activeView = VIEW_HOME;
    private String activeCategory = CATEGORY_ALL;
    private Node homeCenter;
    private Node homeLeft;
    private Node homeRight;
    private Node homeBottom;

    //realtime
    private final Map<Long, AuctionListItemPayload> livePreviewAuctions = new LinkedHashMap<>();
    private final Map<Long, AuctionListItemPayload> comingSoonPreviewAuctions = new LinkedHashMap<>();
    private final Map<Long, AuctionListItemPayload> activeViewAuctions = new LinkedHashMap<>();
    private final Map<Long, MyBidListItemPayload> myBidItemsByAuctionId = new LinkedHashMap<>();
    private RealtimeEventListener<AuctionListItemPayload> auctionListItemListener;
    private RealtimeEventListener<UserBalanceUpdatedPayload> userBalanceListener;
    private RealtimeEventListener<MyBidListItemPayload> myBidListener;
    private RealtimeEventListener<DepositRequestPayload> depositRequestListener;
    private RealtimeEventListener<Void> userKickedListener;
    private RealtimeEventListener<UserWarnedPayload> userWarnedListener;
    private RealtimeEventListener<AutobidUpdatedPayload> autobidUpdatedListener;
    private CreateAuctionController activeCreateAuctionController;
    private DepositBalanceController activeDepositController;
    private BidController activeBidController;
    private MyBidController activeMyBidController;
    private MyAuctionController activeMyAuctionController;
    private Long currentUserId;
    private boolean disposed;


    @FXML
    private void initialize() {
        disposed = false;
        homeCenter = homeRoot.getCenter();
        homeLeft = homeRoot.getLeft();
        homeRight = homeRoot.getRight();
        homeBottom = homeRoot.getBottom();
        currentUserId = UserData.getUserId();

        initializeNavigationMaps();
        updateBalanceDisplay(UserData.getAvailableBalance());
        updateAccountDisplay();
        subscribeRealtimeListener();
        subscribeServerRooms();
        loadMyBidItems();

        paginationBar.setVisible(false);
        paginationBar.setManaged(false);
        selectView(VIEW_HOME);
    }

    private void loadMyBidItems() {
        try {
            replaceMyBidItems(fetchMyBidItems());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private List<MyBidListItemPayload> fetchMyBidItems() throws IOException {
        Respond<?> response = SocketClient.getClient().sendMessage(
            new Request<>(RequestType.GET_MY_BID_LIST, null)
        );
        if (response == null || !response.isStatus()) {
            throw new IOException(response != null ? response.getMessage() : "No response");
        }

        MyBidListResponse myBidResponse = JsonUtils.fromJson(
            JsonUtils.toJson(response.getData()),
            MyBidListResponse.class
        );
        if (myBidResponse == null || myBidResponse.getItems() == null) {
            return List.of();
        }
        return myBidResponse.getItems();
    }

    private void replaceMyBidItems(List<MyBidListItemPayload> items) {
        myBidItemsByAuctionId.clear();
        for (MyBidListItemPayload item : items) {
            mergeMyBidItem(item);
        }
    }

    private void loadHomePreviewAuctions() {
        replaceAuctionSnapshot(livePreviewAuctions, fetchAuctionList("ACTIVE", null, null, 3));
        replaceAuctionSnapshot(comingSoonPreviewAuctions, fetchAuctionList("SCHEDULED", null, null, 3));
    }

    private List<AuctionListItemPayload> auctionsForActiveView() {
        return activeViewAuctions.values().stream()
            .sorted(this::compareAuction)
            .toList();
    }


    private void loadActiveViewAuctions() {
        if (VIEW_MY_AUCTION.equals(activeView) && currentUserId == null) {
            activeViewAuctions.entrySet().removeIf(entry -> !matchesActiveViewQuery(entry.getValue()));
            return;
        }

        String status = switch (activeView) {
            case VIEW_LIVE_AUCTION -> "ACTIVE";
            case VIEW_COMING_SOON -> "SCHEDULED";
            default -> null;
        };

        Long categoryId = categoryIdForActiveFilter();
        Long sellerId = VIEW_MY_AUCTION.equals(activeView) ? currentUserId : null;

        activeViewAuctions.entrySet().removeIf(entry -> !matchesActiveViewQuery(entry.getValue()));
        mergeAuctionSnapshot(activeViewAuctions, fetchAuctionList(status, categoryId, sellerId, null));
    }

    private List<AuctionListItemPayload> fetchAuctionList(
        String status, 
        Long categoryId, 
        Long sellerId, 
        Integer limit) {
        try {
            AuctionListRequest request = new AuctionListRequest(status, categoryId, sellerId, limit);
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.GET_AUCTION_LIST, request)
            );
            if (response == null || !response.isStatus()) {
                throw new IOException(response != null ? response.getMessage() : "No response");
            }
            AuctionListResponse listResponse = JsonUtils.fromJson(JsonUtils.toJson(response.getData()),AuctionListResponse.class);

            return listResponse.getItems() != null ? listResponse.getItems() : List.of();            
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Load auctions failed", exception);
            showMessage(NotificationManager.NotificationType.ERROR, "Load auctions failed", exception.getMessage());
            return List.of();
        }
    }

    private void subscribeServerRooms() {
        try {
            Room room = new Room();
            room.setType(RoomType.AUCTION_LIST);

            SocketClient.getClient().sendMessage(
                new Request<>(RequestType.SUBSCRIBE_ROOM, room)
            );
            if (currentUserId != null) {
                Room userRoom = new Room();
                userRoom.setType(RoomType.USER);
                userRoom.setTargetId(currentUserId);
                SocketClient.getClient().sendMessage(
                    new Request<>(RequestType.SUBSCRIBE_ROOM, userRoom)
                );
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Subscribe server rooms failed", exception);
        }
    }


    private void subscribeRealtimeListener() {
        auctionListItemListener = event -> handleAuctionListItemUpdated(event);
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();
        dispatcher.subscribe(
                RealtimeEventType.AUCTION_LIST_ITEM_UPDATED,
                auctionListItemListener
            );
        userBalanceListener = event -> handleUserBalanceUpdated(event);
        dispatcher.subscribe(
            RealtimeEventType.USER_BALANCE_UPDATED,
            userBalanceListener
        );
        myBidListener = event -> handleMyBidListItemUpdated(event);
        dispatcher.subscribe(
            RealtimeEventType.MY_BID_LIST_ITEM_UPDATED,
            myBidListener
        );
        depositRequestListener = event -> handleDepositRequestUpdated(event);
        dispatcher.subscribe(
            RealtimeEventType.DEPOSIT_REQUEST_UPDATED,
            depositRequestListener
        );
        userKickedListener = event -> handleUserKicked(event);
        dispatcher.subscribe(
            RealtimeEventType.ADMIN_USER_KICKED,
            userKickedListener
        );
        userWarnedListener = event -> handleUserWarned(event);
        dispatcher.subscribe(
            RealtimeEventType.ADMIN_USER_WARNED,
            userWarnedListener
        );
        autobidUpdatedListener = event -> handleAutobidUpdated(event);
        dispatcher.subscribe(
            RealtimeEventType.AUTOBID_UPDATED,
            autobidUpdatedListener
        );
    }

    private void unsubscribeRealtimeListener() {
        if (auctionListItemListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.AUCTION_LIST_ITEM_UPDATED,
                auctionListItemListener
            );
            auctionListItemListener = null;
        }
        if (userBalanceListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.USER_BALANCE_UPDATED,
                userBalanceListener
            );
            userBalanceListener = null;
        }
        if (myBidListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.MY_BID_LIST_ITEM_UPDATED,
                myBidListener
            );
            myBidListener = null;
        }
        if (depositRequestListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.DEPOSIT_REQUEST_UPDATED,
                depositRequestListener
            );
            depositRequestListener = null;
        }
        if (userKickedListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.ADMIN_USER_KICKED,
                userKickedListener
            );
            userKickedListener = null;
        }
        if (userWarnedListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.ADMIN_USER_WARNED,
                userWarnedListener
            );
            userWarnedListener = null;
        }
        if (autobidUpdatedListener != null) {
            SocketClient.getClient().getRealtimeEventDispatcher().unsubscribe(
                RealtimeEventType.AUTOBID_UPDATED,
                autobidUpdatedListener
            );
            autobidUpdatedListener = null;
        }
    }

    private void handleAuctionListItemUpdated(RealtimeEvent<AuctionListItemPayload> event) {
        AuctionListItemPayload item = event.getPayload();
        if (item == null) {
            return;
        }

        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            if (VIEW_HOME.equals(activeView)) {
                updateHomePreviewCaches(item);
            } else {
                updateActiveViewCache(item);
            }

            if (isHomeShellVisible()) {
                renderActiveView();
            } else if (VIEW_MY_AUCTION.equals(activeView) && activeMyAuctionController != null) {
                List<Auction> myAuctions = auctionsForActiveView().stream()
                    .map(this::toAuction)
                    .toList();
                activeMyAuctionController.setInitialItems(myAuctions);
            }
        });
    }
    
    private boolean matchesActiveViewQuery(AuctionListItemPayload item) {
        if (!matchesCategoryFilter(item)) {
            return false;
        }

        return switch (activeView) {
            case VIEW_LIVE_AUCTION -> "ACTIVE".equals(item.getStatus());
            case VIEW_COMING_SOON -> "SCHEDULED".equals(item.getStatus());
            case VIEW_MY_AUCTION -> currentUserId != null && item.getSellerId() == currentUserId;
            default -> true;
        };
    }

    private boolean matchesCategoryFilter(AuctionListItemPayload item) {
        Long categoryId = categoryIdForActiveFilter();
        return categoryId == null || item.getCategoryId() == categoryId;
    }

    private void updateActiveViewCache(AuctionListItemPayload item) {
        AuctionListItemPayload existing = activeViewAuctions.get(item.getAuctionId());
        if (isStaleAuctionListItem(item, existing)) {
            return;
        }

        if (matchesActiveViewQuery(item)) {
            activeViewAuctions.put(item.getAuctionId(), item);
        } else {
            activeViewAuctions.remove(item.getAuctionId());
        }
    }

    private boolean matchesPreviewQuery(AuctionListItemPayload item, String status) {
        return status.equals(item.getStatus());
    }

    private void updateHomePreviewCaches(AuctionListItemPayload item) {
        if (matchesPreviewQuery(item, "ACTIVE")) {
            updatePreviewCache(livePreviewAuctions, item, true);
        } else {
            removeIfNewerOrSameVersion(livePreviewAuctions, item);
        }

        if (matchesPreviewQuery(item, "SCHEDULED")) {
            updatePreviewCache(comingSoonPreviewAuctions, item, true);
        } else {
            removeIfNewerOrSameVersion(comingSoonPreviewAuctions, item);
        }
    }

    private boolean isStaleAuctionListItem(AuctionListItemPayload incoming, AuctionListItemPayload existing) {
        if (existing == null) {
            return false;
        }
        if (incoming.getAuctionVersion() != existing.getAuctionVersion()) {
            return incoming.getAuctionVersion() < existing.getAuctionVersion();
        }
        if (incoming.getUpdatedAt() == null || existing.getUpdatedAt() == null) {
            return false;
        }
        return !incoming.getUpdatedAt().isAfter(existing.getUpdatedAt());
    }

    private void updatePreviewCache(
            Map<Long, AuctionListItemPayload> cache,
            AuctionListItemPayload item,
            boolean trim) {
        AuctionListItemPayload existing = cache.get(item.getAuctionId());
        if (!isStaleAuctionListItem(item, existing)) {
            cache.put(item.getAuctionId(), item);
            if (trim) {
                trimCache(cache, 3);
            }
        }
    }

    private void replaceAuctionSnapshot(
            Map<Long, AuctionListItemPayload> cache,
            List<AuctionListItemPayload> snapshot) {
        cache.clear();
        mergeAuctionSnapshot(cache, snapshot);
        trimCache(cache, 3);
    }

    private void mergeAuctionSnapshot(
            Map<Long, AuctionListItemPayload> cache,
            List<AuctionListItemPayload> snapshot) {
        for (AuctionListItemPayload item : snapshot) {
            if (item == null) {
                continue;
            }
            AuctionListItemPayload existing = cache.get(item.getAuctionId());
            if (!isStaleAuctionListItem(item, existing)) {
                cache.put(item.getAuctionId(), item);
            }
        }
    }

    private void removeIfNewerOrSameVersion(Map<Long, AuctionListItemPayload> cache, AuctionListItemPayload incoming) {
        AuctionListItemPayload existing = cache.get(incoming.getAuctionId());
        if (existing == null || incoming.getAuctionVersion() >= existing.getAuctionVersion()) {
            cache.remove(incoming.getAuctionId());
        }
    }

    private void trimCache(Map<Long, AuctionListItemPayload> cache, int limit) {
        List<AuctionListItemPayload> sorted = cache.values().stream()
            .sorted(this::compareAuction)
            .limit(limit)
            .toList();

        Set<Long> keptAuctionIds = new HashSet<>();
        sorted.forEach(item -> keptAuctionIds.add(item.getAuctionId()));
        cache.entrySet().removeIf(entry -> !keptAuctionIds.contains(entry.getKey()));
    }

    @FXML
    private void handleModuleSelection(ActionEvent event) {
        String selectedView = moduleButtons.get(event.getSource());
        if (selectedView != null) {
            restoreShellIfInSubView();
            selectView(selectedView);
        }
    }

    @FXML
    private void handleCategoryFilterChange(ActionEvent event) {
        activeCategory = normalizeCategoryFilter(categoryFilterComboBox.getValue());

        if (!VIEW_HOME.equals(activeView)) {
            loadActiveViewAuctions();
            renderActiveView();
        }
    }

    @FXML
    private void handleViewAllBidHistory(ActionEvent event) {
        restoreShellIfInSubView();
        selectView(VIEW_BID_HISTORY);
    }

    @FXML
    private void handleViewAllMyAuction(ActionEvent event) {
        restoreShellIfInSubView();
        selectView(VIEW_MY_AUCTION);
    }

    @FXML
    private void handleViewAllComingSoon(ActionEvent event) {
        restoreShellIfInSubView();
        selectView(VIEW_COMING_SOON);
    }

    @FXML
    private void handleViewAllLiveAuction(ActionEvent event) {
        restoreShellIfInSubView();
        selectView(VIEW_LIVE_AUCTION);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            logoutUser();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Logout failed", exception);
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Logout failed",
                exception.getMessage() != null ? exception.getMessage() : "Could not log out from the current session."
            );
            return;
        }
        try {
            disposed = true;
            disposeActiveChildControllers();
            clearRealtimeCaches();
            unsubscribeRealtimeListener();
            SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Navigation to login failed", exception);
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                exception.getMessage() != null ? exception.getMessage() : "Could not return to the login screen."
            );
        }
    }

    @FXML
    private void handleCreateAuctions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CREATE_AUCTION_VIEW));
            BorderPane createAuctionRoot = loader.load();
            CreateAuctionController controller = loader.getController();
            controller.setOnBack(this::restoreHomeLayout);
            controller.setOnAuctionCreated(this::openMyAuctionAfterCreate);
            disposeActiveChildControllers();
            activeCreateAuctionController = controller;
            attachCreateAuctionStylesheet();

            Node createAuctionCenter = createAuctionRoot.getCenter();
            Node createAuctionBottom = createAuctionRoot.getBottom();
            createAuctionRoot.setCenter(null);
            createAuctionRoot.setBottom(null);

            homeRoot.setLeft(null);
            homeRoot.setRight(null);
            homeRoot.setCenter(createAuctionCenter);
            homeRoot.setBottom(createAuctionBottom);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Navigation to create auction failed", exception);
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the create auction scene."
            );
        }
    }

    private void handleUserBalanceUpdated(RealtimeEvent<UserBalanceUpdatedPayload> event) {
        UserBalanceUpdatedPayload payload = event.getPayload();
        if (payload == null || currentUserId == null || payload.getUserId() != currentUserId) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            UserData.setBalances(payload.getAvailableBalance(), payload.getHoldBalance());
            updateBalanceDisplay(payload.getAvailableBalance());
        });
    }

    private void handleDepositRequestUpdated(RealtimeEvent<DepositRequestPayload> event) {
        DepositRequestPayload payload = event.getPayload();
        if (payload == null || currentUserId == null || payload.getUserId() != currentUserId) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            if ("APPROVED".equals(payload.getStatus())) {
                NotificationManager.show(
                    NotificationManager.NotificationType.SUCCESS, 
                    "Deposit Approved", 
                    "admin has accepted your deposit request"
                );
                if (activeDepositController != null) {
                    activeDepositController.updateStatusLabel("admin has accepted your deposit request");
                }
            } else if ("REJECTED".equals(payload.getStatus())) {
                NotificationManager.show(
                    NotificationManager.NotificationType.ERROR, 
                    "Deposit Rejected", 
                    "admin has rejected your deposit request"
                );
                if (activeDepositController != null) {
                    activeDepositController.updateStatusLabel("admin has rejected your deposit request");
                }
            }
        });
    }

    private void handleUserKicked(RealtimeEvent<Void> event) {
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR, 
                "Account Kicked", 
                "You have been permanently kicked and logged out by the administrator!"
            );
            try {
                disposed = true;
                disposeActiveChildControllers();
                clearRealtimeCaches();
                unsubscribeRealtimeListener();
                UserData.setKicked(true);
                UserData.clear();
                SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Redirect to login after ban failed", e);
            }
        });
    }

    private void handleUserWarned(RealtimeEvent<UserWarnedPayload> event) {
        UserWarnedPayload payload = event.getPayload();
        if (payload == null) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            NotificationManager.show(
                NotificationManager.NotificationType.WARNING, 
                "Account Warning", 
                "You have been warned by the administrator! Reason: " + payload.getReason() + " (Warnings: " + payload.getWarningCount() + "/3)"
            );
        });
    }

    private void handleMyBidListItemUpdated(RealtimeEvent<MyBidListItemPayload> event) {
        MyBidListItemPayload item = event.getPayload();
        if (item == null) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            if (!mergeMyBidItem(item)) {
                return;
            }
            if (activeMyBidController != null) {
                activeMyBidController.setInitialItems(myBidItemsByAuctionId.values());
            }
            if (VIEW_HOME.equals(activeView) && isHomeShellVisible()) {
                renderMyBidPreview();
            }
        });
    }

    private void handleAutobidUpdated(RealtimeEvent<AutobidUpdatedPayload> event) {
        AutobidUpdatedPayload payload = event.getPayload();
        if (payload == null || currentUserId == null || payload.getUserId() != currentUserId) {
            return;
        }
        Platform.runLater(() -> {
            if (disposed) {
                return;
            }
            boolean changedAuctionSummary = mergeAutobidSummary(payload);
            boolean changedMyBid = mergeAutobidIntoMyBidItem(payload);
            if (!changedMyBid && payload.isWinning()) {
                refreshMyBidItemsAsync();
            }
            if (changedMyBid && activeMyBidController != null) {
                activeMyBidController.setInitialItems(myBidItemsByAuctionId.values());
            }
            if (isHomeShellVisible() && (changedAuctionSummary || changedMyBid)) {
                renderActiveView();
            }
        });
    }

    private boolean mergeMyBidItem(MyBidListItemPayload item) {
        if (item == null) {
            return false;
        }
        MyBidListItemPayload existing = myBidItemsByAuctionId.get(item.getAuctionId());
        if (isStaleMyBidItem(item, existing)) {
            return false;
        }
        myBidItemsByAuctionId.put(item.getAuctionId(), item);
        return true;
    }

    private boolean mergeAutobidSummary(AutobidUpdatedPayload payload) {
        ViewerAuctionBidSummaryPayload summary = new ViewerAuctionBidSummaryPayload(
            payload.getAuctionId(),
            payload.getUserId(),
            payload.getAutobidStatus() != null,
            payload.getAutobidStatus(),
            payload.isWinning(),
            payload.isShowActiveMaxBid(),
            payload.getUpdatedAt()
        );
        boolean changed = false;
        changed |= mergeAutobidSummaryIntoCache(livePreviewAuctions, summary);
        changed |= mergeAutobidSummaryIntoCache(comingSoonPreviewAuctions, summary);
        changed |= mergeAutobidSummaryIntoCache(activeViewAuctions, summary);
        return changed;
    }

    private boolean mergeAutobidSummaryIntoCache(
            Map<Long, AuctionListItemPayload> cache,
            ViewerAuctionBidSummaryPayload summary) {
        AuctionListItemPayload item = cache.get(summary.getAuctionId());
        if (item == null || isStaleViewerBidSummary(summary, item.getViewerBidState())) {
            return false;
        }
        item.setViewerBidState(summary);
        return true;
    }

    private boolean isStaleViewerBidSummary(
            ViewerAuctionBidSummaryPayload incoming,
            ViewerAuctionBidSummaryPayload existing) {
        if (existing == null || incoming.getUpdatedAt() == null || existing.getUpdatedAt() == null) {
            return false;
        }
        return !incoming.getUpdatedAt().isAfter(existing.getUpdatedAt());
    }

    private boolean mergeAutobidIntoMyBidItem(AutobidUpdatedPayload payload) {
        MyBidListItemPayload item = myBidItemsByAuctionId.get(payload.getAuctionId());
        if (item == null || isStaleAutobidForMyBid(payload, item)) {
            return false;
        }
        item.setBidSource(BidSource.AUTO_BID);
        item.setMyMaxBidAmount(payload.isShowActiveMaxBid() ? payload.getMaxBidAmount() : null);
        BidStatus bidStatus = bidStatusFromAutobidStatus(payload.getAutobidStatus());
        if (bidStatus != null) {
            item.setBidStatus(bidStatus);
        }
        item.setUpdatedAt(payload.getUpdatedAt());
        return true;
    }

    private boolean isStaleAutobidForMyBid(AutobidUpdatedPayload payload, MyBidListItemPayload item) {
        return payload.getUpdatedAt() != null
            && item.getUpdatedAt() != null
            && !payload.getUpdatedAt().isAfter(item.getUpdatedAt());
    }

    private BidStatus bidStatusFromAutobidStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return BidStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            LOGGER.log(Level.FINE, "Unknown AutoBid status for my-bid row: {0}", status);
            return null;
        }
    }

    private boolean isStaleMyBidItem(MyBidListItemPayload incoming, MyBidListItemPayload existing) {
        if (existing == null) {
            return false;
        }
        if (incoming.getAuctionVersion() != existing.getAuctionVersion()) {
            return incoming.getAuctionVersion() < existing.getAuctionVersion();
        }
        if (incoming.getUpdatedAt() == null || existing.getUpdatedAt() == null) {
            return false;
        }
        return incoming.getUpdatedAt().isBefore(existing.getUpdatedAt());
    }

    private void updateBalanceDisplay(BigDecimal availableBalance) {
        BigDecimal balance = availableBalance == null ? BigDecimal.ZERO : availableBalance;
        balanceLabel.setText("BALANCE: " + CURRENCY_FORMAT.format(balance));
    }

    private void updateAccountDisplay() {
        String username = UserData.getUsername();
        accountNameLabel.setText(username == null || username.isBlank() ? "Account" : username);
    }

    @FXML
    private void handleDepositBalance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DEPOSIT_BALANCE_VIEW));
            Node depositCenter = loader.load();
            disposeActiveChildControllers();
            DepositBalanceController controller = loader.getController();
            controller.setOnBack(this::restoreHomeLayout);
            activeDepositController = controller;

            homeRoot.setLeft(homeLeft);
            homeRoot.setRight(null);
            homeRoot.setCenter(depositCenter);
            homeRoot.setBottom(null);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Navigation to deposit balance failed", exception);
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the deposit balance screen."
            );
        }
    }

    public void handleCardClick(long auctionId) {
        openAuctionById(auctionId);
    }

    private void openAuctionById(long auctionId) {
        try {
            Auction detailAuction = fetchAuctionDetail(auctionId);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(BID_VIEW));
            BorderPane bidRoot = loader.load();
            disposeActiveChildControllers();
            BidController controller = loader.getController();
            controller.setOnBack(this::restoreHomeLayout);
            controller.setSceneData(detailAuction);
            activeBidController = controller;
            attachBidStylesheet();

            Node bidCenter = bidRoot.getCenter();
            Node bidBottom = bidRoot.getBottom();
            ///gỡ node ra khỏi parent cũ
            bidRoot.setCenter(null);
            bidRoot.setBottom(null);

            homeRoot.setLeft(null);
            homeRoot.setRight(null);
            homeRoot.setCenter(bidCenter);
            homeRoot.setBottom(bidBottom);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Navigation to bid detail failed", exception);
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the placebid detail scene for the selected product."
            );
        }
    }

    private Auction fetchAuctionDetail(long auctionId) throws IOException {
        Respond<?> response = SocketClient.getClient().sendMessage(
            new Request<>(RequestType.GET_AUCTION_DETAIL, new AuctionDetailRequest(auctionId))
        );
        if (response == null || !response.isStatus()) {
            throw new IOException(response != null ? response.getMessage() : "No response");
        }
        AuctionItemPayload payload = JsonUtils.fromJson(JsonUtils.toJson(response.getData()), AuctionItemPayload.class);
        if (payload == null) {
            throw new IOException("Auction detail response is empty.");
        }
        return toAuction(payload);
    }

    private void handleMyBidAuctionSelected(long auctionId) {
        if (activeMyBidController != null) {
            activeMyBidController.dispose();
            activeMyBidController = null;
        }
        openAuctionById(auctionId);
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
        LOGGER.info("Logout successfully.");
        UserData.clear();
    }

    private void initializeNavigationMaps() {
        moduleButtons.put(homeModuleButton, VIEW_HOME);
        moduleButtons.put(comingSoonModuleButton, VIEW_COMING_SOON);
        moduleButtons.put(liveAuctionModuleButton, VIEW_LIVE_AUCTION);
        moduleButtons.put(bidHistoryModuleButton, VIEW_BID_HISTORY);
        moduleButtons.put(myAuctionModuleButton, VIEW_MY_AUCTION);
    }

    private void selectView(String view) {
        activeView = view;
        if (VIEW_BID_HISTORY.equals(view)) {
            openMyBidHistoryView();
            refreshMyBidItemsAsync();
            return;
        }
        if (VIEW_HOME.equals(view)) {
            loadHomePreviewAuctions();
        } else {
            loadActiveViewAuctions();
        }

        renderActiveView();
    }

    private void renderActiveView() {
        if (VIEW_BID_HISTORY.equals(activeView)) {
            sidebarSubtitle.setText(subtitleForSidebar());
            setActiveNavigationState();
            if (activeMyBidController == null) {
                openMyBidHistoryView();
            }
            return;
        }
        if (VIEW_MY_AUCTION.equals(activeView)) {
            sidebarSubtitle.setText(subtitleForSidebar());
            setActiveNavigationState();
            if (activeMyAuctionController == null) {
                openMyAuctionView();
            } else {
                List<Auction> myAuctions = auctionsForActiveView().stream()
                    .map(this::toAuction)
                    .toList();
                activeMyAuctionController.setInitialItems(myAuctions);
            }
            return;
        }
        List<AuctionListItemPayload> auctions = auctionsForActiveView();
        sidebarSubtitle.setText(subtitleForSidebar());
        homeRoot.setRight(VIEW_HOME.equals(activeView) ? homeRight : null);
        setActiveNavigationState();
        if (VIEW_HOME.equals(activeView)) {
            renderHomeDashboard();
        } else {
            catalogTitle.setText(activeView);
            catalogSubtitle.setText(subtitleForCatalog(auctions.size()));
            setNodeVisibility(homeDashboard, false);
            setNodeVisibility(catalogContent, true);
            renderAuctionItems(auctions);
        }
    }

    private String subtitleForSidebar() {
        if (!VIEW_HOME.equals(activeView) && !CATEGORY_ALL.equals(activeCategory)) {
            return activeView.toLowerCase() + " / " + activeCategory;
        }
        return activeView.toLowerCase();
    }

    private String subtitleForCatalog(int auctionCount) {
        String categoryLabel = CATEGORY_ALL.equals(activeCategory) ? "all categories" : activeCategory;
        return switch (activeView) {
            case VIEW_COMING_SOON -> auctionCount + " upcoming auctions in " + categoryLabel + ".";
            case VIEW_LIVE_AUCTION -> auctionCount + " active auctions in " + categoryLabel + ".";
            case VIEW_BID_HISTORY -> "Your personal bids filtered by " + categoryLabel + ".";
            case VIEW_MY_AUCTION -> "Auctions you created, filtered by " + categoryLabel + ".";
            default -> "Auction list in " + categoryLabel + ".";
        };
    }

    private void setActiveNavigationState() {
        for (Map.Entry<Button, String> entry : moduleButtons.entrySet()) {
            setStyleClassActive(entry.getKey(), "active-module", entry.getValue().equals(activeView));
        }
    }

    private void renderAuctionItems(List<AuctionListItemPayload> auctions) {
        productFlow.getChildren().clear();

        boolean isEmpty = auctions.isEmpty();
        emptyStateLabel.setText(emptyMessage());
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);

        for (AuctionListItemPayload auction : auctions) {
            productFlow.getChildren().add(createAuctionCard(toAuction(auction)));
        }
    }

    private void renderHomeDashboard() {
        setNodeVisibility(homeDashboard, true);
        setNodeVisibility(catalogContent, false);
        renderMyBidPreview();
        renderPreviewFlow(
            comingSoonPreviewFlow,
            comingSoonPreviewAuctions.values().stream()
                .sorted(this::compareAuction)
                .map(this::toAuction)
                .toList()
        );

        renderPreviewFlow(
            liveAuctionPreviewFlow,
            livePreviewAuctions.values().stream()
                .sorted(this::compareAuction)
                .map(this::toAuction)
                .toList()
        );
    }

    private void renderMyBidPreview() {
        if (myBidPreviewBox == null) {
            return;
        }

        myBidPreviewBox.getChildren().clear();
        List<MyBidListItemPayload> items = myBidItemsByAuctionId.values().stream()
            .sorted(Comparator.comparing(this::myBidSortTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(2)
            .toList();

        if (items.isEmpty()) {
            Label empty = new Label("No recent bids.");
            empty.getStyleClass().add("bid-item-meta");
            myBidPreviewBox.getChildren().add(empty);
            return;
        }

        for (MyBidListItemPayload item : items) {
            myBidPreviewBox.getChildren().add(createMyBidPreviewItem(item));
        }
    }

    private LocalDateTime myBidSortTime(MyBidListItemPayload item) {
        return item.getUpdatedAt() == null ? item.getBidTime() : item.getUpdatedAt();
    }

    private Node createMyBidPreviewItem(MyBidListItemPayload item) {
        HBox row = new HBox(10);
        row.getStyleClass().add("bid-line-item");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setOnMouseClicked(event -> handleMyBidAuctionSelected(item.getAuctionId()));

        Region accentBar = new Region();
        accentBar.getStyleClass().add("bid-line-accent");

        VBox content = new VBox(5);
        content.setMinWidth(0);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);

        Label title = new Label(item.getAuctionTitle() == null || item.getAuctionTitle().isBlank()
            ? "Auction #" + item.getAuctionId()
            : item.getAuctionTitle());
        title.setWrapText(false);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.setMinWidth(0);
        title.setPrefWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        title.maxWidthProperty().bind(content.widthProperty().subtract(4));
        title.getStyleClass().add("bid-item-title");

        HBox metaRow = new HBox();
        metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label("CURRENT: " + CURRENCY_FORMAT.format(item.getCurrentPrice() == null ? BigDecimal.ZERO : item.getCurrentPrice()));
        price.getStyleClass().add("bid-item-meta");
        Label status = new Label(item.getBidStatus() == null ? "-" : item.getBidStatus().name());
        status.getStyleClass().add(previewBidStatusStyle(status.getText()));

        metaRow.getChildren().add(price);
        metaRow.getChildren().add(new Region());
        HBox.setHgrow(metaRow.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        metaRow.getChildren().add(status);

        Label bidTime = new Label("BID PLACED: " + formatBidTime(item.getBidTime()));
        bidTime.getStyleClass().add("bid-item-time");

        content.getChildren().addAll(title, metaRow, bidTime);
        row.getChildren().addAll(accentBar, content);
        return row;
    }

    private String formatBidTime(LocalDateTime bidTime) {
        if (bidTime == null) {
            return "-";
        }
        Duration elapsed = Duration.between(bidTime, LocalDateTime.now(UTC_ZONE));
        if (elapsed.isNegative()) {
            elapsed = Duration.ZERO;
        }
        long seconds = elapsed.getSeconds();
        if (seconds < 60) {
            return "Just now";
        }
        if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return String.format("%02dm %02ds", minutes, remainingSeconds);
        }
        if (seconds < 86400) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return String.format("%02dh %02dm", hours, minutes);
        }
        LocalDateTime displayTime = bidTime
            .atZone(UTC_ZONE)
            .withZoneSameInstant(VIETNAM_ZONE)
            .toLocalDateTime();
        return displayTime.format(OTHER_YEAR_BID_TIME_FORMATTER);
    }

    private String previewBidStatusStyle(String status) {
        return switch (status) {
            case "WINNING", "WON" -> "bid-status-accent";
            case "OUTBID" -> "bid-status-danger";
            default -> "bid-status-muted";
        };
    }

    private void renderPreviewFlow(FlowPane flowPane, List<Auction> auctions) {
        flowPane.getChildren().clear();
        for (Auction auction : auctions) {
            flowPane.getChildren().add(createAuctionCard(auction));
        }
    }

    private Auction toAuction(AuctionListItemPayload payload) {
        String imagePath = payload.getThumbnailUrl() == null || payload.getThumbnailUrl().isBlank()
            ? "/jfx/image/products/collectibles.png"
            : payload.getThumbnailUrl();
        String description = payload.getDescription() == null || payload.getDescription().isBlank()
            ? "Auction #" + payload.getAuctionId()
            : payload.getDescription();

        Product product = new Product(
            payload.getProductId(),
            payload.getProductName() == null || payload.getProductName().isBlank() ? payload.getTitle() : payload.getProductName(),
            description,
            payload.getCategoryId(),
            imagePath,
            List.of(imagePath)
        );
        ViewerAuctionBidSummaryPayload viewerBidState = payload.getViewerBidState();

        return new Auction(
            payload.getAuctionId(),
            payload.getAuctionVersion(),
            payload.getSellerId(),
            payload.getTitle(),
            description,
            payload.getStatus(),
            payload.getStartingPrice(),
            payload.getCurrentPrice(),
            payload.getMinimumBidStep(),
            payload.getBuyNowPrice(),
            payload.getWinnerUserId(),
            payload.getReserveMet(),
            payload.isAntiSnipeExtended(),
            payload.getStartingTime(),
            payload.getEndingTime(),
            product,
            null,
            null,
            viewerBidState == null ? null : viewerBidState.getAutobidStatus(),
            viewerBidState != null && viewerBidState.isWinning(),
            viewerBidState != null && viewerBidState.isShowActiveMaxBid(),
            viewerBidState == null ? null : viewerBidState.getUpdatedAt()
        );
    }

    private Auction toAuction(AuctionItemPayload payload) {
        String imagePath = payload.getThumbnailUrl() == null || payload.getThumbnailUrl().isBlank()
            ? "/jfx/image/products/collectibles.png"
            : payload.getThumbnailUrl();
        List<String> imageUrls = payload.getImageUrls() == null || payload.getImageUrls().isEmpty()
            ? List.of(imagePath)
            : payload.getImageUrls();
        String description = payload.getDescription() == null || payload.getDescription().isBlank()
            ? "Auction #" + payload.getAuctionId()
            : payload.getDescription();

        Product product = new Product(
            payload.getProductId(),
            payload.getProductName() == null || payload.getProductName().isBlank() ? payload.getTitle() : payload.getProductName(),
            description,
            payload.getCategoryId(),
            imagePath,
            imageUrls
        );
        var viewerBidState = payload.getViewerBidState();

        Auction auction = new Auction(
            payload.getAuctionId(),
            payload.getAuctionVersion(),
            payload.getSellerId(),
            payload.getTitle(),
            description,
            payload.getStatus(),
            payload.getStartingPrice(),
            payload.getCurrentPrice(),
            payload.getMinimumBidStep(),
            payload.getBuyNowPrice(),
            payload.getWinnerUserId(),
            payload.getReserveMet(),
            payload.isAntiSnipeExtended(),
            payload.getStartingTime(),
            payload.getEndingTime(),
            product,
            viewerBidState == null ? null : viewerBidState.getAutobidId(),
            viewerBidState == null ? null : viewerBidState.getMaxBidAmount(),
            viewerBidState == null ? null : viewerBidState.getAutobidStatus(),
            viewerBidState != null && viewerBidState.isWinning(),
            viewerBidState != null && viewerBidState.isShowActiveMaxBid(),
            viewerBidState == null ? null : viewerBidState.getUpdatedAt()
        );
        auction.setSellerUsername(payload.getSellerUsername());
        auction.setSellerEmail(payload.getSellerEmail());
        return auction;
    }

    private int compareAuction(AuctionListItemPayload left, AuctionListItemPayload right) {
        boolean upcoming = "SCHEDULED".equals(left.getStatus()) && "SCHEDULED".equals(right.getStatus());
        int byStartingTime = upcoming
            ? left.getStartingTime().compareTo(right.getStartingTime())
            : right.getStartingTime().compareTo(left.getStartingTime());
        if (byStartingTime != 0) {
            return byStartingTime;
        }
        return upcoming
            ? Long.compare(left.getAuctionId(), right.getAuctionId())
            : Long.compare(right.getAuctionId(), left.getAuctionId());
    }

    private Long categoryIdForActiveFilter() {
        return switch (activeCategory) {
            case CATEGORY_ELECTRONICS -> 1L;
            case CATEGORY_COLLECTIBLES -> 2L;
            case CATEGORY_ARTS -> 3L;
            case CATEGORY_JEWELRY_WATCHES -> 4L;
            default -> null;
        };
    }

    private String emptyMessage() {
        if (VIEW_LIVE_AUCTION.equals(activeView)) {
            return "No active auctions found for this category.";
        }
        if (VIEW_COMING_SOON.equals(activeView)) {
            return "No upcoming auctions found for this category.";
        }
        if (VIEW_BID_HISTORY.equals(activeView)) {
            return "No personal bids found for this category.";
        }
        if (VIEW_MY_AUCTION.equals(activeView)) {
            return "No created auctions found for this category.";
        }
        return "No auctions available for this category.";
    }

    private void openMyAuctionAfterCreate() {
        restoreHomeLayout();
        selectView(VIEW_MY_AUCTION);
    }

    private String normalizeCategoryFilter(String selectedFilter) {
        if (selectedFilter == null ||
            selectedFilter.isBlank() ||
            CATEGORY_ALL_LABEL.equals(selectedFilter)) {
            return CATEGORY_ALL;
        }
        return selectedFilter;
    }

    private void restoreHomeLayout() {
        disposeActiveChildControllers();
        homeRoot.setLeft(homeLeft);
        homeRoot.setRight(homeRight);
        homeRoot.setCenter(homeCenter);
        homeRoot.setBottom(homeBottom);
        if (VIEW_HOME.equals(activeView)) {
            loadHomePreviewAuctions();
        } else {
            loadActiveViewAuctions();
        }
        renderActiveView();
    }

    private void restoreShellIfInSubView() {
        disposeActiveChildControllers();
        if (!isHomeShellVisible()) {
            homeRoot.setLeft(homeLeft);
            homeRoot.setRight(homeRight);
            homeRoot.setCenter(homeCenter);
            homeRoot.setBottom(homeBottom);
        }
    }

    private boolean isHomeShellVisible() {
        return homeRoot.getCenter() == homeCenter;
    }

    private void attachCreateAuctionStylesheet() {
        String stylesheet = getClass().getResource(CREATE_AUCTION_CSS).toExternalForm();
        if (!homeRoot.getScene().getStylesheets().contains(stylesheet)) {
            homeRoot.getScene().getStylesheets().add(stylesheet);
        }
    }

    private void disposeActiveChildControllers() {
        if (activeCreateAuctionController != null) {
            activeCreateAuctionController.dispose();
            activeCreateAuctionController = null;
        }
        if (activeDepositController != null) {
            activeDepositController.dispose();
            activeDepositController = null;
        }
        if (activeMyBidController != null) {
            activeMyBidController.dispose();
            activeMyBidController = null;
        }
        if (activeMyAuctionController != null) {
            activeMyAuctionController.dispose();
            activeMyAuctionController = null;
        }
        if (activeBidController != null) {
            activeBidController.dispose();
            activeBidController = null;
        }
    }

    private void clearRealtimeCaches() {
        livePreviewAuctions.clear();
        comingSoonPreviewAuctions.clear();
        activeViewAuctions.clear();
        myBidItemsByAuctionId.clear();
    }

    private void attachBidStylesheet() {
        String stylesheet = getClass().getResource(BID_CSS).toExternalForm();
        if (!homeRoot.getScene().getStylesheets().contains(stylesheet)) {
            homeRoot.getScene().getStylesheets().add(stylesheet);
        }
    }

    private void attachMyBidStylesheet() {
        String stylesheet = getClass().getResource(MY_BID_CSS).toExternalForm();
        if (!homeRoot.getScene().getStylesheets().contains(stylesheet)) {
            homeRoot.getScene().getStylesheets().add(stylesheet);
        }
    }

    private void attachMyAuctionStylesheet() {
        String stylesheet = getClass().getResource(MY_AUCTION_CSS).toExternalForm();
        if (!homeRoot.getScene().getStylesheets().contains(stylesheet)) {
            homeRoot.getScene().getStylesheets().add(stylesheet);
        }
    }

    private void openMyAuctionView() {
        try {
            if (activeMyAuctionController != null) {
                activeMyAuctionController.dispose();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MY_AUCTION_VIEW));
            Node myAuctionCenter = loader.load();
            MyAuctionController controller = loader.getController();
            controller.setOnAuctionSelected(auction -> openAuctionById(auction.getId()));

            List<Auction> myAuctions = auctionsForActiveView().stream()
                .map(this::toAuction)
                .toList();
            controller.setInitialItems(myAuctions);
            activeMyAuctionController = controller;
            attachMyAuctionStylesheet();

            homeRoot.setLeft(homeLeft);
            homeRoot.setRight(null);
            homeRoot.setCenter(myAuctionCenter);
            homeRoot.setBottom(null);
            sidebarSubtitle.setText(subtitleForSidebar());
            setActiveNavigationState();
        } catch (Exception exception) {
            exception.printStackTrace();
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the my auction screen."
            );
        }
    }

    private void openMyBidHistoryView() {
        try {
            if (activeMyBidController != null) {
                activeMyBidController.dispose();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MY_BID_VIEW));
            Node myBidCenter = loader.load();
            MyBidController controller = loader.getController();
            controller.setOnAuctionSelected(this::handleMyBidAuctionSelected);
            controller.setInitialItems(myBidItemsByAuctionId.values());
            activeMyBidController = controller;
            attachMyBidStylesheet();

            homeRoot.setLeft(homeLeft);
            homeRoot.setRight(null);
            homeRoot.setCenter(myBidCenter);
            homeRoot.setBottom(null);
            sidebarSubtitle.setText(subtitleForSidebar());
            setActiveNavigationState();
        } catch (Exception exception) {
            exception.printStackTrace();
            NotificationManager.show(
            NotificationManager.NotificationType.ERROR,
            "Navigation failed",
            "Could not open the personal bid screen."
        );
    }
    }

    private void refreshMyBidItemsAsync() {
        Task<List<MyBidListItemPayload>> task = new Task<>() {
            @Override
            protected List<MyBidListItemPayload> call() throws Exception {
                return fetchMyBidItems();
            }
        };
        task.setOnSucceeded(event -> {
            if (disposed) {
                return;
            }
            replaceMyBidItems(task.getValue());
            if (activeMyBidController != null) {
                activeMyBidController.setInitialItems(myBidItemsByAuctionId.values());
            }
            if (VIEW_HOME.equals(activeView) && isHomeShellVisible()) {
                renderMyBidPreview();
            }
        });
        task.setOnFailed(event -> LOGGER.log(Level.WARNING, "Refresh my-bid items failed", task.getException()));

        Thread thread = new Thread(task, "vbay-my-bid-refresh");
        thread.setDaemon(true);
        thread.start();
    }

    private Node createAuctionCard(Auction listAuction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jfx/scene/AuctionCard.fxml"));
            Node card = loader.load();
            AuctionCardController controller = loader.getController();
            controller.setAuction(listAuction);
            controller.setOnSelected(this::handleCardClick);
            return card;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load auction card view.", exception);
        }
    }

    private void setNodeVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void setStyleClassActive(Node node, String styleClass, boolean active) {
        node.getStyleClass().remove(styleClass);
        if (active && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    private void showMessage(NotificationManager.NotificationType type, String title, String content) {
        NotificationManager.show(type, title, content);
    }
}
