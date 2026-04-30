package com.vbay.ui.scene.controller.account;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vbay.ui.model.MockProductCatalog;
import com.vbay.ui.model.Product;
import com.vbay.ui.scene.SceneManager;
import com.vbay.ui.scene.controller.AuctionCardController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {
    private static final String ELECTRONICS = "Electronics";
    private static final String COLLECTIBLES = "Collectibles";
    private static final String ART = "Art";
    private static final String JEWELRY_WATCHES = "Jewelry & Watches";
    private static final String SPORTING_GOODS = "Sporting Goods";
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
    private Map<Button, CategorySection> categorySections;
    private Map<Button, SubcategoryDefinition> subcategoryDefinitions;
    private List<Button> categoryButtons;
    private List<Button> subcategoryButtons;
    private List<Product> activeProducts = List.of();
    @FXML
    private void initialize() {
        categoryData = MockProductCatalog.categoryData();
        categoryButtons = List.of(
            electronicsButton,
            collectiblesButton,
            artButton,
            jewelryWatchesButton,
            sportingGoodsButton
        );
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
            pickleballRacketsButton
        );

        initializeCategorySections();
        initializeSubcategoryDefinitions();
        paginationBar.setVisible(false);
        paginationBar.setManaged(false);
        selectSubcategory(MockProductCatalog.ELECTRONICS_MACBOOKS);
    }

    @FXML
    private void handleCategoryToggle(ActionEvent event) {
        CategorySection section = categorySections.get(event.getSource());
        if (section == null) {
            return;
        }

        selectSubcategory(section.defaultSubcategoryKey);
    }

    @FXML
    private void handleSubcategorySelection(ActionEvent event) {
        SubcategoryDefinition definition = subcategoryDefinitions.get(event.getSource());
        if (definition == null) {
            return;
        }

        selectSubcategory(definition.key);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/account/login.fxml");
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Logout failed",
                "Could not return to the login screen."
            );
        }
    }

    public void renderProducts(List<Product> products) {
        productFlow.getChildren().clear();

        boolean isEmpty = products.isEmpty();
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);

        for (Product product : products) {
            Node card = createProductCard(product);
            productFlow.getChildren().add(card);
        }
    }

    public void handleCardClick(Product product) {
        try {
            SceneManager.switchScene("/jfx/scene/bid/bid.fxml", product);
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Navigation failed",
                "Could not open the placebid detail scene for the selected product."
            );
        }
    }

    private void initializeCategorySections() {
        Map<Button, CategorySection> sections = new LinkedHashMap<>();
        sections.put(
            electronicsButton,
            new CategorySection(ELECTRONICS, electronicsButton, electronicsSubmenu, MockProductCatalog.ELECTRONICS_MACBOOKS)
        );
        sections.put(
            collectiblesButton,
            new CategorySection(COLLECTIBLES, collectiblesButton, collectiblesSubmenu, MockProductCatalog.COLLECTIBLES_CARDS)
        );
        sections.put(
            artButton,
            new CategorySection(ART, artButton, artSubmenu, MockProductCatalog.ART_STAMPS)
        );
        sections.put(
            jewelryWatchesButton,
            new CategorySection(
                JEWELRY_WATCHES,
                jewelryWatchesButton,
                jewelryWatchesSubmenu,
                MockProductCatalog.JEWELRY_CLOCKS
            )
        );
        sections.put(
            sportingGoodsButton,
            new CategorySection(
                SPORTING_GOODS,
                sportingGoodsButton,
                sportingGoodsSubmenu,
                MockProductCatalog.SPORTING_RACKETS
            )
        );
        categorySections = Collections.unmodifiableMap(sections);
    }

    private void initializeSubcategoryDefinitions() {
        Map<Button, SubcategoryDefinition> definitions = new LinkedHashMap<>();
        definitions.put(
            macbooksButton,
            new SubcategoryDefinition(
                MockProductCatalog.ELECTRONICS_MACBOOKS,
                ELECTRONICS,
                macbooksButton,
                "MacBooks",
                "High-end Apple laptops and premium creator machines."
            )
        );
        definitions.put(
            phonesButton,
            new SubcategoryDefinition(
                MockProductCatalog.ELECTRONICS_PHONES,
                ELECTRONICS,
                phonesButton,
                "Phones",
                "Flagship smartphones, foldables, and sealed collector devices."
            )
        );
        definitions.put(
            gamingButton,
            new SubcategoryDefinition(
                MockProductCatalog.ELECTRONICS_GAMING,
                ELECTRONICS,
                gamingButton,
                "Gaming",
                "Tournament-ready rigs, GPUs, and enthusiast desktop bundles."
            )
        );
        definitions.put(
            audioButton,
            new SubcategoryDefinition(
                MockProductCatalog.ELECTRONICS_AUDIO,
                ELECTRONICS,
                audioButton,
                "Audio",
                "Studio-grade headphones, DACs, and audiophile desk setups."
            )
        );
        definitions.put(
            cardsButton,
            new SubcategoryDefinition(
                MockProductCatalog.COLLECTIBLES_CARDS,
                COLLECTIBLES,
                cardsButton,
                "Cards",
                "Graded hits, sealed hobby boxes, and investment-grade sports cards."
            )
        );
        definitions.put(
            figuresButton,
            new SubcategoryDefinition(
                MockProductCatalog.COLLECTIBLES_FIGURES,
                COLLECTIBLES,
                figuresButton,
                "Figures",
                "Display statues, die-cast heroes, and limited collector runs."
            )
        );
        definitions.put(
            stampsButton,
            new SubcategoryDefinition(
                MockProductCatalog.ART_STAMPS,
                ART,
                stampsButton,
                "Stamps",
                "Historic postal rarities and archive-worthy philatelic sets."
            )
        );
        definitions.put(
            paintbrushesButton,
            new SubcategoryDefinition(
                MockProductCatalog.ART_PAINTBRUSHES,
                ART,
                paintbrushesButton,
                "Paintbrushes",
                "Studio brush kits and handcrafted tools for atelier work."
            )
        );
        definitions.put(
            clocksButton,
            new SubcategoryDefinition(
                MockProductCatalog.JEWELRY_CLOCKS,
                JEWELRY_WATCHES,
                clocksButton,
                "Clocks",
                "Mechanical desk clocks and statement collector timepieces."
            )
        );
        definitions.put(
            earingsButton,
            new SubcategoryDefinition(
                MockProductCatalog.JEWELRY_EARRINGS,
                JEWELRY_WATCHES,
                earingsButton,
                "Earrings",
                "Diamond drops, halo sets, and event-ready fine jewelry."
            )
        );
        definitions.put(
            racketsButton,
            new SubcategoryDefinition(
                MockProductCatalog.SPORTING_RACKETS,
                SPORTING_GOODS,
                racketsButton,
                "Rackets",
                "Tour-level badminton frames and pro-stock match gear."
            )
        );
        definitions.put(
            pickleballRacketsButton,
            new SubcategoryDefinition(
                MockProductCatalog.SPORTING_PICKLEBALL,
                SPORTING_GOODS,
                pickleballRacketsButton,
                "Pickleball Rackets",
                "Competition paddles, carbon faces, and starter bundles."
            )
        );
        subcategoryDefinitions = Collections.unmodifiableMap(definitions);
    }

    private void selectSubcategory(String subcategoryKey) {
        SubcategoryDefinition definition = findSubcategoryDefinition(subcategoryKey);
        if (definition == null) {
            return;
        }

        CategorySection section = findCategorySection(definition.categoryName);
        if (section == null) {
            return;
        }

        activeProducts = categoryData.getOrDefault(definition.key, List.of());

        setActiveCategory(section.button);
        setCategoryMenuVisibility(section);
        setActiveSubSelection(definition.button);
        sidebarSubtitle.setText(definition.categoryName + " / " + definition.displayName);
        catalogTitle.setText(definition.displayName);
        catalogSubtitle.setText(definition.subtitle);

        renderCurrentPage();
    }

    private void setCategoryMenuVisibility(CategorySection selectedSection) {
        for (CategorySection section : categorySections.values()) {
            boolean visible = section == selectedSection;
            section.submenu.setVisible(visible);
            section.submenu.setManaged(visible);
            section.button.setText(section.categoryName + (visible ? " -" : " +"));
        }
    }

    private void setActiveCategory(Button selectedCategoryButton) {
        for (Button button : categoryButtons) {
            button.getStyleClass().remove("active");
        }
        if (!selectedCategoryButton.getStyleClass().contains("active")) {
            selectedCategoryButton.getStyleClass().add("active");
        }
    }

    private void setActiveSubSelection(Button selectedSubButton) {
        for (Button button : subcategoryButtons) {
            button.getStyleClass().remove("active-sub");
        }
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

    private CategorySection findCategorySection(String categoryName) {
        for (CategorySection section : categorySections.values()) {
            if (section.categoryName.equals(categoryName)) {
                return section;
            }
        }
        return null;
    }

    private SubcategoryDefinition findSubcategoryDefinition(String key) {
        for (SubcategoryDefinition definition : subcategoryDefinitions.values()) {
            if (definition.key.equals(key)) {
                return definition;
            }
        }
        return null;
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static final class CategorySection {
        private final String categoryName;
        private final Button button;
        private final VBox submenu;
        private final String defaultSubcategoryKey;

        private CategorySection(String categoryName, Button button, VBox submenu, String defaultSubcategoryKey) {
            this.categoryName = categoryName;
            this.button = button;
            this.submenu = submenu;
            this.defaultSubcategoryKey = defaultSubcategoryKey;
        }
    }

    private static final class SubcategoryDefinition {
        private final String key;
        private final String categoryName;
        private final Button button;
        private final String displayName;
        private final String subtitle;

        private SubcategoryDefinition(
            String key,
            String categoryName,
            Button button,
            String displayName,
            String subtitle
        ) {
            this.key = key;
            this.categoryName = categoryName;
            this.button = button;
            this.displayName = displayName;
            this.subtitle = subtitle;
        }
    }
}
