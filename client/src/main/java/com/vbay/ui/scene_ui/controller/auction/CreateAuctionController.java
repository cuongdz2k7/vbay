package com.vbay.ui.scene_ui.controller.auction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import com.vbay.network.SocketClient;
import com.vbay.network.image.ImageUploadClient;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.ui.scene_ui.SceneManager;
import com.vbay.ui.util.MoneyInput;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ImageUploadClient imageUploadClient = new ImageUploadClient();
    private final List<File> selectedImageFiles = new ArrayList<>();

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
    private TextField buyNowPriceField;
    @FXML
    private DatePicker startingDatePicker;
    @FXML
    private DatePicker endingDatePicker;
    @FXML
    private TextField startingTimeField;
    @FXML
    private TextField endingTimeField;

    @FXML
    private void initialize() {
        configureDatePicker(startingDatePicker);
        configureDatePicker(endingDatePicker);
        MoneyInput.install(startingPriceField);
        MoneyInput.install(minimumBidStepField);
        MoneyInput.install(reservePriceField);
        MoneyInput.install(buyNowPriceField);
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnAuctionCreated(Runnable onAuctionCreated) {
        this.onAuctionCreated = onAuctionCreated;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (onBack != null) {
            onBack.run();
            return;
        }

        try {
            SceneManager.switchScene("/jfx/scene/Home.fxml");
        } catch (Exception exception) {
            showMessage(Alert.AlertType.ERROR, "Navigation failed", "Could not return to the home scene.");
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

            if (!response.isStatus()) {
                showMessage(Alert.AlertType.ERROR, "Create auction failed", response.getMessage());
                return;
            }

            showMessage(
                Alert.AlertType.INFORMATION,
                "Auction created",
                "Auction created successfully."
            );
            if (onAuctionCreated != null) {
                onAuctionCreated.run();
            }
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, "Invalid auction data", exception.getMessage());
        } catch (IOException exception) {
            showMessage(Alert.AlertType.ERROR, "Create auction failed", exception.getMessage());
        }
    }

    @FXML
    private void handleUploadAsset() {
        if (selectedImageFiles.size() >= MAX_IMAGES) {
            showMessage(Alert.AlertType.INFORMATION, "Image limit reached", "You can select up to 4 images.");
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
            showMessage(Alert.AlertType.INFORMATION, "Image limit", "Only the first " + remainingSlots + " image(s) were added.");
        }

        files.stream()
            .limit(remainingSlots)
            .forEach(selectedImageFiles::add);
        renderSelectedImages();
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

        imageView.setViewport(new javafx.geometry.Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
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
        BigDecimal reservePrice = optionalMoney(reservePriceField, "Reserve price");
        BigDecimal buyNowPrice = optionalMoney(buyNowPriceField, "Buy now price");
        LocalDateTime startingTime = requireDateTime(startingDatePicker, startingTimeField, "Starting date/time");
        LocalDateTime endingTime = requireDateTime(endingDatePicker, endingTimeField, "Ending date/time");

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
            formInput.startingTime(),
            formInput.endingTime()
        );
    }

    private void validateRequiredImages() {
        if (selectedImageFiles.isEmpty()) {
            throw new IllegalArgumentException("Select at least one product image.");
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

    private LocalDateTime requireDateTime(DatePicker datePicker, TextField timeField, String fieldName) {
        LocalDate date = parseDate(datePicker, fieldName);
        LocalTime time = parseTime(timeField, fieldName);
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

    private LocalTime parseTime(TextField timeField, String fieldName) {
        String timeText = timeField.getText();
        if (timeText == null || timeText.isBlank()) {
            throw new IllegalArgumentException(fieldName + " time is required in HH:mm:ss format.");
        }

        try {
            return LocalTime.parse(timeText.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " time must use HH:mm:ss format.");
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

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("VBay");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
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
