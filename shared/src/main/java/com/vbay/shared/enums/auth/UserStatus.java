package com.vbay.shared.enums.auth;

public enum UserStatus {
    ACTIVE, // hoat dong
    SUSPENDED, // bi tam ngung
    BANNED, // bi ban <(")
    DELETED // bi xoa (khong con ton tai trong database, chi de phan biet voi banned)
}

