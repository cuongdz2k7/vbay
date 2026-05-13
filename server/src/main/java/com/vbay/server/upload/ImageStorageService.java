package com.vbay.server.upload;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.vbay.server.exception.ValidationException;
import com.vbay.shared.dto.productDTO.UploadImageRequest;

/*
nhận UploadImageRequest
-> kiểm tra base64 có rỗng không
-> kiểm tra đuôi file hợp lệ
-> decode Base64 thành byte[]
-> kiểm tra ảnh <= 2MB
-> tạo tên file UUID
-> lưu vào uploads/products
-> trả về imageUrl


Client có imageUrl từ DB: /uploads/products/uuid.png
-> gửi GET_IMAGE(imageUrl) qua socket
-> server đọc file uploads/products/uuid.png
-> server encode Base64
-> trả Base64 về client
-> client decode Base64 thành byte[]
-> JavaFX tạo Image từ ByteArrayInputStream
-> set vào ImageView


*/
public class ImageStorageService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    private static final int LIST_THUMBNAIL_MAX_WIDTH = 640;
    private static final int LIST_THUMBNAIL_MAX_HEIGHT = 400;
    private static final String THUMBNAIL_FORMAT = "png";
    private static final String UPLOAD_URL_PREFIX = "/uploads/products/";
    private static final String THUMBNAIL_URL_PREFIX = "/uploads/products/thumbnails/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Path uploadDirectory;
    private final Path thumbnailDirectory;
    private final String publicBaseUrl;
    
    public ImageStorageService() {
        this(Paths.get("uploads", "products"), "http://localhost:8080");
    }

    public ImageStorageService(Path uploadDirectory) {
        this(uploadDirectory, "http://localhost:8080");
    }

    public ImageStorageService(Path uploadDirectory, String publicBaseUrl) {
        this.uploadDirectory = uploadDirectory;
        this.thumbnailDirectory = uploadDirectory.resolve("thumbnails");
        this.publicBaseUrl = publicBaseUrl;
    }

    public String storeUploadedImage(UploadImageRequest request) throws IOException {
        if (request == null || request.getBase64Data() == null || request.getBase64Data().isBlank()) {
            throw new ValidationException("Image data is required");
        }

        String extension = extensionOf(request.getFileName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Unsupported image type");
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(request.getBase64Data());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Invalid image data");
        }

        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw new ValidationException("Image file must be 10MB or smaller");
        }
        Files.createDirectories(uploadDirectory);
        String storedFileName = UUID.randomUUID() + extension;
        Path target = uploadDirectory.resolve(storedFileName).normalize();
        if (!target.startsWith(uploadDirectory.normalize())) {
            throw new ValidationException("Invalid image path");
        }

        Files.write(target, imageBytes);
        createThumbnailFile(target, thumbnailPathFor(storedFileName));

        return UPLOAD_URL_PREFIX + storedFileName;
    }

    public String toPublicThumbnailUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        String fileName = fileNameOf(imageUrl);
        if (fileName == null) {
            return toPublicImageUrl(imageUrl);
        }

        Path thumbnailPath = thumbnailPathFor(fileName);
        if (!Files.exists(thumbnailPath)) {
            Path imagePath = resolveStoredImagePath(imageUrl);
            if (imagePath != null) {
                try {
                    createThumbnailFile(imagePath, thumbnailPath);
                } catch (IOException exception) {
                    return toPublicImageUrl(imageUrl);
                }
            }
        }

        if (!Files.exists(thumbnailPath)) {
            return toPublicImageUrl(imageUrl);
        }
        return publicBaseUrl + THUMBNAIL_URL_PREFIX + fileNameWithoutExtension(fileName) + "." + THUMBNAIL_FORMAT;
    }

    public String toPublicImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        String normalizedPath = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return publicBaseUrl + normalizedPath;
    }

    private void createThumbnailFile(Path sourcePath, Path thumbnailPath) throws IOException {
        BufferedImage sourceImage = ImageIO.read(sourcePath.toFile());
        if (sourceImage == null) {
            return;
        }

        Files.createDirectories(thumbnailDirectory);
        BufferedImage thumbnail = resizeToFit(sourceImage, LIST_THUMBNAIL_MAX_WIDTH, LIST_THUMBNAIL_MAX_HEIGHT);
        ImageIO.write(thumbnail, THUMBNAIL_FORMAT, thumbnailPath.toFile());
    }

    private BufferedImage resizeToFit(BufferedImage sourceImage, int maxWidth, int maxHeight) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();
        if (sourceWidth <= maxWidth && sourceHeight <= maxHeight) {
            return sourceImage;
        }

        double scale = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = thumbnail.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return thumbnail;
    }

    private Path resolveStoredImagePath(String imageUrl) {
        String normalizedPath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;

        Path cwdPath = Paths.get(normalizedPath);
        if (Files.exists(cwdPath)) {
            return cwdPath;
        }

        Path serverPath = Paths.get("server").resolve(normalizedPath);
        if (Files.exists(serverPath)) {
            return serverPath;
        }

        return null;
    }

    private Path thumbnailPathFor(String fileName) {
        return thumbnailDirectory.resolve(fileNameWithoutExtension(fileName) + "." + THUMBNAIL_FORMAT).normalize();
    }

    private String fileNameOf(String imageUrl) {
        int slashIndex = imageUrl.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? imageUrl.substring(slashIndex + 1) : imageUrl;
        return fileName.isBlank() || fileName.contains("..") ? null : fileName;
    }

    private String fileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.US);
    }
}
