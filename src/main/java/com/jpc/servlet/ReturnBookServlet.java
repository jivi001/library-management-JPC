package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.TransactionDAO;
import com.jpc.model.Transaction;
import com.jpc.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/books/return")
public class ReturnBookServlet extends AuthenticatedServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = requireAuthenticatedUserId(request, response);
        if (userId == null) {
            return;
        }

        Long transactionId = parsePositiveLong(request.getParameter("transactionId"));
        BigDecimal fineAmount = ValidationUtil.parseNonNegativeAmount(request.getParameter("fineAmount"));
        String notes = ValidationUtil.normalizeOptional(request.getParameter("notes"));

        String validationError = ValidationUtil.validateReturnRequest(transactionId, fineAmount, notes);
        if (validationError != null) {
            redirectWithQuery(request, response, "/books", "error", validationError);
            return;
        }

        try {
            Optional<Transaction> transactionOptional = transactionDAO.findById(transactionId);
            if (transactionOptional.isEmpty()) {
                redirectWithQuery(request, response, "/books", "error", "Transaction not found.");
                return;
            }

            Transaction transaction = transactionOptional.get();
            if (transaction.getUserId() != userId.longValue()) {
                redirectWithQuery(request, response, "/books", "error",
                        "You cannot return a book for another user.");
                return;
            }
            if (transaction.getReturnedAt() != null || "RETURNED".equalsIgnoreCase(transaction.getStatus())) {
                redirectWithQuery(request, response, "/books", "error",
                        "This transaction has already been closed.");
                return;
            }

            boolean returned = transactionDAO.returnBook(transactionId, java.time.LocalDateTime.now(), fineAmount, notes);
            if (!returned) {
                redirectWithQuery(request, response, "/books", "error", "Unable to return the book.");
                return;
            }

            redirectWithQuery(request, response, "/books", "message",
                    "Book returned successfully. Transaction ID: " + transaction.getId());
        } catch (DAOException exception) {
            redirectWithQuery(request, response, "/books", "error", "Unable to return the book right now.");
        }
    }
}
