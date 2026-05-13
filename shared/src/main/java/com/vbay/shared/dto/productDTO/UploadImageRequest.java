package com.vbay.shared.dto.productDTO;

public class UploadImageRequest {
    private String fileName;
    private String base64Data;

    public UploadImageRequest(String fileName, String base64Data) {
        this.fileName = fileName;
        this.base64Data = base64Data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBase64Data() {
        return base64Data;
    }
}
