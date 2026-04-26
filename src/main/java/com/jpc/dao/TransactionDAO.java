package com.jpc.dao;

import com.jpc.model.Transaction;
import com.jpc.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAO {

    private static final String LOCK_ACTIVE_USER = "SELECT id FROM users " +
            "WHERE id = ? AND active = 1 FOR UPDATE";

    private static final String LOCK_ACTIVE_BOOK = "SELECT id, available_copies FROM books " +
            "WHERE id = ? AND active = 1 FOR UPDATE";

    private static final String INSERT_TRANSACTION = "INSERT INTO transactions (" +
            "user_id, book_id, borrowed_at, due_at, returned_at, status, fine_amount, notes" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String DECREMENT_AVAILABLE_COPIES = "UPDATE books " +
            "SET available_copies = available_copies - 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = ?";

    private static final String LOCK_ACTIVE_TRANSACTION = "SELECT id, user_id, book_id, borrowed_at, due_at, " +
            "returned_at, status, fine_amount, notes, created_at, updated_at FROM transactions " +
            "WHERE id = ? AND returned_at IS NULL AND status IN ('BORROWED', 'OVERDUE') FOR UPDATE";

    private static final String UPDATE_RETURN_TRANSACTION = "UPDATE transactions SET " +
            "returned_at = ?, status = 'RETURNED', fine_amount = ?, notes = ?, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String INCREMENT_AVAILABLE_COPIES = "UPDATE books " +
            "SET available_copies = available_copies + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = ?";

    private static final String SELECT_TRANSACTION_BY_ID = "SELECT id, user_id, book_id, borrowed_at, due_at, " +
            "returned_at, status, fine_amount, notes, created_at, updated_at FROM transactions WHERE id = ?";

    private static final String SELECT_TRANSACTIONS_BY_USER_ID = "SELECT id, user_id, book_id, borrowed_at, " +
            "due_at, returned_at, status, fine_amount, notes, created_at, updated_at FROM transactions " +
            "WHERE user_id = ? ORDER BY borrowed_at DESC, id DESC";

    private static final String SELECT_ACTIVE_TRANSACTION_BY_BOOK_ID = "SELECT id, user_id, book_id, borrowed_at, " +
            "due_at, returned_at, status, fine_amount, notes, created_at, updated_at FROM transactions " +
            "WHERE book_id = ? AND returned_at IS NULL AND status IN ('BORROWED', 'OVERDUE') " +
            "ORDER BY borrowed_at DESC, id DESC LIMIT 1";

    public Transaction issueBook(long userId, long bookId, LocalDateTime dueAt, String notes) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureActiveUserExists(connection, userId);
                ensureBookAvailable(connection, bookId);

                long transactionId = insertTransaction(connection, userId, bookId, dueAt, notes);
                updateBookAvailability(connection, DECREMENT_AVAILABLE_COPIES, bookId);

                connection.commit();
                return findById(transactionId)
                        .orElseThrow(() -> new DAOException("Failed to load newly created transaction with id " + transactionId));
            } catch (Exception exception) {
                rollbackQuietly(connection);
                if (exception instanceof DAOException) {
                    throw (DAOException) exception;
                }
                throw new DAOException("Failed to issue book.", exception);
            } finally {
                resetAutoCommit(connection);
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to issue book.", exception);
        }
    }

    public boolean returnBook(long transactionId, LocalDateTime returnedAt, BigDecimal fineAmount, String notes) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Transaction activeTransaction = lockActiveTransaction(connection, transactionId)
                        .orElseThrow(() -> new DAOException("No active transaction found for id " + transactionId));

                try (PreparedStatement statement = connection.prepareStatement(UPDATE_RETURN_TRANSACTION)) {
                    statement.setTimestamp(1, Timestamp.valueOf(returnedAt));
                    statement.setBigDecimal(2, fineAmount == null ? BigDecimal.ZERO : fineAmount);
                    statement.setString(3, notes);
                    statement.setLong(4, transactionId);

                    if (statement.executeUpdate() == 0) {
                        throw new DAOException("Failed to update transaction return state.");
                    }
                }

                updateBookAvailability(connection, INCREMENT_AVAILABLE_COPIES, activeTransaction.getBookId());
                connection.commit();
                return true;
            } catch (Exception exception) {
                rollbackQuietly(connection);
                if (exception instanceof DAOException) {
                    throw (DAOException) exception;
                }
                throw new DAOException("Failed to return book.", exception);
            } finally {
                resetAutoCommit(connection);
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to return book.", exception);
        }
    }

    public List<Transaction> findByUserId(long userId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TRANSACTIONS_BY_USER_ID)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
            return transactions;
        } catch (SQLException exception) {
            throw new DAOException("Failed to find transactions by user id.", exception);
        }
    }

    public Optional<Transaction> findActiveByBookId(long bookId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVE_TRANSACTION_BY_BOOK_ID)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTransaction(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find active transaction by book id.", exception);
        }
    }

    public Optional<Transaction> findById(long transactionId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_TRANSACTION_BY_ID)) {
            statement.setLong(1, transactionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTransaction(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find transaction by id.", exception);
        }
    }

    private void ensureActiveUserExists(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_ACTIVE_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DAOException("Active user not found for id " + userId);
                }
            }
        }
    }

    private void ensureBookAvailable(Connection connection, long bookId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_ACTIVE_BOOK)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DAOException("Active book not found for id " + bookId);
                }
                int availableCopies = resultSet.getInt("available_copies");
                if (availableCopies <= 0) {
                    throw new DAOException("No available copies for book id " + bookId);
                }
            }
        }
    }

    private long insertTransaction(Connection connection, long userId, long bookId, LocalDateTime dueAt, String notes)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TRANSACTION, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime borrowedAt = LocalDateTime.now();
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            statement.setTimestamp(3, Timestamp.valueOf(borrowedAt));
            statement.setTimestamp(4, Timestamp.valueOf(dueAt));
            statement.setTimestamp(5, null);
            statement.setString(6, "BORROWED");
            statement.setBigDecimal(7, BigDecimal.ZERO);
            statement.setString(8, notes);

            if (statement.executeUpdate() == 0) {
                throw new DAOException("Failed to insert transaction.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new DAOException("Failed to insert transaction: no generated key returned.");
                }
                return generatedKeys.getLong(1);
            }
        }
    }

    private Optional<Transaction> lockActiveTransaction(Connection connection, long transactionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_ACTIVE_TRANSACTION)) {
            statement.setLong(1, transactionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTransaction(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    private void updateBookAvailability(Connection connection, String sql, long bookId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            if (statement.executeUpdate() == 0) {
                throw new DAOException("Failed to update book availability for book id " + bookId);
            }
        }
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getLong("book_id"),
                toLocalDateTime(resultSet.getTimestamp("borrowed_at")),
                toLocalDateTime(resultSet.getTimestamp("due_at")),
                toLocalDateTime(resultSet.getTimestamp("returned_at")),
                resultSet.getString("status"),
                resultSet.getBigDecimal("fine_amount"),
                resultSet.getString("notes"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("updated_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DAOException("Rollback failed.", exception);
        }
    }

    private void resetAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            throw new DAOException("Failed to reset auto-commit.", exception);
        }
    }
}
