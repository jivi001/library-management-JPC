package com.jpc.servlet;

import com.jpc.dao.BookDAO;
import com.jpc.dao.DAOException;
import com.jpc.dao.TransactionDAO;
import com.jpc.model.Book;
import com.jpc.model.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@WebServlet("/books/issue")
public class IssueBookServlet extends AuthenticatedServlet {

    private static final int MAX_NOTES_LENGTH = 500;

    private final BookDAO bookDAO = new BookDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = requireAuthenticatedUserId(request, response);
        if (userId == null) {
            return;
        }

        Long bookId = parsePositiveLong(request.getParameter("bookId"));
        LocalDateTime dueAt = parseDueAt(request.getParameter("dueDate"));
        String notes = normalizeOptional(request.getParameter("notes"));

        String validationError = validateIssueInput(bookId, dueAt, notes);
        if (validationError != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
            return;
        }

        try {
            Optional<Book> bookOptional = bookDAO.findById(bookId);
            if (bookOptional.isEmpty() || !bookOptional.get().isActive()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                return;
            }
            if (bookOptional.get().getAvailableCopies() <= 0) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "This book is currently unavailable.");
                return;
            }

            Transaction transaction = transactionDAO.issueBook(userId, bookId, dueAt, notes);
            writeJson(response, HttpServletResponse.SC_CREATED, """
                    {
                      "message":"Book issued successfully.",
                      "transactionId":%d,
                      "bookId":%d,
                      "userId":%d,
                      "status":"%s"
                    }
                    """.formatted(transaction.getId(), transaction.getBookId(), transaction.getUserId(),
                    escapeJson(transaction.getStatus())));
        } catch (DAOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("No available copies")) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "This book is currently unavailable.");
                return;
            }
            throw new ServletException("Unable to issue book.", exception);
        }
    }

    private String validateIssueInput(Long bookId, LocalDateTime dueAt, String notes) {
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

    private LocalDateTime parseDueAt(String value) {
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
}
