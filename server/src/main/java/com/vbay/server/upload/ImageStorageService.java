package com.vbay.server.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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
    private static final long MAX_IMAGE_BYTES = 2L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Path uploadDirectory;
    
    public ImageStorageService() {
        this(Paths.get("uploads", "products"));
    }

    public ImageStorageService(Path uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public String saveBase64Image(UploadImageRequest request) throws IOException {
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
            throw new ValidationException("Image file must be 2MB or smaller");
        }
        Files.createDirectories(uploadDirectory);
        String storedFileName = UUID.randomUUID() + extension;
        Path target = uploadDirectory.resolve(storedFileName).normalize();
        if (!target.startsWith(uploadDirectory.normalize())) {
            throw new ValidationException("Invalid image path");
        }

        Files.write(target, imageBytes);
        return "/uploads/products/" + storedFileName;
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
