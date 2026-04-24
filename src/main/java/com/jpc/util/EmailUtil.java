package com.jpc.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class EmailUtil {

    private static final Properties FILE_PROPERTIES = loadFileProperties();
    private static final String SMTP_HOST = require("mail.smtp.host", "MAIL_SMTP_HOST");
    private static final String SMTP_PORT = getOptional("mail.smtp.port", "MAIL_SMTP_PORT", "587");
    private static final String SMTP_USERNAME = getOptional("mail.smtp.username", "MAIL_SMTP_USERNAME", "");
    private static final String SMTP_PASSWORD = getOptional("mail.smtp.password", "MAIL_SMTP_PASSWORD", "");
    private static final boolean SMTP_AUTH = Boolean.parseBoolean(
            getOptional("mail.smtp.auth", "MAIL_SMTP_AUTH", "true"));
    private static final boolean STARTTLS_ENABLED = Boolean.parseBoolean(
            getOptional("mail.smtp.starttls.enable", "MAIL_SMTP_STARTTLS", "true"));
    private static final boolean SSL_ENABLED = Boolean.parseBoolean(
            getOptional("mail.smtp.ssl.enable", "MAIL_SMTP_SSL", "false"));
    private static final String FROM_EMAIL = require("mail.from", "MAIL_FROM");
    private static final Session SESSION = buildSession();

    private EmailUtil() {
    }

    public static void sendEmail(String to, String subject, String body) {
        validate(to, subject, body);

        try {
            MimeMessage message = new MimeMessage(SESSION);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setText(body, StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Failed to send email to " + to, exception);
        }
    }

    private static Session buildSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", String.valueOf(SMTP_AUTH));
        properties.put("mail.smtp.starttls.enable", String.valueOf(STARTTLS_ENABLED));
        properties.put("mail.smtp.ssl.enable", String.valueOf(SSL_ENABLED));
        properties.put("mail.smtp.connectiontimeout", getOptional(
                "mail.smtp.connectiontimeout", "MAIL_SMTP_CONNECTION_TIMEOUT", "10000"));
        properties.put("mail.smtp.timeout", getOptional(
                "mail.smtp.timeout", "MAIL_SMTP_TIMEOUT", "10000"));
        properties.put("mail.smtp.writetimeout", getOptional(
                "mail.smtp.writetimeout", "MAIL_SMTP_WRITE_TIMEOUT", "10000"));

        if (!SMTP_AUTH) {
            return Session.getInstance(properties);
        }

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
    }

    private static Properties loadFileProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = EmailUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load application.properties", exception);
        }

        return properties;
    }

    private static String require(String propertyKey, String envKey) {
        String value = getOptional(propertyKey, envKey, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + propertyKey + " / " + envKey);
        }
        return value;
    }

    private static String getOptional(String propertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (isBlank(value)) {
            value = System.getenv(envKey);
        }
        if (isBlank(value)) {
            value = FILE_PROPERTIES.getProperty(propertyKey);
        }
        if (isBlank(value)) {
            value = defaultValue;
        }
        return value;
    }

    private static void validate(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email must not be blank.");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject must not be blank.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Email body must not be blank.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
