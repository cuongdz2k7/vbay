package com.vbay.ui.scene_ui.controller.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vbay.network.ClientAuthSession;
import com.vbay.network.SocketClient;
import com.vbay.network.dispatcher.RealtimeEventDispatcher;
import com.vbay.network.dispatcher.RealtimeEventListener;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListResponse;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.MockProductCatalog;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.scene_ui.controller.auction.CreateAuctionController;
import com.vbay.ui.scene_ui.controller.card.AuctionCardController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {
    private static final String CREATE_AUCTION_VIEW = "/jfx/scene/CreateAuction.fxml";
    private static final String CREATE_AUCTION_CSS = "/jfx/css/CreateAuction.css";

    private static final String VIEW_HOME = "Home";
    private static final String VIEW_COMING_SOON = "Coming Soon";
    private static final String VIEW_LIVE_AUCTION = "Live Auction";
    private static final String VIEW_BID_HISTORY = "Bid History";
    private static final String VIEW_MY_AUCTION = "My Auction";

    private static final String CATEGORY_ALL = "All";
    private static final String CATEGORY_ALL_LABEL = "All Categories";
    private static final String CATEGORY_ELECTRONICS = "Electronics";
    private static final String CATEGORY_COLLECTIBLES = "Collectibles";
    private static final String CATEGORY_ARTS = "Arts";
    private static final String CATEGORY_JEWELRY_WATCHES = "Jewelry & Watches";


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
    private Label emptyStateLabel;
    @FXML
    private HBox paginationBar;

    private final Map<Button, String> moduleButtons = new LinkedHashMap<>();

    private Map<String, List<Product>> productsByCategory;
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
    private RealtimeEventListener<AuctionListItemPayload> auctionListItemListener;
    private Long currentUserId;


    @FXML
    private void initialize() {
        homeCenter = homeRoot.getCenter();
        homeLeft = homeRoot.getLeft();
        homeRight = homeRoot.getRight();
        homeBottom = homeRoot.getBottom();
        currentUserId = ClientAuthSession.getUserId();

        productsByCategory = initializeCategoryData();
        initializeNavigationMaps();
        subscribeRealtimeListener();
        subscribeServerRooms();

        paginationBar.setVisible(false);
        paginationBar.setManaged(false);
        selectView(VIEW_HOME);
    }

    private void loadHomePreviewAuctions() {
        livePreviewAuctions.clear();
        comingSoonPreviewAuctions.clear();

        fetchAuctionList("ACTIVE", null, null, 3)
            .forEach(item -> livePreviewAuctions.put(item.getAuctionId(), item));

        fetchAuctionList("SCHEDULED", null, null, 3)
            .forEach(item -> comingSoonPreviewAuctions.put(item.getAuctionId(), item));
    }

    private List<AuctionListItemPayload> auctionsForActiveView() {
        return activeViewAuctions.values().stream()
            .sorted(this::compareAuction)
            .toList();
    }


    private void loadActiveViewAuctions() {
        activeViewAuctions.clear();

        if (VIEW_MY_AUCTION.equals(activeView) && currentUserId == null) {
            return;
        }

        String status = switch (activeView) {
            case VIEW_LIVE_AUCTION -> "ACTIVE";
            case VIEW_COMING_SOON -> "SCHEDULED";
            default -> null;
        };

        Long categoryId = categoryIdForActiveFilter();
        Long sellerId = VIEW_MY_AUCTION.equals(activeView) ? currentUserId : null;

        fetchAuctionList(status, categoryId, sellerId, null)
            .forEach(item -> activeViewAuctions.put(item.getAuctionId(), item));
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
            exception.printStackTrace();
            showMessage(Alert.AlertType.ERROR, "Load auctions failed", exception.getMessage());
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
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    private void subscribeRealtimeListener() {
        auctionListItemListener = event -> handleAuctionListItemUpdated(event);
        RealtimeEventDispatcher dispatcher = SocketClient.getClient().getRealtimeEventDispatcher();
        dispatcher.subscribe(
                RealtimeEventType.AUCTION_LIST_ITEM_UPDATED,
                auctionListItemListener
            );
    }

    private void handleAuctionListItemUpdated(RealtimeEvent<AuctionListItemPayload> event) {
        AuctionListItemPayload item = event.getPayload();

        Platform.runLater(() -> {
            if (VIEW_HOME.equals(activeView)) {
                updateHomePreviewCaches(item);
            } else {
                updateActiveViewCache(item);
            }

            renderActiveView();
        });
    }

    private void updateActiveViewCache(AuctionListItemPayload item) {
        if (matchesActiveViewQuery(item)) {
            activeViewAuctions.put(item.getAuctionId(), item);
        } else {
            activeViewAuctions.remove(item.getAuctionId());
        }
    }

    private void updateHomePreviewCaches(AuctionListItemPayload item) {
        if (matchesPreviewQuery(item, "ACTIVE")) {
            livePreviewAuctions.put(item.getAuctionId(), item);
            trimCache(livePreviewAuctions, 3);
        } else {
            livePreviewAuctions.remove(item.getAuctionId());
        }

        if (matchesPreviewQuery(item, "SCHEDULED")) {
            comingSoonPreviewAuctions.put(item.getAuctionId(), item);
            trimCache(comingSoonPreviewAuctions, 3);
        } else {
            comingSoonPreviewAuctions.remove(item.getAuctionId());
        }
    }

    private boolean matchesPreviewQuery(AuctionListItemPayload item, String status) {
        return status.equals(item.getStatus()) && matchesCategoryFilter(item);
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

    private void trimCache(Map<Long, AuctionListItemPayload> cache, int limit) {
        List<AuctionListItemPayload> sorted = cache.values().stream()
            .sorted(this::compareAuction)
            .limit(limit)
            .toList();

        cache.clear();
        for (AuctionListItemPayload item : sorted) {
            cache.put(item.getAuctionId(), item);
        }
    }

    @FXML
    private void handleModuleSelection(ActionEvent event) {
        String selectedView = moduleButtons.get(event.getSource());
        if (selectedView != null) {
            selectView(selectedView);
        }
    }

    @FXML
    private void handleCategoryFilterChange(ActionEvent event) {
        activeCategory = normalizeCategoryFilter(categoryFilterComboBox.getValue());

        if (VIEW_HOME.equals(activeView)) {
            loadHomePreviewAuctions();
        } else {
            loadActiveViewAuctions();
        }

        renderActiveView();
    }

    @FXML
    private void handleViewAllBidHistory(ActionEvent event) {
        activeCategory = CATEGORY_ALL;
        categoryFilterComboBox.setValue(CATEGORY_ALL_LABEL);
        selectView(VIEW_BID_HISTORY);
    }

    @FXML
    private void handleViewAllMyAuction(ActionEvent event) {
        activeCategory = CATEGORY_ALL;
        categoryFilterComboBox.setValue(CATEGORY_ALL_LABEL);
        selectView(VIEW_MY_AUCTION);
    }

    @FXML
    private void handleViewAllComingSoon(ActionEvent event) {
        selectView(VIEW_COMING_SOON);
    }

    @FXML
    private void handleViewAllLiveAuction(ActionEvent event) {
        selectView(VIEW_LIVE_AUCTION);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            logoutUser();
        } catch (IOException exception) {
            exception.printStackTrace();
            showMessage(
                Alert.AlertType.ERROR,
                "Logout failed(Server Side)",
                exception.getMessage() != null ? exception.getMessage() : "Could not log out from the current session."
            );
            return;
        }
        try {
            SceneManager.switchScene("/jfx/scene/auth/Login.fxml");
        } catch (Exception exception) {
            exception.printStackTrace();
            showMessage(
                Alert.AlertType.ERROR,
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
            exception.printStackTrace();
            showMessage(
                Alert.AlertType.ERROR,
                "Navigation failed",
                "Could not open the create auction scene."
            );
        }
    }

    public void handleCardClick(Product product) {
        try {
            SceneManager.switchScene("/jfx/scene/bid/Bid.fxml", product);
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Navigation failed",
                "Could not open the placebid detail scene for the selected product."
            );
        }
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
        ClientAuthSession.clear();
    }

    private void initializeNavigationMaps() {
        moduleButtons.put(homeModuleButton, VIEW_HOME);
        moduleButtons.put(comingSoonModuleButton, VIEW_COMING_SOON);
        moduleButtons.put(liveAuctionModuleButton, VIEW_LIVE_AUCTION);
        moduleButtons.put(bidHistoryModuleButton, VIEW_BID_HISTORY);
        moduleButtons.put(myAuctionModuleButton, VIEW_MY_AUCTION);
    }

    private Map<String, List<Product>> initializeCategoryData() {
        Map<String, List<Product>> source = MockProductCatalog.categoryData();
        Map<String, List<Product>> data = new LinkedHashMap<>();
        data.put(
            CATEGORY_ELECTRONICS,
            combine(
                source.getOrDefault(MockProductCatalog.ELECTRONICS_MACBOOKS, List.of()),
                source.getOrDefault(MockProductCatalog.ELECTRONICS_PHONES, List.of()),
                source.getOrDefault(MockProductCatalog.ELECTRONICS_GAMING, List.of()),
                source.getOrDefault(MockProductCatalog.ELECTRONICS_AUDIO, List.of())
            )
        );
        data.put(
            CATEGORY_COLLECTIBLES,
            combine(
                source.getOrDefault(MockProductCatalog.COLLECTIBLES_CARDS, List.of()),
                source.getOrDefault(MockProductCatalog.COLLECTIBLES_FIGURES, List.of())
            )
        );
        data.put(
            CATEGORY_ARTS,
            combine(
                source.getOrDefault(MockProductCatalog.ART_STAMPS, List.of()),
                source.getOrDefault(MockProductCatalog.ART_PAINTBRUSHES, List.of())
            )
        );
        data.put(
            CATEGORY_JEWELRY_WATCHES,
            combine(
                source.getOrDefault(MockProductCatalog.JEWELRY_CLOCKS, List.of()),
                source.getOrDefault(MockProductCatalog.JEWELRY_EARRINGS, List.of())
            )
        );
        return Map.copyOf(data);
    }

    @SafeVarargs
    private final List<Product> combine(List<Product>... productLists) {
        List<Product> combined = new ArrayList<>();
        for (List<Product> products : productLists) {
            combined.addAll(products);
        }
        return List.copyOf(combined);
    }

    private void selectView(String view) {
        activeView = view;
        if (VIEW_HOME.equals(view)) {
            loadHomePreviewAuctions();
        } else {
            loadActiveViewAuctions();
        }

        renderActiveView();
    }

    private void renderActiveView() {
        List<AuctionListItemPayload> auctions = auctionsForActiveView();
        sidebarSubtitle.setText(subtitleForSidebar());
        catalogTitle.setText(activeView);
        catalogSubtitle.setText(subtitleForCatalog(auctions.size()));
        setActiveNavigationState();
        if (VIEW_HOME.equals(activeView)) {
            renderHomeDashboard();
        } else {
            setNodeVisibility(homeDashboard, false);
            setNodeVisibility(catalogContent, true);
            renderAuctionItems(auctions);
        }
    }

    private List<Product> filteredProductsForActiveView() {
        List<Product> baseProducts = switch (activeView) {
            case VIEW_COMING_SOON -> comingSoonProducts();
            case VIEW_LIVE_AUCTION -> allCategoryProducts();
            case VIEW_BID_HISTORY -> bidHistoryProducts();
            case VIEW_MY_AUCTION -> myAuctionProducts();
            default -> allCategoryProducts();
        };

        if (CATEGORY_ALL.equals(activeCategory)) {
            return baseProducts;
        }

        List<Product> categoryProducts = productsByCategory.getOrDefault(activeCategory, List.of());
        return baseProducts.stream()
            .filter(categoryProducts::contains)
            .toList();
    }

    private List<Product> comingSoonProducts() {
        return combine(
            productsByCategory.getOrDefault(CATEGORY_ARTS, List.of()),
            productsByCategory.getOrDefault(CATEGORY_JEWELRY_WATCHES, List.of()),
            productsByCategory.getOrDefault(CATEGORY_COLLECTIBLES, List.of()).stream().limit(2).toList()
        );
    }

    private List<Product> bidHistoryProducts() {
        return allCategoryProducts().stream()
            .skip(2)
            .limit(6)
            .toList();
    }

    private List<Product> myAuctionProducts() {
        return combine(
            productsByCategory.getOrDefault(CATEGORY_COLLECTIBLES, List.of()).stream().limit(1).toList(),
            productsByCategory.getOrDefault(CATEGORY_ARTS, List.of()).stream().limit(2).toList(),
            productsByCategory.getOrDefault(CATEGORY_ELECTRONICS, List.of()).stream().limit(1).toList()
        );
    }

    private List<Product> allCategoryProducts() {
        List<Product> products = new ArrayList<>();
        for (List<Product> categoryProducts : productsByCategory.values()) {
            products.addAll(categoryProducts);
        }
        return List.copyOf(products);
    }

    private String subtitleForSidebar() {
        if (CATEGORY_ALL.equals(activeCategory)) {
            return activeView.toLowerCase();
        }
        return activeView.toLowerCase() + " / " + activeCategory;
    }

    private String subtitleForCatalog(int productCount) {
        String categoryLabel = CATEGORY_ALL.equals(activeCategory) ? "all categories" : activeCategory;
        return switch (activeView) {
            case VIEW_COMING_SOON -> productCount + " upcoming auctions in " + categoryLabel + ".";
            case VIEW_LIVE_AUCTION -> productCount + " active auctions in " + categoryLabel + ".";
            case VIEW_BID_HISTORY -> "Your bid records filtered by " + categoryLabel + ".";
            case VIEW_MY_AUCTION -> "Auctions you created, filtered by " + categoryLabel + ".";
            default -> "Previewing upcoming and live auctions across " + categoryLabel + ".";
        };
    }

    private void setActiveNavigationState() {
        for (Map.Entry<Button, String> entry : moduleButtons.entrySet()) {
            setStyleClassActive(entry.getKey(), "active-module", entry.getValue().equals(activeView));
        }
    }

    private void renderProducts(List<Product> products) {
        productFlow.getChildren().clear();

        boolean isEmpty = products.isEmpty();
        emptyStateLabel.setText(emptyMessage());
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);

        for (Product product : products) {
            productFlow.getChildren().add(createProductCard(product));
        }
    }

    private void renderAuctionItems(List<AuctionListItemPayload> auctions) {
        productFlow.getChildren().clear();

        boolean isEmpty = auctions.isEmpty();
        emptyStateLabel.setText(emptyMessage());
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);

        for (AuctionListItemPayload auction : auctions) {
            productFlow.getChildren().add(createProductCard(toProduct(auction)));
        }
    }

    private void renderHomeDashboard() {
        setNodeVisibility(homeDashboard, true);
        setNodeVisibility(catalogContent, false);
        renderPreviewFlow(
            comingSoonPreviewFlow,
            comingSoonPreviewAuctions.values().stream()
                .sorted(this::compareAuction)
                .map(this::toProduct)
                .toList()
        );

        renderPreviewFlow(
            liveAuctionPreviewFlow,
            livePreviewAuctions.values().stream()
                .sorted(this::compareAuction)
                .map(this::toProduct)
                .toList()
        );
    }

    private void renderPreviewFlow(FlowPane flowPane, List<Product> products) {
        flowPane.getChildren().clear();
        for (Product product : products) {
            flowPane.getChildren().add(createProductCard(product));
        }
    }

    private List<Product> filteredProducts(List<Product> products) {
        if (CATEGORY_ALL.equals(activeCategory)) {
            return products;
        }
        List<Product> categoryProducts = productsByCategory.getOrDefault(activeCategory, List.of());
        return products.stream()
            .filter(categoryProducts::contains)
            .toList();
    }

    private Product toProduct(AuctionListItemPayload payload) {
        String price = payload.getCurrentPrice() == null ? "-" : payload.getCurrentPrice().toPlainString();
        String imagePath = payload.getThumbnailBase64() == null || payload.getThumbnailBase64().isBlank()
            ? "/jfx/image/products/collectibles.png"
            : payload.getThumbnailBase64();

        return new Product(
            payload.getTitle(),
            "Auction #" + payload.getAuctionId(),
            price,
            price,
            "-",
            "Ends " + payload.getEndingTime(),
            0.5,
            imagePath
        );
    }

    private int compareAuction(AuctionListItemPayload left, AuctionListItemPayload right) {
        int byStartingTime = right.getStartingTime().compareTo(left.getStartingTime());
        if (byStartingTime != 0) {
            return byStartingTime;
        }
        return Long.compare(right.getAuctionId(), left.getAuctionId());
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
            return "No bid history found for this category.";
        }
        if (VIEW_MY_AUCTION.equals(activeView)) {
            return "No created auctions found for this category.";
        }
        return "No auctions available for this category.";
    }

    private void openMyAuctionAfterCreate() {
        restoreHomeLayout();
        activeCategory = CATEGORY_ALL;
        categoryFilterComboBox.setValue(CATEGORY_ALL_LABEL);
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
        homeRoot.setLeft(homeLeft);
        homeRoot.setRight(homeRight);
        homeRoot.setCenter(homeCenter);
        homeRoot.setBottom(homeBottom);
    }

    private void attachCreateAuctionStylesheet() {
        String stylesheet = getClass().getResource(CREATE_AUCTION_CSS).toExternalForm();
        if (!homeRoot.getScene().getStylesheets().contains(stylesheet)) {
            homeRoot.getScene().getStylesheets().add(stylesheet);
        }
    }

    private Node createProductCard(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jfx/scene/AuctionCard.fxml"));
            Node card = loader.load();
            AuctionCardController controller = loader.getController();
            controller.setProduct(product);
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

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
