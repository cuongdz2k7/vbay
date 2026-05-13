package com.vbay.shared.enums.payment;

public enum PaymentStatus {
    HELD,      // platform đang giữ tiền
    RELEASED,  // đã chuyển cho seller
    REFUNDED,  // đã hoàn buyer
    FAILED,
    CANCELLED

}

