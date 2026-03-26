package com.vbay.protocol;

import com.vbay.Utils.IDGenerator;
import com.vbay.shared.enums.RequestType;

public class Request<T> {
    private RequestType type;
    private String requestId;
    private T payload; ///dữ liệu gửi kèm theo yêu cầu, có thể là thông tin đăng nhập, thông tin đấu giá, v.v.

    public Request(RequestType type, T payload) {
        this.type = type;
        this.requestId = IDGenerator.generateID();
        this.payload = payload;
    }    
    public RequestType getType() {
        return type;
    }
    public void setType(RequestType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public T getPayload() {
        return payload;
    }

}
