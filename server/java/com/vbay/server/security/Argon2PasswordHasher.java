package com.vbay.server.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;


///argon2 không cam kết thread-safe tuyệt đối :VV
public class Argon2PasswordHasher implements PasswordHasher {
    // Config (có thể tune)
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // KB = 64MB
    private static final int PARALLELISM = 1;

    @Override
    public String hash (char[] rawPassword) {
        Argon2 argon2 = Argon2Factory.create();
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, rawPassword);
    }
    @Override
    public boolean matches (char[] rawPassword, String hash) {
        Argon2 argon2 = Argon2Factory.create(); 
        return argon2.verify (hash, rawPassword);
    }
    
}
