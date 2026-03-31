package com.vbay.server.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2PasswordHasher implements PasswordHasher {
    // Config (có thể tune)
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // KB = 64MB
    private static final int PARALLELISM = 1;

    private static final Argon2 argon2 = Argon2Factory.create();
    @Override
    public String hash (char[] rawPassword) {
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, rawPassword);
    }
    @Override
    public boolean matches (char[] rawPassword, String hash) { 
        return argon2.verify (hash, rawPassword);
        
    }
    
}
