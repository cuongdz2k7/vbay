package com.vbay.shared.protocol;

import com.vbay.shared.Utils.IDGenerator;

import com.vbay.shared.enums.MessageType;
import com.vbay.shared.enums.RequestType;


public class Request<T> {
    private MessageType messageType = MessageType.REQUEST;
    private RequestType requestType;
    private String requestId;
    private T payload; ///dữ liệu gửi kèm theo yêu cầu, có thể là thông tin đăng nhập, thông tin đấu giá, v.v.

    public Request(RequestType requestType, T payload) {
        this.requestType = requestType;
        this.requestId = IDGenerator.generateID();
        this.payload = payload;
    }    
    public RequestType getRequestType() {
        return requestType;
    }
    public void setRequestType(RequestType type) {
        this.requestType = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public T getPayload() {
        return payload;
    }

}
