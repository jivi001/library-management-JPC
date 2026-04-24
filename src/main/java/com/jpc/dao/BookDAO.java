package com.jpc.dao;

import com.jpc.model.Book;
import com.jpc.util.DBConnection;

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

public class BookDAO {

    private static final String INSERT_BOOK = """
            INSERT INTO books (
                isbn,
                title,
                author,
                publisher,
                category,
                published_year,
                total_copies,
                available_copies,
                shelf_location,
                is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BOOK_BY_ID = """
            SELECT id, isbn, title, author, publisher, category, published_year,
                   total_copies, available_copies, shelf_location, is_active,
                   created_at, updated_at
            FROM books
            WHERE id = ?
            """;

    private static final String SELECT_ALL_ACTIVE_BOOKS = """
            SELECT id, isbn, title, author, publisher, category, published_year,
                   total_copies, available_copies, shelf_location, is_active,
                   created_at, updated_at
            FROM books
            WHERE is_active = 1
            ORDER BY title ASC, author ASC
            """;

    private static final String UPDATE_BOOK = """
            UPDATE books
            SET isbn = ?,
                title = ?,
                author = ?,
                publisher = ?,
                category = ?,
                published_year = ?,
                total_copies = ?,
                available_copies = ?,
                shelf_location = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_active = 1
            """;

    private static final String SOFT_DELETE_BOOK = """
            UPDATE books
            SET is_active = 0,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_active = 1
            """;

    public Book createBook(Book book) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)) {
            bindCreateBook(statement, book);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Failed to create book: no rows inserted.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new DAOException("Failed to create book: no generated key returned.");
                }
                long bookId = generatedKeys.getLong(1);
                return findById(bookId)
                        .orElseThrow(() -> new DAOException("Failed to load newly created book with id " + bookId));
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to create book.", exception);
        }
    }

    public Optional<Book> findById(long id) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BOOK_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBook(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find book by id.", exception);
        }
    }

    public List<Book> findAllActive() {
        List<Book> books = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_ACTIVE_BOOKS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                books.add(mapBook(resultSet));
            }
            return books;
        } catch (SQLException exception) {
            throw new DAOException("Failed to load active books.", exception);
        }
    }

    public boolean updateBook(Book book) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BOOK)) {
            bindUpdateBook(statement, book);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DAOException("Failed to update book.", exception);
        }
    }

    public boolean softDeleteBook(long id) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SOFT_DELETE_BOOK)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DAOException("Failed to soft delete book.", exception);
        }
    }

    private void bindCreateBook(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.getIsbn());
        statement.setString(2, book.getTitle());
        statement.setString(3, book.getAuthor());
        statement.setString(4, book.getPublisher());
        statement.setString(5, book.getCategory());
        setInteger(statement, 6, book.getPublishedYear());
        statement.setInt(7, book.getTotalCopies());
        statement.setInt(8, book.getAvailableCopies());
        statement.setString(9, book.getShelfLocation());
        statement.setBoolean(10, book.isActive());
    }

    private void bindUpdateBook(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.getIsbn());
        statement.setString(2, book.getTitle());
        statement.setString(3, book.getAuthor());
        statement.setString(4, book.getPublisher());
        statement.setString(5, book.getCategory());
        setInteger(statement, 6, book.getPublishedYear());
        statement.setInt(7, book.getTotalCopies());
        statement.setInt(8, book.getAvailableCopies());
        statement.setString(9, book.getShelfLocation());
        statement.setLong(10, book.getId());
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        int publishedYearValue = resultSet.getInt("published_year");
        Integer publishedYear = resultSet.wasNull() ? null : publishedYearValue;

        return new Book(
                resultSet.getLong("id"),
                resultSet.getString("isbn"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getString("publisher"),
                resultSet.getString("category"),
                publishedYear,
                resultSet.getInt("total_copies"),
                resultSet.getInt("available_copies"),
                resultSet.getString("shelf_location"),
                resultSet.getBoolean("is_active"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("updated_at"))
        );
    }

    private void setInteger(PreparedStatement statement, int parameterIndex, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, java.sql.Types.INTEGER);
        } else {
            statement.setInt(parameterIndex, value);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
