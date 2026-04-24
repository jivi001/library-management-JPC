package com.jpc.dao;

import com.jpc.model.User;
import com.jpc.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDAO {

    private static final String INSERT_USER = """
            INSERT INTO users (
                full_name,
                email,
                password_hash,
                is_verified,
                email_verification_token,
                email_verification_expires_at,
                email_verified_at,
                last_login_at,
                is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_USER_BY_ID = """
            SELECT id, full_name, email, password_hash, is_verified, email_verification_token,
                   email_verification_expires_at, email_verified_at, last_login_at, is_active,
                   created_at, updated_at
            FROM users
            WHERE id = ?
            """;

    private static final String SELECT_USER_BY_EMAIL = """
            SELECT id, full_name, email, password_hash, is_verified, email_verification_token,
                   email_verification_expires_at, email_verified_at, last_login_at, is_active,
                   created_at, updated_at
            FROM users
            WHERE email = ?
            """;

    private static final String SELECT_VERIFIED_ACTIVE_USER_BY_EMAIL = """
            SELECT id, full_name, email, password_hash, is_verified, email_verification_token,
                   email_verification_expires_at, email_verified_at, last_login_at, is_active,
                   created_at, updated_at
            FROM users
            WHERE email = ? AND is_verified = 1 AND is_active = 1
            """;

    private static final String VERIFY_USER = """
            UPDATE users
            SET is_verified = 1,
                email_verified_at = CURRENT_TIMESTAMP,
                email_verification_token = NULL,
                email_verification_expires_at = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE email_verification_token = ?
              AND is_verified = 0
              AND is_active = 1
              AND email_verification_expires_at IS NOT NULL
              AND email_verification_expires_at >= CURRENT_TIMESTAMP
            """;

    private static final String UPDATE_LAST_LOGIN = """
            UPDATE users
            SET last_login_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    public User createUser(User user) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            bindCreateUser(statement, user);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Failed to create user: no rows inserted.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new DAOException("Failed to create user: no generated key returned.");
                }
                long userId = generatedKeys.getLong(1);
                return findById(userId)
                        .orElseThrow(() -> new DAOException("Failed to load newly created user with id " + userId));
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to create user.", exception);
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find user by email.", exception);
        }
    }

    public boolean verifyUser(String token) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(VERIFY_USER)) {
            statement.setString(1, token);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DAOException("Failed to verify user.", exception);
        }
    }

    public Optional<User> findVerifiedActiveUserByEmail(String email) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_VERIFIED_ACTIVE_USER_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find verified active user by email.", exception);
        }
    }

    public boolean updateLastLogin(long userId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_LOGIN)) {
            statement.setLong(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DAOException("Failed to update last login.", exception);
        }
    }

    private Optional<User> findById(long userId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find user by id.", exception);
        }
    }

    private void bindCreateUser(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getFullName());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getPasswordHash());
        statement.setBoolean(4, user.isVerified());
        statement.setString(5, user.getEmailVerificationToken());
        setTimestamp(statement, 6, user.getEmailVerificationExpiresAt());
        setTimestamp(statement, 7, user.getEmailVerifiedAt());
        setTimestamp(statement, 8, user.getLastLoginAt());
        statement.setBoolean(9, user.isActive());
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getBoolean("is_verified"),
                resultSet.getString("email_verification_token"),
                toLocalDateTime(resultSet.getTimestamp("email_verification_expires_at")),
                toLocalDateTime(resultSet.getTimestamp("email_verified_at")),
                toLocalDateTime(resultSet.getTimestamp("last_login_at")),
                resultSet.getBoolean("is_active"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("updated_at"))
        );
    }

    private void setTimestamp(PreparedStatement statement, int parameterIndex, LocalDateTime value) throws SQLException {
        if (value == null) {
            statement.setTimestamp(parameterIndex, null);
        } else {
            statement.setTimestamp(parameterIndex, Timestamp.valueOf(value));
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
