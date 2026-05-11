package com.vbay.network.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.productDTO.UploadImageRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;

/*
max 2MB/file
đuôi cho phép: jpg, jpeg, png, gif, webp
*/

public class ImageUploadClient {
    private static final long MAX_IMAGE_BYTES = 2L * 1024L * 1024L;

    public String upload(File file) throws IOException {
        if (file.length() > MAX_IMAGE_BYTES) {
            throw new IOException("Image file must be 2MB or smaller.");
        }

        byte[] imageBytes = Files.readAllBytes(file.toPath());
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        UploadImageRequest uploadRequest = new UploadImageRequest(file.getName(), base64Data);

        Respond<?> response = SocketClient.getClient().sendMessage(new Request<>(RequestType.UPLOAD_IMAGE, uploadRequest));
        if (response == null) {
            throw new IOException("No response from server.");
        }
        if (!response.isStatus()) {
            throw new IOException(response.getMessage() != null ? response.getMessage() : "Image upload failed.");
        }
        Object data = response.getData();
        if (!(data instanceof String imageUrl) || imageUrl.isBlank()) {
            throw new IOException("Server did not return an image URL.");
        }
        return imageUrl;
    }
}
