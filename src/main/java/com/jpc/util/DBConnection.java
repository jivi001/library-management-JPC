package com.jpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class DBConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Properties FILE_PROPERTIES = loadFileProperties();

    private static final String DB_URL = require("db.url", "DB_URL");
    private static final String DB_USERNAME = require("db.username", "DB_USERNAME");
    private static final String DB_PASSWORD = require("db.password", "DB_PASSWORD");
    private static final String DB_DRIVER = getOptional("db.driver", "DB_DRIVER", DEFAULT_DRIVER);

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to load JDBC driver: " + DB_DRIVER, exception);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    private static Properties loadFileProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
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

        return Objects.requireNonNullElse(value, defaultValue);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
