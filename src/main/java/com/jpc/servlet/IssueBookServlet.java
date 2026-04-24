package com.jpc.servlet;

import com.jpc.dao.BookDAO;
import com.jpc.dao.DAOException;
import com.jpc.dao.TransactionDAO;
import com.jpc.model.Book;
import com.jpc.model.Transaction;
import com.jpc.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@WebServlet("/books/issue")
public class IssueBookServlet extends AuthenticatedServlet {

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
        LocalDateTime dueAt = ValidationUtil.parseDueDateAtEndOfDay(request.getParameter("dueDate"));
        String notes = ValidationUtil.normalizeOptional(request.getParameter("notes"));

        String validationError = ValidationUtil.validateIssueRequest(bookId, dueAt, notes);
        if (validationError != null) {
            redirectWithQuery(request, response, "/books", "error", validationError);
            return;
        }

        try {
            Optional<Book> bookOptional = bookDAO.findById(bookId);
            if (bookOptional.isEmpty() || !bookOptional.get().isActive()) {
                redirectWithQuery(request, response, "/books", "error", "Book not found.");
                return;
            }
            if (bookOptional.get().getAvailableCopies() <= 0) {
                redirectWithQuery(request, response, "/books", "error", "This book is currently unavailable.");
                return;
            }

            Transaction transaction = transactionDAO.issueBook(userId, bookId, dueAt, notes);
            redirectWithQuery(request, response, "/books", "message",
                    "Book issued successfully. Transaction ID: " + transaction.getId());
        } catch (DAOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("No available copies")) {
                redirectWithQuery(request, response, "/books", "error", "This book is currently unavailable.");
                return;
            }
            redirectWithQuery(request, response, "/books", "error", "Unable to issue the book right now.");
        }
    }
}
