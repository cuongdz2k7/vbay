package com.vbay.ui.scene.controller;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import com.vbay.ui.scene.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class homecontroller {
    private static final String ELECTRONICS = "Electronics";
    private static final String COLLECTIBLES = "Collectibles";
    private static final String ART = "Art";
    private static final String JEWELRY_WATCHES = "Jewelry & Watches";
    private static final String SPORTING_GOODS = "Sporting Goods";
    private static final String DEFAULT_PRODUCT_IMAGE = "/jfx/image/logo_Hust.png";

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
    private Label featuredCardTitle;
    @FXML
    private Label featuredPriceLabel;
    @FXML
    private Label featuredTimeLabel;
    @FXML
    private ProgressBar featuredProgressBar;
    @FXML
    private Hyperlink featuredImageLink;
    @FXML
    private ImageView featuredImageView;
    @FXML
    private Label paginationLabel;
    @FXML
    private Button previousPageButton;
    @FXML
    private Button pageOneButton;
    @FXML
    private Button pageTwoButton;
    @FXML
    private Button pageThreeButton;
    @FXML
    private Button nextPageButton;

    private List<Button> categoryButtons;
    private List<Button> subCategoryButtons;
    private List<Button> electronicsPageButtons;
    private List<Button> collectiblesPageButtons;
    private List<Button> artPageButtons;
    private List<Button> jewelryWatchesPageButtons;
    private List<Button> sportingGoodsPageButtons;
    private List<Button> currentPageButtons;
    private String currentProductLink;
    private int currentPageIndex;

    @FXML
    private void initialize() {
        categoryButtons = List.of(
            electronicsButton,
            collectiblesButton,
            artButton,
            jewelryWatchesButton,
            sportingGoodsButton
        );
        subCategoryButtons = List.of(
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
        electronicsPageButtons = List.of(macbooksButton, phonesButton, gamingButton, audioButton);
        collectiblesPageButtons = List.of(cardsButton, figuresButton);
        artPageButtons = List.of(stampsButton, paintbrushesButton);
        jewelryWatchesPageButtons = List.of(clocksButton, earingsButton);
        sportingGoodsPageButtons = List.of(racketsButton, pickleballRacketsButton);

        applySubCategorySelection(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            macbooksButton,
            "MacBooks",
            "High-end Apple laptops and workstation bidding.",
            "Apple MacBook Pro 16 M3 Max",
            "Current Bid: $3,450.00",
            "Time Left: 2h 45m",
            0.85,
            "/jfx/image/products/macbooks.png",
            "https://example.com/products/macbooks"
        );
        setPaginationGroup(electronicsPageButtons, 0);
    }

    @FXML
    private void toggleElectronicsMenu(ActionEvent event) {
        toggleCategoryMenu(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            "Premium devices, creator workstations, and flagship tech auctions.",
            "Apple Vision Pro Launch Edition",
            "Current Bid: $2,980.00",
            "Time Left: 4h 12m",
            0.62,
            "/jfx/image/products/rams.png",
            "https://example.com/categories/electronics"
        );
        setPaginationGroup(electronicsPageButtons, 0);
    }

    @FXML
    private void toggleCollectiblesMenu(ActionEvent event) {
        toggleCategoryMenu(
            collectiblesButton,
            collectiblesSubmenu,
            COLLECTIBLES,
            "Rare memorabilia, sealed sets, and investment-grade finds.",
            "1999 Pokemon Base Set Booster Box",
            "Current Bid: $8,900.00",
            "Time Left: 6h 12m",
            0.71,
            "/jfx/image/products/ironmans.png",
            "https://example.com/categories/collectibles"
        );
        setPaginationGroup(collectiblesPageButtons, 0);
    }

    @FXML
    private void toggleArtMenu(ActionEvent event) {
        toggleCategoryMenu(
            artButton,
            artSubmenu,
            ART,
            "Curated originals, studio tools, and gallery-ready collector lots.",
            "Signed Limited Edition Abstract Canvas",
            "Current Bid: $2,760.00",
            "Time Left: 4h 09m",
            0.52,
            "/jfx/image/products/arts.png",
            "https://example.com/categories/art"
        );
        setPaginationGroup(artPageButtons, 0);
    }

    @FXML
    private void toggleJewelryWatchesMenu(ActionEvent event) {
        toggleCategoryMenu(
            jewelryWatchesButton,
            jewelryWatchesSubmenu,
            JEWELRY_WATCHES,
            "Luxury timepieces, fine jewelry, and certified statement pieces.",
            "Omega Speedmaster Moonwatch Professional",
            "Current Bid: $5,480.00",
            "Time Left: 7h 21m",
            0.43,
            "/jfx/image/products/jewelry-watches.png",
            "https://example.com/categories/jewelry-watches"
        );
        setPaginationGroup(jewelryWatchesPageButtons, 0);
    }

    @FXML
    private void toggleSportingGoodsMenu(ActionEvent event) {
        toggleCategoryMenu(
            sportingGoodsButton,
            sportingGoodsSubmenu,
            SPORTING_GOODS,
            "Performance gear, signed equipment, and club-ready bundles.",
            "Tour-Issue TaylorMade Iron Set",
            "Current Bid: $1,320.00",
            "Time Left: 3h 47m",
            0.61,
            "/jfx/image/products/100zz.png",
            "https://example.com/categories/sporting-goods"
        );
        setPaginationGroup(sportingGoodsPageButtons, 0);
    }

    @FXML
    private void handleMacbooksSelection(ActionEvent event) {
        applySubCategorySelection(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            macbooksButton,
            "MacBooks",
            "High-end Apple laptops and workstation bidding.",
            "Apple MacBook Pro 16 M3 Max",
            "Current Bid: $3,450.00",
            "Time Left: 2h 45m",
            0.85,
            "/jfx/image/products/macbooks.png",
            "https://example.com/products/macbooks"
        );
        setPaginationGroup(electronicsPageButtons, 0);
    }

    @FXML
    private void handlePhonesSelection(ActionEvent event) {
        applySubCategorySelection(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            phonesButton,
            "Phones",
            "Flagship smartphones, foldables, and collector devices.",
            "iPhone 15 Pro Max 1TB Titanium",
            "Current Bid: $1,680.00",
            "Time Left: 1h 18m",
            0.64,
            "/jfx/image/products/phones.png",
            "https://example.com/products/phones"
        );
        setPaginationGroup(electronicsPageButtons, 1);
    }

    @FXML
    private void handleGamingSelection(ActionEvent event) {
        applySubCategorySelection(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            gamingButton,
            "Gaming",
            "Consoles, GPUs, and tournament-ready hardware lots.",
            "RTX 4090 Liquid Cooled Build",
            "Current Bid: $2,240.00",
            "Time Left: 3h 06m",
            0.58,
            "/jfx/image/products/gaming.png",
            "https://example.com/products/gaming"
        );
        setPaginationGroup(electronicsPageButtons, 2);
    }

    @FXML
    private void handleAudioSelection(ActionEvent event) {
        applySubCategorySelection(
            electronicsButton,
            electronicsSubmenu,
            ELECTRONICS,
            audioButton,
            "Audio",
            "Studio gear, audiophile headphones, and hi-fi bundles.",
            "Sony IER-Z1R Signature In-Ear Monitors",
            "Current Bid: $1,120.00",
            "Time Left: 5h 31m",
            0.39,
            "/jfx/image/products/audio.png",
            "https://example.com/products/audio"
        );
        setPaginationGroup(electronicsPageButtons, 3);
    }

    @FXML
    private void handleCardsSelection(ActionEvent event) {
        applySubCategorySelection(
            collectiblesButton,
            collectiblesSubmenu,
            COLLECTIBLES,
            cardsButton,
            "Cards",
            "Vintage trading cards, graded hits, and sealed hobby drops.",
            "PSA 10 Charizard Holo",
            "Current Bid: $4,250.00",
            "Time Left: 5h 02m",
            0.66,
            "/jfx/image/products/cards.png",
            "https://example.com/products/cards"
        );
        setPaginationGroup(collectiblesPageButtons, 0);
    }

    @FXML
    private void handleFiguresSelection(ActionEvent event) {
        applySubCategorySelection(
            collectiblesButton,
            collectiblesSubmenu,
            COLLECTIBLES,
            figuresButton,
            "Figures",
            "Designer vinyl, statues, and limited-run display pieces.",
            "Hot Toys Iron Man Mark XLVI Diecast",
            "Current Bid: $760.00",
            "Time Left: 2h 21m",
            0.48,
            "/jfx/image/products/figures.png",
            "https://example.com/products/figures"
        );
        setPaginationGroup(collectiblesPageButtons, 1);
    }

    @FXML
    private void handleStampsSelection(ActionEvent event) {
        applySubCategorySelection(
            artButton,
            artSubmenu,
            ART,
            stampsButton,
            "Stamps",
            "Historic postal rarities and museum-grade philatelic sets.",
            "Inverted Jenny Plate Block Replica Set",
            "Current Bid: $1,940.00",
            "Time Left: 8h 14m",
            0.34,
            "/jfx/image/products/stamps.png",
            "https://example.com/products/stamps"
        );
        setPaginationGroup(artPageButtons, 0);
    }

    @FXML
    private void handlePaintbrushesSelection(ActionEvent event) {
        applySubCategorySelection(
            artButton,
            artSubmenu,
            ART,
            paintbrushesButton,
            "Paintbrushes",
            "Handmade brush kits and atelier-grade tools for studio work.",
            "Sable Detail Brush Master Set",
            "Current Bid: $285.00",
            "Time Left: 1h 52m",
            0.57,
            "/jfx/image/products/paintbrushes.png",
            "https://example.com/products/paintbrushes"
        );
        setPaginationGroup(artPageButtons, 1);
    }

    @FXML
    private void handleClocksSelection(ActionEvent event) {
        applySubCategorySelection(
            jewelryWatchesButton,
            jewelryWatchesSubmenu,
            JEWELRY_WATCHES,
            clocksButton,
            "Clocks",
            "Statement desk clocks and mechanical collector pieces.",
            "Jaeger-LeCoultre Atmos Classique",
            "Current Bid: $3,180.00",
            "Time Left: 9h 03m",
            0.29,
            "/jfx/image/products/clocks.png",
            "https://example.com/products/clocks"
        );
        setPaginationGroup(jewelryWatchesPageButtons, 0);
    }

    @FXML
    private void handleEaringsSelection(ActionEvent event) {
        applySubCategorySelection(
            jewelryWatchesButton,
            jewelryWatchesSubmenu,
            JEWELRY_WATCHES,
            earingsButton,
            "Earrings",
            "Diamond studs, artisan drops, and luxury signature pairs.",
            "18K Diamond Halo Drop Earrings",
            "Current Bid: $2,140.00",
            "Time Left: 3h 11m",
            0.55,
            "/jfx/image/products/earrings.png",
            "https://example.com/products/earrings"
        );
        setPaginationGroup(jewelryWatchesPageButtons, 1);
    }

    @FXML
    private void handleRacketsSelection(ActionEvent event) {
        applySubCategorySelection(
            sportingGoodsButton,
            sportingGoodsSubmenu,
            SPORTING_GOODS,
            racketsButton,
            "Rackets",
            "Tournament-ready rackets and pro-stock frame bundles.",
            "Wilson Pro Staff RF97 Signature",
            "Current Bid: $410.00",
            "Time Left: 2h 08m",
            0.44,
            "/jfx/image/products/100zz.png",
            "https://example.com/products/rackets"
        );
        setPaginationGroup(sportingGoodsPageButtons, 0);
    }

    @FXML
    private void handlePickleballRacketsSelection(ActionEvent event) {
        applySubCategorySelection(
            sportingGoodsButton,
            sportingGoodsSubmenu,
            SPORTING_GOODS,
            pickleballRacketsButton,
            "Pickleball Rackets",
            "Carbon-face paddles and competition-ready starter sets.",
            "Selkirk Vanguard Power Air Epic",
            "Current Bid: $235.00",
            "Time Left: 1h 29m",
            0.73,
            "/jfx/image/products/pickleball-rackets.png",
            "https://example.com/products/pickleball-rackets"
        );
        setPaginationGroup(sportingGoodsPageButtons, 1);
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        openPageAt(currentPageIndex - 1);
    }

    @FXML
    private void handlePageOne(ActionEvent event) {
        openVisiblePageAtOffset(0);
    }

    @FXML
    private void handlePageTwo(ActionEvent event) {
        openVisiblePageAtOffset(1);
    }

    @FXML
    private void handlePageThree(ActionEvent event) {
        openVisiblePageAtOffset(2);
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        openPageAt(currentPageIndex + 1);
    }

    @FXML
    private void handleFeaturedImageClick(ActionEvent event) {
        if (currentProductLink == null || currentProductLink.isBlank()) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(currentProductLink));
                return;
            }
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Cannot open product link",
                "Could not open: " + currentProductLink
            );
            return;
        }

        showMessage(
            Alert.AlertType.INFORMATION,
            "Product link",
            currentProductLink
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            SceneManager.switchScene("/jfx/scene/login.fxml");
        } catch (Exception exception) {
            showMessage(
                Alert.AlertType.ERROR,
                "Logout failed",
                "Could not return to the login screen."
            );
        }
    }

    private void toggleCategoryMenu(
        Button categoryButton,
        VBox submenu,
        String categoryName,
        String subtitle,
        String featuredTitle,
        String price,
        String timeLeft,
        double progress,
        String imagePath,
        String productLink
    ) {
        boolean shouldShow = !submenu.isVisible();
        setActiveCategory(categoryButton);
        clearActiveSubSelection();
        hideAllCategoryMenus();
        setCategoryMenuVisible(categoryButton, submenu, categoryName, shouldShow);
        updateCatalog(categoryName, subtitle, featuredTitle, price, timeLeft, progress, imagePath, productLink);
        sidebarSubtitle.setText(categoryName);
    }

    private void applySubCategorySelection(
        Button categoryButton,
        VBox submenu,
        String categoryName,
        Button selectedSubButton,
        String title,
        String subtitle,
        String featuredTitle,
        String price,
        String timeLeft,
        double progress,
        String imagePath,
        String productLink
    ) {
        setActiveCategory(categoryButton);
        hideAllCategoryMenus();
        setCategoryMenuVisible(categoryButton, submenu, categoryName, true);
        setActiveSubSelection(selectedSubButton);
        updateCatalog(title, subtitle, featuredTitle, price, timeLeft, progress, imagePath, productLink);
        sidebarSubtitle.setText(categoryName + " / " + title);
    }

    private void updateCatalog(
        String title,
        String subtitle,
        String featuredTitle,
        String price,
        String timeLeft,
        double progress,
        String imagePath,
        String productLink
    ) {
        catalogTitle.setText(title);
        catalogSubtitle.setText(subtitle);
        featuredCardTitle.setText(featuredTitle);
        featuredPriceLabel.setText(price);
        featuredTimeLabel.setText(timeLeft);
        featuredProgressBar.setProgress(progress);
        featuredImageView.setImage(loadProductImage(imagePath));
        currentProductLink = productLink;
        featuredImageLink.setDisable(productLink == null || productLink.isBlank());
    }

    private Image loadProductImage(String imagePath) {
        String resolvedImagePath = imagePath == null || imagePath.isBlank() ? DEFAULT_PRODUCT_IMAGE : imagePath;

        InputStream imageStream = getClass().getResourceAsStream(resolvedImagePath);
        if (imageStream == null && !DEFAULT_PRODUCT_IMAGE.equals(resolvedImagePath)) {
            imageStream = getClass().getResourceAsStream(DEFAULT_PRODUCT_IMAGE);
        }
        if (imageStream == null) {
            throw new IllegalStateException("Missing default product image: " + DEFAULT_PRODUCT_IMAGE);
        }

        return new Image(imageStream);
    }

    private void setCategoryMenuVisible(
        Button categoryButton,
        VBox submenu,
        String categoryName,
        boolean visible
    ) {
        submenu.setVisible(visible);
        submenu.setManaged(visible);
        categoryButton.setText(categoryName + (visible ? " -" : " +"));
    }

    private void hideAllCategoryMenus() {
        setCategoryMenuVisible(electronicsButton, electronicsSubmenu, ELECTRONICS, false);
        setCategoryMenuVisible(collectiblesButton, collectiblesSubmenu, COLLECTIBLES, false);
        setCategoryMenuVisible(artButton, artSubmenu, ART, false);
        setCategoryMenuVisible(jewelryWatchesButton, jewelryWatchesSubmenu, JEWELRY_WATCHES, false);
        setCategoryMenuVisible(sportingGoodsButton, sportingGoodsSubmenu, SPORTING_GOODS, false);
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
        clearActiveSubSelection();
        if (!selectedSubButton.getStyleClass().contains("active-sub")) {
            selectedSubButton.getStyleClass().add("active-sub");
        }
    }

    private void clearActiveSubSelection() {
        for (Button button : subCategoryButtons) {
            button.getStyleClass().remove("active-sub");
        }
    }

    private void setPaginationGroup(List<Button> pageButtons, int selectedIndex) {
        currentPageButtons = pageButtons;
        currentPageIndex = selectedIndex;
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        if (currentPageButtons == null || currentPageButtons.isEmpty()) {
            paginationLabel.setText("Page 0 of 0");
            previousPageButton.setDisable(true);
            nextPageButton.setDisable(true);
            configurePageButton(pageOneButton, -1);
            configurePageButton(pageTwoButton, -1);
            configurePageButton(pageThreeButton, -1);
            return;
        }

        int totalPages = currentPageButtons.size();
        int startIndex = getVisiblePageStartIndex();

        paginationLabel.setText("Page " + (currentPageIndex + 1) + " of " + totalPages);
        previousPageButton.setDisable(currentPageIndex <= 0);
        nextPageButton.setDisable(currentPageIndex >= totalPages - 1);

        configurePageButton(pageOneButton, startIndex);
        configurePageButton(pageTwoButton, startIndex + 1);
        configurePageButton(pageThreeButton, startIndex + 2);
    }

    private int getVisiblePageStartIndex() {
        if (currentPageButtons.size() <= 3) {
            return 0;
        }

        return Math.max(0, Math.min(currentPageIndex - 1, currentPageButtons.size() - 3));
    }

    private void configurePageButton(Button button, int pageIndex) {
        if (pageIndex < 0 || currentPageButtons == null || pageIndex >= currentPageButtons.size()) {
            button.setVisible(false);
            button.setManaged(false);
            button.setDisable(true);
            return;
        }

        button.setVisible(true);
        button.setManaged(true);
        button.setDisable(pageIndex == currentPageIndex);
        button.setText(String.valueOf(pageIndex + 1));
    }

    private void openVisiblePageAtOffset(int offset) {
        openPageAt(getVisiblePageStartIndex() + offset);
    }

    private void openPageAt(int pageIndex) {
        if (currentPageButtons == null || pageIndex < 0 || pageIndex >= currentPageButtons.size()) {
            return;
        }

        currentPageButtons.get(pageIndex).fire();
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
