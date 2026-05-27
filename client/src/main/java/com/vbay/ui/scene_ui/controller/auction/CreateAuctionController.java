package com.vbay.ui.scene_ui.controller.auction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.vbay.network.SocketClient;
import com.vbay.network.image.ImageUploadClient;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.NotificationManager;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.MoneyInput;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

/*
Bấm LAUNCH AUCTION
-> validate toàn bộ input client
-> nếu sai: báo lỗi, chưa upload ảnh, chưa gửi request
-> nếu đúng: upload ảnh lên server để lấy imageUrl
-> build CreateProductRequest
-> build CreateAuctionRequest
-> gửi RequestType.CREATE_AUCTION qua SocketClient
-> server xử lý và lưu DB

*/

public class CreateAuctionController {
    private static final int MAX_IMAGES = 4;
    private static final double PREVIEW_SIZE = 72;
    private static final String OPTIONAL_PRICE_ENABLED_CLASS = "optional-price-enabled";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PREVIEW_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm:ss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final String ACTIVE_TAB_STYLE_CLASS = "active-tab";

    private final ImageUploadClient imageUploadClient = new ImageUploadClient();
    private final List<File> selectedImageFiles = new ArrayList<>();
    private boolean reservePriceEnabled;
    private boolean buyNowPriceEnabled;
    private boolean quickAuctionMode = true;

    private Runnable onBack;
    private Runnable onAuctionCreated;

    @FXML
    private HBox imagePreviewContainer;
    @FXML
    private TextField productNameField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ToggleGroup conditionGroup;
    @FXML
    private TextArea technicalDescriptionArea;
    @FXML
    private TextArea listingNarrativeArea;
    @FXML
    private TextField auctionTitleField;
    @FXML
    private TextField startingPriceField;
    @FXML
    private TextField minimumBidStepField;
    @FXML
    private TextField reservePriceField;
    @FXML
    private StackPane reservePriceToggle;
    @FXML
    private Circle reservePriceToggleDot;
    @FXML
    private TextField buyNowPriceField;
    @FXML
    private StackPane buyNowPriceToggle;
    @FXML
    private Circle buyNowPriceToggleDot;
    @FXML
    private DatePicker startingDatePicker;
    @FXML
    private TextField startingHourField;
    @FXML
    private TextField startingMinuteField;
    @FXML
    private TextField startingSecondField;
    @FXML
    private Button quickAuctionTabButton;
    @FXML
    private Button standardAuctionTabButton;
    @FXML
    private ComboBox<String> durationPresetComboBox;
    @FXML
    private Label durationPreviewLabel;

    @FXML
    private void initialize() {
        configureDatePicker(startingDatePicker);
        configureTimeSegmentInputs();
        configureDurationControls();
        MoneyInput.install(startingPriceField);
        MoneyInput.install(minimumBidStepField);
        MoneyInput.install(reservePriceField);
        MoneyInput.install(buyNowPriceField);
        setReservePriceEnabled(false);
        setBuyNowPriceEnabled(false);
    }

