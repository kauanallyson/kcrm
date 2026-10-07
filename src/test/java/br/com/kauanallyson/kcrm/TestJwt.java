package br.com.kauanallyson.kcrm;

import java.security.SecureRandom;
import java.util.Base64;

public final class TestJwt {
    private static final SecureRandom RANDOM = new SecureRandom();

    private TestJwt() {
    }

    public static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
