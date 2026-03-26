package com.vbay.shared.protocol;

public class Respond<T> {
    String requestId; // ID của yêu cầu mà phản hồi này trả về
    boolean status; 
    String message; // Thông điệp mô tả kết quả của phản hồi
    T data; 
    
    public Respond(String requestId, boolean status, String message, T data) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public String getRequestId() {
        return requestId;
    }

    public boolean isStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public T getData() {
        return data;
    }

}
