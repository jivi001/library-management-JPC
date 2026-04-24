package com.jpc.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public final class TokenUtil {

    private static final int TOKEN_BYTES = 32;
    private static final long DEFAULT_EXPIRY_HOURS = 24L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenUtil() {
    }

    public static String generateVerificationToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte current : bytes) {
            builder.append(String.format("%02x", current));
        }
        return builder.toString();
    }

    public static LocalDateTime generateExpiry() {
        return LocalDateTime.now().plusHours(resolveExpiryHours());
    }

    private static long resolveExpiryHours() {
        String configured = System.getProperty("security.verification.expiry.hours");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("VERIFICATION_EXPIRY_HOURS");
        }
        if (configured == null || configured.isBlank()) {
            return DEFAULT_EXPIRY_HOURS;
        }

        try {
            long hours = Long.parseLong(configured);
            if (hours <= 0) {
                throw new IllegalStateException("Verification expiry must be positive.");
            }
            return hours;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid verification expiry hours: " + configured, exception);
        }
    }
}