    @FXML
    private void handleCancel() {
        try {
            SceneManager.switchScene("/jfx/scene/Home.fxml");
            NotificationManager.show(NotificationManager.NotificationType.INFO, "Cancellation", "The auction creation was cancelled.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnAuctionCreated(Runnable onAuctionCreated) {
        this.onAuctionCreated = onAuctionCreated;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Runnable backAction = onBack;
        dispose();
        if (backAction != null) {
            backAction.run();
            return;
        }

        try {
            SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Navigation Failed", "Could not return to the home scene.");
        }
    }

    @FXML
    private void handleLaunchAuction(ActionEvent event) {
        try {
            validateRequiredImages();
            AuctionFormInput formInput = collectFormInput();
            List<ProductImageDTO> uploadedImages = uploadSelectedImages();
            CreateAuctionRequest createAuctionRequest = buildCreateAuctionRequest(formInput, uploadedImages);
            Respond<?> response = SocketClient.getClient().sendMessage(
                new Request<>(RequestType.CREATE_AUCTION, createAuctionRequest)
            );


            if (response == null || !response.isStatus()) {
                NotificationManager.show(NotificationManager.NotificationType.ERROR, "Create Auction Failed", response.getMessage());
                return;
            }

            NotificationManager.show(
                NotificationManager.NotificationType.INFO,
                "Auction Created",
                "The auction was created successfully."
            );
            Runnable auctionCreatedAction = onAuctionCreated;
            dispose();
            if (auctionCreatedAction != null) {
                auctionCreatedAction.run();
            }
        } catch (IllegalArgumentException exception) {
            NotificationManager.show(NotificationManager.NotificationType.WARNING, "Invalid Auction Data", exception.getMessage());
        } catch (IOException exception) {
            NotificationManager.show(NotificationManager.NotificationType.ERROR, "Create Auction Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleUploadAsset() {
        if (selectedImageFiles.size() >= MAX_IMAGES) {
            NotificationManager.show(NotificationManager.NotificationType.INFO, "Image Limit Reached", "You can select up to 4 images.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select product images");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Window owner = SceneManager.getStage();
        List<File> files = fileChooser.showOpenMultipleDialog(owner);
        if (files == null || files.isEmpty()) {
            return;
        }

        int remainingSlots = MAX_IMAGES - selectedImageFiles.size();
        if (files.size() > remainingSlots) {
            NotificationManager.show(NotificationManager.NotificationType.INFO, "Image Limit", "Only the first " + remainingSlots + " image(s) were added.");
        }

        files.stream()
            .limit(remainingSlots)
            .forEach(selectedImageFiles::add);
        renderSelectedImages();
    }

    @FXML
    private void handleReservePriceToggle() {
        setReservePriceEnabled(!reservePriceEnabled);
    }

    @FXML
    private void handleBuyNowPriceToggle() {
        setBuyNowPriceEnabled(!buyNowPriceEnabled);
    }

    @FXML
    private void handleQuickAuctionMode(ActionEvent event) {
        if (!quickAuctionMode) {
            quickAuctionMode = true;
            updateDurationModeStyles();
            populateDurationOptions();
            updateDurationPreview();
        }
    }

    @FXML
    private void handleStandardAuctionMode(ActionEvent event) {
        if (quickAuctionMode) {
            quickAuctionMode = false;
            updateDurationModeStyles();
            populateDurationOptions();
            updateDurationPreview();
        }
    }

    private void renderSelectedImages() {
        imagePreviewContainer.getChildren().clear();
        for (int i = 0; i < selectedImageFiles.size(); i++) {
            imagePreviewContainer.getChildren().add(createPreviewCell(selectedImageFiles.get(i), i));
        }
    }

    private StackPane createPreviewCell(File imageFile, int imageIndex) {
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(PREVIEW_SIZE);
        imageView.setFitHeight(PREVIEW_SIZE);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        applyCoverViewport(imageView, image, PREVIEW_SIZE, PREVIEW_SIZE);

        Rectangle clip = new Rectangle(PREVIEW_SIZE, PREVIEW_SIZE);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        imageView.setClip(clip);

        Button removeButton = new Button("x");
        removeButton.setFocusTraversable(false);
        removeButton.getStyleClass().add("remove-image-button");
        removeButton.setOnAction(event -> {
            selectedImageFiles.remove(imageIndex);
            renderSelectedImages();
        });

        StackPane previewCell = new StackPane(imageView);
        previewCell.getStyleClass().add("media-thumb");
        previewCell.setMinSize(PREVIEW_SIZE, PREVIEW_SIZE);
        previewCell.setPrefSize(PREVIEW_SIZE, PREVIEW_SIZE);
        previewCell.setMaxSize(PREVIEW_SIZE, PREVIEW_SIZE);
        if (imageIndex == 0) {
            Label thumbnailBadge = new Label("THUMBNAIL");
            thumbnailBadge.getStyleClass().add("thumbnail-badge");
            previewCell.getChildren().add(thumbnailBadge);
            StackPane.setAlignment(thumbnailBadge, Pos.BOTTOM_CENTER);
        }

        previewCell.getChildren().add(removeButton);
        StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
        return previewCell;
    }

    private void applyCoverViewport(ImageView imageView, Image image, double targetWidth, double targetHeight) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        double targetRatio = targetWidth / targetHeight;
        double imageRatio = imageWidth / imageHeight;
        double viewportWidth = imageWidth;
        double viewportHeight = imageHeight;
        double viewportX = 0;
        double viewportY = 0;

        if (imageRatio > targetRatio) {
            viewportWidth = imageHeight * targetRatio;
            viewportX = (imageWidth - viewportWidth) / 2;
        } else {
            viewportHeight = imageWidth / targetRatio;
            viewportY = (imageHeight - viewportHeight) / 2;
        }

        imageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
    }

    private List<ProductImageDTO> uploadSelectedImages() throws IOException {
        List<ProductImageDTO> uploadedImages = new ArrayList<>();
        for (int i = 0; i < selectedImageFiles.size(); i++) {
            String imageUrl = imageUploadClient.upload(selectedImageFiles.get(i));
            uploadedImages.add(new ProductImageDTO(imageUrl, i == 0));
        }
        return uploadedImages;
    }

    private AuctionFormInput collectFormInput() {
        String productName = requireText(productNameField, "Product name is required.");
        String condition = requireCondition();
        String technicalDescription = requireText(technicalDescriptionArea, "Technical description is required.");
        String auctionTitle = requireText(auctionTitleField, "Auction title is required.");
        String listingNarrative = requireText(listingNarrativeArea, "Listing narrative is required.");

        BigDecimal startingPrice = requireMoney(startingPriceField, "Starting price");
        BigDecimal minimumBidStep = requireMoney(minimumBidStepField, "Bid step");
        BigDecimal reservePrice = optionalReserveMoney();
        BigDecimal buyNowPrice = optionalBuyNowMoney();
        LocalDateTime startingTime = requireStartingDateTime();
        LocalDateTime endingTime = startingTime.plus(requireDuration());

        validateAuctionBusinessRules(startingPrice, reservePrice, buyNowPrice, startingTime, endingTime);

        return new AuctionFormInput(
            productName,
            condition,
            technicalDescription,
            auctionTitle,
            listingNarrative,
            startingPrice,
            reservePrice,
            buyNowPrice,
            minimumBidStep,
            startingTime,
            endingTime,
            resolveCategoryId(categoryComboBox.getValue())
        );
    }

    private void validateAuctionBusinessRules(
            BigDecimal startingPrice,
            BigDecimal reservePrice,
            BigDecimal buyNowPrice,
            LocalDateTime startingTime,
            LocalDateTime endingTime) {
        if (reservePrice != null && reservePrice.compareTo(startingPrice) < 0) {
            throw new IllegalArgumentException("Reserve price must be greater than or equal to starting price.");
        }

        if (reservePrice != null && buyNowPrice != null && buyNowPrice.compareTo(reservePrice) < 0) {
            throw new IllegalArgumentException("Buy now price must be greater than or equal to reserve price.");
        }

        if (!startingTime.isBefore(endingTime)) {
            throw new IllegalArgumentException("Starting date/time must be before ending date/time.");
        }
    }

    private CreateAuctionRequest buildCreateAuctionRequest(AuctionFormInput formInput, List<ProductImageDTO> uploadedImages) {
        CreateProductRequest productRequest = new CreateProductRequest(
            formInput.productName(),
            uploadedImages,
            formInput.technicalDescription(),
            formInput.condition(),
            formInput.categoryId()
        );

        return new CreateAuctionRequest(
            productRequest,
            formInput.auctionTitle(),
            formInput.listingNarrative(),
            formInput.startingPrice(),
            formInput.reservePrice(),
            formInput.buyNowPrice(),
            formInput.minimumBidStep(),
            vietnamTimeToUtc(formInput.startingTime()),
            vietnamTimeToUtc(formInput.endingTime())
        );
    }

    private LocalDateTime vietnamTimeToUtc(LocalDateTime vietnamTime) {
        return vietnamTime
            .atZone(VIETNAM_ZONE)
            .withZoneSameInstant(UTC_ZONE)
            .toLocalDateTime();
    }

    private void validateRequiredImages() {
        if (selectedImageFiles.isEmpty()) {
            throw new IllegalArgumentException("Select at least one product image.");
        }
    }

    public void dispose() {
        selectedImageFiles.clear();
        if (imagePreviewContainer != null) {
            imagePreviewContainer.getChildren().clear();
        }
        onBack = null;
        onAuctionCreated = null;
    }

    private void setReservePriceEnabled(boolean enabled) {
        reservePriceEnabled = enabled;
        reservePriceField.setDisable(!enabled);
        reservePriceField.setEditable(enabled);
        setStyleClassActive(reservePriceField, OPTIONAL_PRICE_ENABLED_CLASS, enabled);
        if (!enabled) {
            reservePriceField.clear();
        }

        if (reservePriceToggle != null) {
            reservePriceToggle.getStyleClass().remove("switch-track-active");
            if (enabled) {
                reservePriceToggle.getStyleClass().add("switch-track-active");
            }
        }

        if (reservePriceToggleDot != null) {
            reservePriceToggleDot.setTranslateX(enabled ? 9 : -9);
        }
    }

    private void setBuyNowPriceEnabled(boolean enabled) {
        buyNowPriceEnabled = enabled;
        buyNowPriceField.setDisable(!enabled);
        buyNowPriceField.setEditable(enabled);
        setStyleClassActive(buyNowPriceField, OPTIONAL_PRICE_ENABLED_CLASS, enabled);
        if (!enabled) {
            buyNowPriceField.clear();
        }

        if (buyNowPriceToggle != null) {
            buyNowPriceToggle.getStyleClass().remove("switch-track-active");
            if (enabled) {
                buyNowPriceToggle.getStyleClass().add("switch-track-active");
            }
        }

        if (buyNowPriceToggleDot != null) {
            buyNowPriceToggleDot.setTranslateX(enabled ? 9 : -9);
        }
    }

    private void setStyleClassActive(TextField field, String styleClass, boolean active) {
        field.getStyleClass().remove(styleClass);
        if (active) {
            field.getStyleClass().add(styleClass);
        }
    }

    private String requireText(TextField textField, String message) {
        String value = textField.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String requireText(TextArea textArea, String message) {
        String value = textArea.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String requireCondition() {
        Toggle selectedToggle = conditionGroup.getSelectedToggle();
        if (selectedToggle == null || selectedToggle.getUserData() == null) {
            throw new IllegalArgumentException("Condition state is required.");
        }
        return selectedToggle.getUserData().toString();
    }

    private BigDecimal requireMoney(TextField textField, String fieldName) {
        String value = textField.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        BigDecimal money = MoneyInput.parse(value, fieldName);
        if (money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        textField.setText(money.toPlainString());
        return money;
    }

    private BigDecimal optionalMoney(TextField textField, String fieldName) {
        String value = textField.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        BigDecimal money = MoneyInput.parse(value, fieldName);
        if (money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        textField.setText(money.toPlainString());
        return money;
    }

    private BigDecimal optionalReserveMoney() {
        if (!reservePriceEnabled) {
            return null;
        }
        return optionalMoney(reservePriceField, "Reserve price");
    }

    private BigDecimal optionalBuyNowMoney() {
        if (!buyNowPriceEnabled) {
            return null;
        }
        return optionalMoney(buyNowPriceField, "Buy now price");
    }

    private LocalDateTime requireStartingDateTime() {
        LocalDate date = parseDate(startingDatePicker, "Starting date/time");
        LocalTime time = parseClockTime(
            startingHourField,
            startingMinuteField,
            startingSecondField,
            "Starting time"
        );
        return LocalDateTime.of(date, time);
    }

    private LocalDate parseDate(DatePicker datePicker, String fieldName) {
        String dateText = datePicker.getEditor().getText();
        if (dateText == null || dateText.isBlank()) {
            throw new IllegalArgumentException(fieldName + " date is required in MM/dd/yyyy format.");
        }

        try {
            return LocalDate.parse(dateText.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " date must use MM/dd/yyyy format.");
        }
    }

    private LocalTime parseClockTime(TextField hourField, TextField minuteField, TextField secondField, String fieldName) {
        int hour = requireTimePart(hourField, fieldName + " hour", 0, 23);
        int minute = requireTimePart(minuteField, fieldName + " minute", 0, 59);
        int second = requireTimePart(secondField, fieldName + " second", 0, 59);
        return LocalTime.of(hour, minute, second);
    }

    private Duration requireDuration() {
        Duration duration = durationForPreset(durationPresetComboBox.getValue());
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be greater than 0.");
        }
        return duration;
    }

    private int requireTimePart(TextField field, String fieldName, int min, int max) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            int parsedValue = Integer.parseInt(value.trim());
            if (parsedValue < min || parsedValue > max) {
                throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + ".");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be numeric.");
        }
    }

    private void configureTimeSegmentInputs() {
        configureNumericSegment(startingHourField, 2);
        configureNumericSegment(startingMinuteField, 2);
        configureNumericSegment(startingSecondField, 2);
    }

    private void configureNumericSegment(TextField field, int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d{0," + maxLength + "}") ? change : null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private void configureDurationControls() {
        durationPresetComboBox.setOnAction(event -> updateDurationPreview());
        startingDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateDurationPreview());
        startingDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> updateDurationPreview());
        startingHourField.textProperty().addListener((observable, oldValue, newValue) -> updateDurationPreview());
        startingMinuteField.textProperty().addListener((observable, oldValue, newValue) -> updateDurationPreview());
        startingSecondField.textProperty().addListener((observable, oldValue, newValue) -> updateDurationPreview());
        updateDurationModeStyles();
        populateDurationOptions();
        updateDurationPreview();
    }

    private void populateDurationOptions() {
        if (isQuickAuctionMode()) {
            durationPresetComboBox.getItems().setAll(
                "30 seconds",
                "1 minute",
                "3 minutes",
                "5 minutes",
                "15 minutes",
                "30 minutes",
                "1 hour"
            );
            durationPresetComboBox.setPromptText("Choose fast duration");
            durationPresetComboBox.setValue("5 minutes");
            return;
        }
        durationPresetComboBox.getItems().setAll(
            "1 day",
            "3 days",
            "5 days",
            "7 days",
            "14 days",
            "30 days"
        );
        durationPresetComboBox.setPromptText("Choose standard duration");
        durationPresetComboBox.setValue("7 days");
    }

    private boolean isQuickAuctionMode() {
        return quickAuctionMode;
    }

    private void updateDurationModeStyles() {
        setActiveDurationTab(quickAuctionTabButton, isQuickAuctionMode());
        setActiveDurationTab(standardAuctionTabButton, !isQuickAuctionMode());
    }

    private void setActiveDurationTab(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove(ACTIVE_TAB_STYLE_CLASS);
        if (active) {
            button.getStyleClass().add(ACTIVE_TAB_STYLE_CLASS);
        }
    }

    private Duration durationForPreset(String preset) {
        if (preset == null || preset.isBlank()) {
            throw new IllegalArgumentException("Duration is required.");
        }
        return switch (preset) {
            case "30 seconds" -> Duration.ofSeconds(30);
            case "1 minute" -> Duration.ofMinutes(1);
            case "3 minutes" -> Duration.ofMinutes(3);
            case "5 minutes" -> Duration.ofMinutes(5);
            case "15 minutes" -> Duration.ofMinutes(15);
            case "30 minutes" -> Duration.ofMinutes(30);
            case "1 hour" -> Duration.ofHours(1);
            case "1 day" -> Duration.ofDays(1);
            case "3 days" -> Duration.ofDays(3);
            case "5 days" -> Duration.ofDays(5);
            case "7 days" -> Duration.ofDays(7);
            case "14 days" -> Duration.ofDays(14);
            case "30 days" -> Duration.ofDays(30);
            default -> throw new IllegalArgumentException("Unsupported duration: " + preset);
        };
    }

    private void updateDurationPreview() {
        durationPreviewLabel.getStyleClass().removeAll("duration-preview-quick", "duration-preview-standard");
        try {
            LocalDateTime endingTime = requireStartingDateTime().plus(requireDuration());
            if (isQuickAuctionMode()) {
                durationPreviewLabel.setText("Ends quickly at: " + PREVIEW_FORMATTER.format(endingTime));
                durationPreviewLabel.getStyleClass().add("duration-preview-quick");
            } else {
                durationPreviewLabel.setText("Auction ends at: " + PREVIEW_FORMATTER.format(endingTime));
                durationPreviewLabel.getStyleClass().add("duration-preview-standard");
            }
        } catch (IllegalArgumentException exception) {
            durationPreviewLabel.setText("Set start time and duration to preview ending time.");
            durationPreviewLabel.getStyleClass().add("duration-preview-standard");
        }
    }

    private long resolveCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        return switch (categoryName) {
            case "Electronics" -> 1L;
            case "Collectibles" -> 2L;
            case "Arts" -> 3L;
            case "Jewelry & Watches" -> 4L;
            default -> throw new IllegalArgumentException("Unsupported product category: " + categoryName);
        };
    }

    private void configureDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE_FORMATTER.format(date);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }
                return LocalDate.parse(value.trim(), DATE_FORMATTER);
            }
        });
    }


    private record AuctionFormInput(
        String productName,
        String condition,
        String technicalDescription,
        String auctionTitle,
        String listingNarrative,
        BigDecimal startingPrice,
        BigDecimal reservePrice,
        BigDecimal buyNowPrice,
        BigDecimal minimumBidStep,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        long categoryId
    ) { }
}
