package com.vbay.ui.scene_ui.controller.home;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.network.SocketClient;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.model.MockProductCatalog;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.scene_ui.controller.card.AuctionCardController;
import com.vbay.ui.scene_ui.controller.home.classes.ModuleSection;
import com.vbay.ui.scene_ui.controller.home.classes.SectionGroup;
import com.vbay.ui.scene_ui.controller.home.classes.ViewDefinition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import com.vbay.ui.scene_ui.NotificationManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {
    private static final String MODULE_CATEGORIES = "Collections";
    private static final String MODULE_DASHBOARD = "Dashboard";
    private static final String MODULE_MY_BID = "My Bids";
    private static final String MODULE_UPCOMING = "Coming Soon";
    private static final String MODULE_LIVE_AUCTIONS = "Live Auctions";

    private static final String SECTION_ELECTRONICS = "Electronics";
    private static final String SECTION_COLLECTIBLES = "Collectibles";
    private static final String SECTION_ART = "Art";
    private static final String SECTION_JEWELRY_WATCHES = "Jewelry & Watches";
    private static final String SECTION_SPORTING_GOODS = "Sporting Goods";

    private static final String DASHBOARD_HIGHLIGHTS = "Highlights";
    private static final String DASHBOARD_SELLER_DESK = "Seller Desk";
    private static final String MY_BID_ACTIVE = "Active Rooms";
    private static final String MY_BID_RESULTS = "Results";
    private static final String UPCOMING_SCHEDULE = "Schedule";
    private static final String UPCOMING_CURATED = "Curated Drops";
    private static final String LIVE_NOW = "Now Live";
    private static final String LIVE_PULSE = "Room Pulse";

    private static final String DASHBOARD_FEATURED_TECH = "dashboard.featuredTech";
    private static final String DASHBOARD_COLLECTOR_DESK = "dashboard.collectorDesk";
    private static final String DASHBOARD_DRAFT_WATCH = "dashboard.draftWatch";
    private static final String DASHBOARD_PERFORMANCE_BOARD = "dashboard.performanceBoard";
    private static final String MY_BID_OPEN_LOTS = "myBid.openLots";
    private static final String MY_BID_OUTBID_WATCH = "myBid.outbidWatch";
    private static final String MY_BID_WINNING_LOTS = "myBid.winningLots";
    private static final String MY_BID_WATCHLIST = "myBid.watchlist";
    private static final String UPCOMING_TONIGHT = "upcoming.tonight";
    private static final String UPCOMING_TOMORROW = "upcoming.tomorrow";
    private static final String UPCOMING_PREMIUM_DROPS = "upcoming.premiumDrops";
    private static final String UPCOMING_FRESH_LISTINGS = "upcoming.freshListings";
    private static final String LIVE_CLOSING_FAST = "live.closingFast";
    private static final String LIVE_TRENDING_LOTS = "live.trendingLots";
    private static final String LIVE_HIGH_STAKES = "live.highStakes";
    private static final String LIVE_COMPETITIVE_ROOM = "live.competitiveRoom";
    //Logger 
    private static final Logger LOGGER = LoggingUtils.getLogger(HomeController.class);
    @FXML
    private Button categoriesModuleButton;
    @FXML
    private Button dashboardModuleButton;
    @FXML
    private Button myBidModuleButton;
    @FXML
    private Button upcomingModuleButton;
    @FXML
    private Button liveAuctionsModuleButton;

    @FXML
    private VBox categoriesModulePanel;
    @FXML
    private VBox dashboardModulePanel;
    @FXML
    private VBox myBidModulePanel;
    @FXML
    private VBox upcomingModulePanel;
    @FXML
    private VBox liveAuctionsModulePanel;

    @FXML
    private Button electronicsButton;
    @FXML
    private Button collectiblesButton;
    @FXML
    private Button artButton;
    @FXML
    private Button jewelryWatchesButton;
    @FXML
    private Button sportingGoodsButton;
    @FXML
    private Button dashboardHighlightsButton;
    @FXML
    private Button dashboardSellerDeskButton;
    @FXML
    private Button myBidActiveButton;
    @FXML
    private Button myBidResultsButton;
    @FXML
    private Button upcomingScheduleButton;
    @FXML
    private Button upcomingCuratedButton;
    @FXML
    private Button liveNowButton;
    @FXML
    private Button livePulseButton;

    @FXML
    private VBox electronicsSubmenu;
    @FXML
    private VBox collectiblesSubmenu;
    @FXML
    private VBox artSubmenu;
    @FXML
    private VBox jewelryWatchesSubmenu;
    @FXML
    private VBox sportingGoodsSubmenu;
    @FXML
    private VBox dashboardHighlightsSubmenu;
    @FXML
    private VBox dashboardSellerDeskSubmenu;
    @FXML
    private VBox myBidActiveSubmenu;
    @FXML
    private VBox myBidResultsSubmenu;
    @FXML
    private VBox upcomingScheduleSubmenu;
    @FXML
    private VBox upcomingCuratedSubmenu;
    @FXML
    private VBox liveNowSubmenu;
    @FXML
    private VBox livePulseSubmenu;

    @FXML
    private Button macbooksButton;
    @FXML
    private Button phonesButton;
    @FXML
    private Button gamingButton;
    @FXML
    private Button audioButton;
    @FXML
    private Button cardsButton;
    @FXML
    private Button figuresButton;
    @FXML
    private Button stampsButton;
    @FXML
    private Button paintbrushesButton;
    @FXML
    private Button clocksButton;
    @FXML
    private Button earingsButton;
    @FXML
    private Button racketsButton;
    @FXML
    private Button pickleballRacketsButton;
    @FXML
    private Button dashboardFeaturedTechButton;
    @FXML
    private Button dashboardCollectorDeskButton;
    @FXML
    private Button dashboardDraftWatchButton;
    @FXML
    private Button dashboardPerformanceBoardButton;
    @FXML
    private Button myBidOpenLotsButton;
    @FXML
    private Button myBidOutbidWatchButton;
    @FXML
    private Button myBidWinningLotsButton;
    @FXML
    private Button myBidWatchlistButton;
    @FXML
    private Button upcomingTonightButton;
    @FXML
    private Button upcomingTomorrowButton;
    @FXML
    private Button upcomingPremiumDropsButton;
    @FXML
    private Button upcomingFreshListingsButton;
    @FXML
    private Button liveClosingFastButton;
    @FXML
    private Button liveTrendingLotsButton;
    @FXML
    private Button liveHighStakesButton;
    @FXML
    private Button liveCompetitiveRoomButton;

    @FXML
    private Label sidebarSubtitle;
    @FXML
    private Label catalogTitle;
    @FXML
    private Label catalogSubtitle;
    @FXML
    private FlowPane productFlow;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private HBox paginationBar;

    private Map<String, List<Product>> categoryData;
    private Map<String, List<Product>> viewData;
    private Map<Button, ModuleSection> moduleSections;
    private Map<Button, SectionGroup> sectionGroups;
    private Map<Button, ViewDefinition> viewDefinitions;
    private List<Button> subcategoryButtons;
    private List<Product> activeProducts = List.of();
    private String activeModuleKey;
    private String activeSectionKey;
    private String activeViewKey;
    private String emptyStateMessage = "Select a subcategory to display auctions.";

    @FXML
    private void initialize() {
        categoryData = MockProductCatalog.categoryData();
        viewData = initializeViewData();

        subcategoryButtons = List.of(
            macbooksButton,
            phonesButton,
            gamingButton,
            audioButton,
            cardsButton,
            figuresButton,
            stampsButton,
            paintbrushesButton,
            clocksButton,
            earingsButton,
            racketsButton,
            pickleballRacketsButton,
            dashboardFeaturedTechButton,
            dashboardCollectorDeskButton,
            dashboardDraftWatchButton,
            dashboardPerformanceBoardButton,
            myBidOpenLotsButton,
            myBidOutbidWatchButton,
            myBidWinningLotsButton,
            myBidWatchlistButton,
            upcomingTonightButton,
            upcomingTomorrowButton,
            upcomingPremiumDropsButton,
            upcomingFreshListingsButton,
            liveClosingFastButton,
            liveTrendingLotsButton,
            liveHighStakesButton,
            liveCompetitiveRoomButton
        );

        initializeModuleSections();
        initializeSectionGroups();
        initializeViewDefinitions();

        paginationBar.setVisible(false);
        paginationBar.setManaged(false);

        openModule(findModuleSection(MODULE_DASHBOARD));
    }

    @FXML
    private void handleModuleToggle(ActionEvent event) {
        ModuleSection section = moduleSections.get(event.getSource());
        if (section == null) {
            return;
        }

        if (section.key.equals(activeModuleKey)) {
            collapseAllModules();
            return;
        }

        openModule(section);
    }

    @FXML
    private void handleSectionToggle(ActionEvent event) {
        SectionGroup section = sectionGroups.get(event.getSource());
        if (section == null || !section.moduleKey.equals(activeModuleKey)) {
            return;
        }

        if (section.key.equals(activeSectionKey)) {
            collapseSection(section);
            return;
        }

        openSection(section);
    }

    @FXML
    private void handleSubcategorySelection(ActionEvent event) {
        ViewDefinition definition = viewDefinitions.get(event.getSource());
        if (definition == null || !definition.moduleKey.equals(activeModuleKey)) {
            return;
        }

        activeViewKey = definition.key;
        activeProducts = viewData.getOrDefault(definition.key, List.of());
        emptyStateMessage = "No products available for " + definition.displayName + " yet.";

        setActiveSubSelection(definition.button);
        sidebarSubtitle.setText(definition.moduleKey + " / " + definition.sectionName + " / " + definition.displayName);
        catalogTitle.setText(definition.displayName);
        catalogSubtitle.setText(definition.subtitle);

        renderCurrentPage();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            logoutUser();
        } catch (IOException exception) {
            exception.printStackTrace();
            showMessage(
                NotificationManager.NotificationType.ERROR,
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
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                exception.getMessage() != null ? exception.getMessage() : "Could not return to the login screen."
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
    }

    @FXML
    private void handleCreateAuctions(ActionEvent event) {
        try {
            NotificationManager.show(
                NotificationManager.NotificationType.SUCCESS,
                "Auction Created",
                "Your auction has been created successfully. Redirecting..."
            );
            // Simulate redirection for now as requested
            javafx.application.Platform.runLater(() -> {
                try {
                    SceneManager.switchScene("/jfx/scene/Home.fxml");
                } catch (Exception e) {
                    LOGGER.log(java.util.logging.Level.SEVERE, "Fail to refresh home", e);
                }
            });
        } catch (Exception exception) {
            NotificationManager.show(
                NotificationManager.NotificationType.ERROR,
                "Fail Creation",
                "Could not create Auction"
            );
        }
    }

    public void renderProducts(List<Product> products) {
        productFlow.getChildren().clear();

        boolean isEmpty = products.isEmpty();
        emptyStateLabel.setText(emptyStateMessage);
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);

        for (Product product : products) {
            Node card = createProductCard(product);
            productFlow.getChildren().add(card);
        }
    }

    public void handleCardClick(Product product) {
        try {
            SceneManager.switchScene("/jfx/scene/bid/Bid.fxml", product);
        } catch (Exception exception) {
            showMessage(
                NotificationManager.NotificationType.ERROR,
                "Navigation failed",
                "Could not open the placebid detail scene for the selected product."
            );
        }
    }

    private Map<String, List<Product>> initializeViewData() {
        Map<String, List<Product>> data = new LinkedHashMap<>(categoryData);

        data.put(DASHBOARD_FEATURED_TECH, categoryData.getOrDefault(MockProductCatalog.ELECTRONICS_MACBOOKS, List.of()));
        data.put(DASHBOARD_COLLECTOR_DESK, categoryData.getOrDefault(MockProductCatalog.COLLECTIBLES_CARDS, List.of()));
        data.put(DASHBOARD_DRAFT_WATCH, categoryData.getOrDefault(MockProductCatalog.ART_PAINTBRUSHES, List.of()));
        data.put(DASHBOARD_PERFORMANCE_BOARD, categoryData.getOrDefault(MockProductCatalog.SPORTING_RACKETS, List.of()));

        data.put(MY_BID_OPEN_LOTS, categoryData.getOrDefault(MockProductCatalog.ELECTRONICS_PHONES, List.of()));
        data.put(MY_BID_OUTBID_WATCH, categoryData.getOrDefault(MockProductCatalog.COLLECTIBLES_FIGURES, List.of()));
        data.put(MY_BID_WINNING_LOTS, categoryData.getOrDefault(MockProductCatalog.JEWELRY_CLOCKS, List.of()));
        data.put(MY_BID_WATCHLIST, categoryData.getOrDefault(MockProductCatalog.ELECTRONICS_AUDIO, List.of()));

        data.put(UPCOMING_TONIGHT, categoryData.getOrDefault(MockProductCatalog.SPORTING_PICKLEBALL, List.of()));
        data.put(UPCOMING_TOMORROW, categoryData.getOrDefault(MockProductCatalog.ART_STAMPS, List.of()));
        data.put(UPCOMING_PREMIUM_DROPS, categoryData.getOrDefault(MockProductCatalog.JEWELRY_EARRINGS, List.of()));
        data.put(UPCOMING_FRESH_LISTINGS, categoryData.getOrDefault(MockProductCatalog.COLLECTIBLES_CARDS, List.of()));

        data.put(LIVE_CLOSING_FAST, categoryData.getOrDefault(MockProductCatalog.ELECTRONICS_GAMING, List.of()));
        data.put(LIVE_TRENDING_LOTS, categoryData.getOrDefault(MockProductCatalog.ELECTRONICS_MACBOOKS, List.of()));
        data.put(LIVE_HIGH_STAKES, categoryData.getOrDefault(MockProductCatalog.JEWELRY_CLOCKS, List.of()));
        data.put(LIVE_COMPETITIVE_ROOM, categoryData.getOrDefault(MockProductCatalog.SPORTING_PICKLEBALL, List.of()));

        return Map.copyOf(data);
    }

    private void initializeModuleSections() {
        Map<Button, ModuleSection> sections = new LinkedHashMap<>();
        sections.put(categoriesModuleButton, new ModuleSection(MODULE_CATEGORIES, categoriesModuleButton, categoriesModulePanel));
        sections.put(dashboardModuleButton, new ModuleSection(MODULE_DASHBOARD, dashboardModuleButton, dashboardModulePanel));
        sections.put(myBidModuleButton, new ModuleSection(MODULE_MY_BID, myBidModuleButton, myBidModulePanel));
        sections.put(upcomingModuleButton, new ModuleSection(MODULE_UPCOMING, upcomingModuleButton, upcomingModulePanel));
        sections.put(
            liveAuctionsModuleButton,
            new ModuleSection(MODULE_LIVE_AUCTIONS, liveAuctionsModuleButton, liveAuctionsModulePanel)
        );
        moduleSections = Map.copyOf(sections);
    }

    private void initializeSectionGroups() {
        Map<Button, SectionGroup> sections = new LinkedHashMap<>();

        sections.put(
            electronicsButton,
            new SectionGroup(MODULE_CATEGORIES, SECTION_ELECTRONICS, electronicsButton, electronicsSubmenu)
        );
        sections.put(
            collectiblesButton,
            new SectionGroup(MODULE_CATEGORIES, SECTION_COLLECTIBLES, collectiblesButton, collectiblesSubmenu)
        );
        sections.put(
            artButton,
            new SectionGroup(MODULE_CATEGORIES, SECTION_ART, artButton, artSubmenu)
        );
        sections.put(
            jewelryWatchesButton,
            new SectionGroup(MODULE_CATEGORIES, SECTION_JEWELRY_WATCHES, jewelryWatchesButton, jewelryWatchesSubmenu)
        );
        sections.put(
            sportingGoodsButton,
            new SectionGroup(MODULE_CATEGORIES, SECTION_SPORTING_GOODS, sportingGoodsButton, sportingGoodsSubmenu)
        );
        sections.put(
            dashboardHighlightsButton,
            new SectionGroup(MODULE_DASHBOARD, DASHBOARD_HIGHLIGHTS, dashboardHighlightsButton, dashboardHighlightsSubmenu)
        );
        sections.put(
            dashboardSellerDeskButton,
            new SectionGroup(MODULE_DASHBOARD, DASHBOARD_SELLER_DESK, dashboardSellerDeskButton, dashboardSellerDeskSubmenu)
        );
        sections.put(
            myBidActiveButton,
            new SectionGroup(MODULE_MY_BID, MY_BID_ACTIVE, myBidActiveButton, myBidActiveSubmenu)
        );
        sections.put(
            myBidResultsButton,
            new SectionGroup(MODULE_MY_BID, MY_BID_RESULTS, myBidResultsButton, myBidResultsSubmenu)
        );
        sections.put(
            upcomingScheduleButton,
            new SectionGroup(MODULE_UPCOMING, UPCOMING_SCHEDULE, upcomingScheduleButton, upcomingScheduleSubmenu)
        );
        sections.put(
            upcomingCuratedButton,
            new SectionGroup(MODULE_UPCOMING, UPCOMING_CURATED, upcomingCuratedButton, upcomingCuratedSubmenu)
        );
        sections.put(
            liveNowButton,
            new SectionGroup(MODULE_LIVE_AUCTIONS, LIVE_NOW, liveNowButton, liveNowSubmenu)
        );
        sections.put(
            livePulseButton,
            new SectionGroup(MODULE_LIVE_AUCTIONS, LIVE_PULSE, livePulseButton, livePulseSubmenu)
        );

        sectionGroups = Map.copyOf(sections);
    }

    private void initializeViewDefinitions() {
        Map<Button, ViewDefinition> definitions = new LinkedHashMap<>();

        definitions.put(
            macbooksButton,
            new ViewDefinition(
                MockProductCatalog.ELECTRONICS_MACBOOKS,
                MODULE_CATEGORIES,
                SECTION_ELECTRONICS,
                macbooksButton,
                "MacBooks",
                "High-end Apple laptops and premium creator machines."
            )
        );
        definitions.put(
            phonesButton,
            new ViewDefinition(
                MockProductCatalog.ELECTRONICS_PHONES,
                MODULE_CATEGORIES,
                SECTION_ELECTRONICS,
                phonesButton,
                "Phones",
                "Flagship smartphones, foldables, and sealed collector devices."
            )
        );
        definitions.put(
            gamingButton,
            new ViewDefinition(
                MockProductCatalog.ELECTRONICS_GAMING,
                MODULE_CATEGORIES,
                SECTION_ELECTRONICS,
                gamingButton,
                "Gaming",
                "Tournament-ready rigs, GPUs, and enthusiast desktop bundles."
            )
        );
        definitions.put(
            audioButton,
            new ViewDefinition(
                MockProductCatalog.ELECTRONICS_AUDIO,
                MODULE_CATEGORIES,
                SECTION_ELECTRONICS,
                audioButton,
                "Audio",
                "Studio-grade headphones, DACs, and audiophile desk setups."
            )
        );
        definitions.put(
            cardsButton,
            new ViewDefinition(
                MockProductCatalog.COLLECTIBLES_CARDS,
                MODULE_CATEGORIES,
                SECTION_COLLECTIBLES,
                cardsButton,
                "Cards",
                "Graded hits, sealed hobby boxes, and investment-grade sports cards."
            )
        );
        definitions.put(
            figuresButton,
            new ViewDefinition(
                MockProductCatalog.COLLECTIBLES_FIGURES,
                MODULE_CATEGORIES,
                SECTION_COLLECTIBLES,
                figuresButton,
                "Figures",
                "Display statues, die-cast heroes, and limited collector runs."
            )
        );
        definitions.put(
            stampsButton,
            new ViewDefinition(
                MockProductCatalog.ART_STAMPS,
                MODULE_CATEGORIES,
                SECTION_ART,
                stampsButton,
                "Stamps",
                "Historic postal rarities and archive-worthy philatelic sets."
            )
        );
        definitions.put(
            paintbrushesButton,
            new ViewDefinition(
                MockProductCatalog.ART_PAINTBRUSHES,
                MODULE_CATEGORIES,
                SECTION_ART,
                paintbrushesButton,
                "Paintbrushes",
                "Studio brush kits and handcrafted tools for atelier work."
            )
        );
        definitions.put(
            clocksButton,
            new ViewDefinition(
                MockProductCatalog.JEWELRY_CLOCKS,
                MODULE_CATEGORIES,
                SECTION_JEWELRY_WATCHES,
                clocksButton,
                "Clocks",
                "Mechanical desk clocks and statement collector timepieces."
            )
        );
        definitions.put(
            earingsButton,
            new ViewDefinition(
                MockProductCatalog.JEWELRY_EARRINGS,
                MODULE_CATEGORIES,
                SECTION_JEWELRY_WATCHES,
                earingsButton,
                "Earrings",
                "Diamond drops, halo sets, and event-ready fine jewelry."
            )
        );
        definitions.put(
            racketsButton,
            new ViewDefinition(
                MockProductCatalog.SPORTING_RACKETS,
                MODULE_CATEGORIES,
                SECTION_SPORTING_GOODS,
                racketsButton,
                "Rackets",
                "Tour-level badminton frames and pro-stock match gear."
            )
        );
        definitions.put(
            pickleballRacketsButton,
            new ViewDefinition(
                MockProductCatalog.SPORTING_PICKLEBALL,
                MODULE_CATEGORIES,
                SECTION_SPORTING_GOODS,
                pickleballRacketsButton,
                "Pickleball Rackets",
                "Competition paddles, carbon faces, and starter bundles."
            )
        );
        definitions.put(
            dashboardFeaturedTechButton,
            new ViewDefinition(
                DASHBOARD_FEATURED_TECH,
                MODULE_DASHBOARD,
                DASHBOARD_HIGHLIGHTS,
                dashboardFeaturedTechButton,
                "Featured Tech",
                "A quick dashboard cut of premium devices drawing the most attention."
            )
        );
        definitions.put(
            dashboardCollectorDeskButton,
            new ViewDefinition(
                DASHBOARD_COLLECTOR_DESK,
                MODULE_DASHBOARD,
                DASHBOARD_HIGHLIGHTS,
                dashboardCollectorDeskButton,
                "Collector Desk",
                "High-conviction collector pieces surfaced for quick review."
            )
        );
        definitions.put(
            dashboardDraftWatchButton,
            new ViewDefinition(
                DASHBOARD_DRAFT_WATCH,
                MODULE_DASHBOARD,
                DASHBOARD_SELLER_DESK,
                dashboardDraftWatchButton,
                "Draft Watch",
                "A seller-focused view for listings that still need final attention."
            )
        );
        definitions.put(
            dashboardPerformanceBoardButton,
            new ViewDefinition(
                DASHBOARD_PERFORMANCE_BOARD,
                MODULE_DASHBOARD,
                DASHBOARD_SELLER_DESK,
                dashboardPerformanceBoardButton,
                "Performance Board",
                "Items with clean bidding momentum and strong marketplace pacing."
            )
        );
        definitions.put(
            myBidOpenLotsButton,
            new ViewDefinition(
                MY_BID_OPEN_LOTS,
                MODULE_MY_BID,
                MY_BID_ACTIVE,
                myBidOpenLotsButton,
                "Open Lots",
                "Your currently active bidding rooms and live tracked lots."
            )
        );
        definitions.put(
            myBidOutbidWatchButton,
            new ViewDefinition(
                MY_BID_OUTBID_WATCH,
                MODULE_MY_BID,
                MY_BID_ACTIVE,
                myBidOutbidWatchButton,
                "Outbid Watch",
                "Rooms where the pace changed and your position may need attention."
            )
        );
        definitions.put(
            myBidWinningLotsButton,
            new ViewDefinition(
                MY_BID_WINNING_LOTS,
                MODULE_MY_BID,
                MY_BID_RESULTS,
                myBidWinningLotsButton,
                "Winning Lots",
                "Auction rooms where you are currently leading or near the top."
            )
        );
        definitions.put(
            myBidWatchlistButton,
            new ViewDefinition(
                MY_BID_WATCHLIST,
                MODULE_MY_BID,
                MY_BID_RESULTS,
                myBidWatchlistButton,
                "Watchlist",
                "Saved lots worth revisiting before the next decision point."
            )
        );
        definitions.put(
            upcomingTonightButton,
            new ViewDefinition(
                UPCOMING_TONIGHT,
                MODULE_UPCOMING,
                UPCOMING_SCHEDULE,
                upcomingTonightButton,
                "Tonight",
                "Auctions lining up for the next session window."
            )
        );
        definitions.put(
            upcomingTomorrowButton,
            new ViewDefinition(
                UPCOMING_TOMORROW,
                MODULE_UPCOMING,
                UPCOMING_SCHEDULE,
                upcomingTomorrowButton,
                "Tomorrow",
                "A forward look at the next batch of scheduled lots."
            )
        );
        definitions.put(
            upcomingPremiumDropsButton,
            new ViewDefinition(
                UPCOMING_PREMIUM_DROPS,
                MODULE_UPCOMING,
                UPCOMING_CURATED,
                upcomingPremiumDropsButton,
                "Premium Drops",
                "High-value releases expected to pull stronger bidding activity."
            )
        );
        definitions.put(
            upcomingFreshListingsButton,
            new ViewDefinition(
                UPCOMING_FRESH_LISTINGS,
                MODULE_UPCOMING,
                UPCOMING_CURATED,
                upcomingFreshListingsButton,
                "Fresh Listings",
                "Newly surfaced inventory worth scanning before rooms get crowded."
            )
        );
        definitions.put(
            liveClosingFastButton,
            new ViewDefinition(
                LIVE_CLOSING_FAST,
                MODULE_LIVE_AUCTIONS,
                LIVE_NOW,
                liveClosingFastButton,
                "Closing Fast",
                "Live rooms pressing into their last bidding windows."
            )
        );
        definitions.put(
            liveTrendingLotsButton,
            new ViewDefinition(
                LIVE_TRENDING_LOTS,
                MODULE_LIVE_AUCTIONS,
                LIVE_NOW,
                liveTrendingLotsButton,
                "Trending Lots",
                "The most watched live inventory across the current floor."
            )
        );
        definitions.put(
            liveHighStakesButton,
            new ViewDefinition(
                LIVE_HIGH_STAKES,
                MODULE_LIVE_AUCTIONS,
                LIVE_PULSE,
                liveHighStakesButton,
                "High Stakes",
                "Premium rooms where pricing is moving aggressively."
            )
        );
        definitions.put(
            liveCompetitiveRoomButton,
            new ViewDefinition(
                LIVE_COMPETITIVE_ROOM,
                MODULE_LIVE_AUCTIONS,
                LIVE_PULSE,
                liveCompetitiveRoomButton,
                "Competitive Room",
                "Fast-response bidding rooms where timing matters most."
            )
        );

        viewDefinitions = Map.copyOf(definitions);
    }

    private void openModule(ModuleSection selectedModule) {
        activeModuleKey = selectedModule.key;
        activeSectionKey = null;
        activeViewKey = null;
        activeProducts = List.of();
        emptyStateMessage = "Select a subcategory to display auctions.";

        setModuleVisibility(selectedModule);
        resetSectionState();
        clearSubcategorySelection();
        renderModulePrompt(selectedModule);
    }

    private void collapseAllModules() {
        activeModuleKey = null;
        activeSectionKey = null;
        activeViewKey = null;
        activeProducts = List.of();
        emptyStateMessage = "Open a module, then choose a subcategory to display auctions.";

        for (ModuleSection module : moduleSections.values()) {
            setNodeVisibility(module.button, true);
            setNodeVisibility(module.panel, false);
            setModuleButtonState(module, false);
        }

        resetSectionState();
        clearSubcategorySelection();
        sidebarSubtitle.setText("Open a module to browse auctions.");
        catalogTitle.setText("Auction Explorer");
        catalogSubtitle.setText("Choose a module on the left, then drill into a subcategory to load products.");
        renderCurrentPage();
    }

    private void openSection(SectionGroup selectedSection) {
        activeSectionKey = selectedSection.key;
        activeViewKey = null;
        activeProducts = List.of();
        emptyStateMessage = "Select a subcategory to display auctions.";

        for (SectionGroup section : sectionGroups.values()) {
            boolean sameModule = section.moduleKey.equals(activeModuleKey);
            boolean expanded = sameModule && section == selectedSection;
            setNodeVisibility(section.submenu, expanded);
            setSectionButtonState(section, expanded);
        }

        clearSubcategorySelection();
        sidebarSubtitle.setText(selectedSection.moduleKey + " / " + selectedSection.key);
        catalogTitle.setText(selectedSection.key);
        catalogSubtitle.setText("Choose a subcategory to show the auction list for this section.");
        renderCurrentPage();
    }

    private void collapseSection(SectionGroup section) {
        activeSectionKey = null;
        activeViewKey = null;
        activeProducts = List.of();
        emptyStateMessage = "Select a subcategory to display auctions.";

        setNodeVisibility(section.submenu, false);
        setSectionButtonState(section, false);
        clearSubcategorySelection();
        renderModulePrompt(findModuleSection(section.moduleKey));
    }

    private void renderModulePrompt(ModuleSection module) {
        if (module == null) {
            return;
        }

        sidebarSubtitle.setText(module.key);
        catalogTitle.setText(module.key);
        catalogSubtitle.setText("Open one of the groups below and choose a subcategory to display auctions.");
        renderCurrentPage();
    }

    private void setModuleVisibility(ModuleSection selectedModule) {
        for (ModuleSection module : moduleSections.values()) {
            boolean isActive = module == selectedModule;
            setNodeVisibility(module.button, true);
            setNodeVisibility(module.panel, isActive);
            setModuleButtonState(module, isActive);
        }
    }

    private void resetSectionState() {
        for (SectionGroup section : sectionGroups.values()) {
            setNodeVisibility(section.submenu, false);
            setSectionButtonState(section, false);
        }
    }

    private void setModuleButtonState(ModuleSection module, boolean active) {
        module.button.getStyleClass().remove("active-module");
        if (active && !module.button.getStyleClass().contains("active-module")) {
            module.button.getStyleClass().add("active-module");
        }
    }

    private void setSectionButtonState(SectionGroup section, boolean active) {
        section.button.setText(section.key + (active ? " -" : " +"));
        section.button.getStyleClass().remove("active-section");
        if (active && !section.button.getStyleClass().contains("active-section")) {
            section.button.getStyleClass().add("active-section");
        }
    }

    private void clearSubcategorySelection() {
        for (Button button : subcategoryButtons) {
            button.getStyleClass().remove("active-sub");
        }
    }

    private void setActiveSubSelection(Button selectedSubButton) {
        clearSubcategorySelection();
        if (!selectedSubButton.getStyleClass().contains("active-sub")) {
            selectedSubButton.getStyleClass().add("active-sub");
        }
    }

    private void renderCurrentPage() {
        renderProducts(activeProducts);
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

    private ModuleSection findModuleSection(String key) {
        for (ModuleSection section : moduleSections.values()) {
            if (section.key.equals(key)) {
                return section;
            }
        }
        return null;
    }

    private void setNodeVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void showMessage(NotificationManager.NotificationType type, String title, String content) {
        NotificationManager.show(type, title, content);
    }

}
