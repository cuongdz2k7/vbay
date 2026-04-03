package com.vbay.server.security;

public interface PasswordHasher {
    public String hash(char[] rawPassword);
    public boolean matches (char[] rawPassword, String hash);
}
