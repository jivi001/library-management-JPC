package com.jpc.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationUtil {

    public static final int MIN_PASSWORD_LENGTH = 12;
    public static final int MAX_PASSWORD_LENGTH = 72;
    public static final int MAX_NOTES_LENGTH = 500;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Fa-f0-9]{64}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^[0-9Xx-]{10,20}$");

    private ValidationUtil() {
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeOptional(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    public static String normalizeEmail(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= 255 && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidVerificationToken(String token) {
        return token != null && TOKEN_PATTERN.matcher(token).matches();
    }

    public static String validateSignup(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.length() < 2 || fullName.length() > 120) {
            return "Full name must be between 2 and 120 characters.";
        }
        if (!isValidEmail(email)) {
            return "Please provide a valid email address.";
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return "Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Password and confirm password must match.";
        }
        return null;
    }

    public static String validateLogin(String email, String password) {
        if (!isValidEmail(email)) {
            return "Please provide a valid email address.";
        }
        if (password == null || password.isBlank() || password.length() > MAX_PASSWORD_LENGTH) {
            return "Password is required.";
        }
        return null;
    }

    public static String validateBookInput(String isbn, String title, String author, String publisher, String category,
                                           String shelfLocation, Integer publishedYear, Integer totalCopies) {
        if (isbn != null && (!ISBN_PATTERN.matcher(isbn).matches() || isbn.length() > 20)) {
            return "ISBN must contain only digits, hyphens, or X and be between 10 and 20 characters.";
        }
        if (title == null || title.isBlank() || title.length() > 255) {
            return "Title is required and must not exceed 255 characters.";
        }
        if (author == null || author.isBlank() || author.length() > 255) {
            return "Author is required and must not exceed 255 characters.";
        }
        if (publisher != null && publisher.length() > 255) {
            return "Publisher must not exceed 255 characters.";
        }
        if (category != null && category.length() > 100) {
            return "Category must not exceed 100 characters.";
        }
        if (shelfLocation != null && shelfLocation.length() > 50) {
            return "Shelf location must not exceed 50 characters.";
        }
        if (publishedYear != null) {
            int currentYear = Year.now().getValue();
            if (publishedYear < 1000 || publishedYear > currentYear + 1) {
                return "Published year is out of range.";
            }
        }
        if (totalCopies == null || totalCopies <= 0) {
            return "Total copies must be a positive whole number.";
        }
        return null;
    }

    public static String validateIssueRequest(Long bookId, LocalDateTime dueAt, String notes) {
        if (bookId == null) {
            return "Book id must be a positive number.";
        }
        if (dueAt == null) {
            return "Due date is required and must use yyyy-MM-dd format.";
        }
        if (!dueAt.isAfter(LocalDateTime.now())) {
            return "Due date must be in the future.";
        }
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            return "Notes must not exceed 500 characters.";
        }
        return null;
    }

    public static String validateReturnRequest(Long transactionId, BigDecimal fineAmount, String notes) {
        if (transactionId == null) {
            return "Transaction id must be a positive number.";
        }
        if (fineAmount == null || fineAmount.compareTo(BigDecimal.ZERO) < 0) {
            return "Fine amount must be a non-negative number.";
        }
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            return "Notes must not exceed 500 characters.";
        }
        return null;
    }

    public static Integer parseNullableYear(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }

    public static LocalDateTime parseDueDateAtEndOfDay(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(normalized).atTime(23, 59, 59);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    public static BigDecimal parseNonNegativeAmount(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal parsed = new BigDecimal(normalized);
            return parsed.scale() > 2 ? null : parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
