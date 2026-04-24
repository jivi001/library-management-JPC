package com.jpc.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int DEFAULT_LOG_ROUNDS = 12;
    private static final int LOG_ROUNDS = resolveLogRounds();

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        validatePlainPassword(plainPassword);
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        validatePlainPassword(plainPassword);
        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static int resolveLogRounds() {
        String configured = System.getProperty("security.bcrypt.rounds");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("BCRYPT_LOG_ROUNDS");
        }
        if (configured == null || configured.isBlank()) {
            return DEFAULT_LOG_ROUNDS;
        }

        try {
            int rounds = Integer.parseInt(configured);
            if (rounds < 10 || rounds > 16) {
                throw new IllegalStateException("BCrypt log rounds must be between 10 and 16.");
            }
            return rounds;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid BCrypt log rounds: " + configured, exception);
        }
    }

    private static void validatePlainPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank.");
        }
    }
}
