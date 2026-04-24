package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.TransactionDAO;
import com.jpc.model.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/books/return")
public class ReturnBookServlet extends AuthenticatedServlet {

    private static final int MAX_NOTES_LENGTH = 500;
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = requireAuthenticatedUserId(request, response);
        if (userId == null) {
            return;
        }

        Long transactionId = parsePositiveLong(request.getParameter("transactionId"));
        BigDecimal fineAmount = parseFineAmount(request.getParameter("fineAmount"));
        String notes = normalizeOptional(request.getParameter("notes"));

        String validationError = validateReturnInput(transactionId, fineAmount, notes);
        if (validationError != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
            return;
        }

        try {
            Optional<Transaction> transactionOptional = transactionDAO.findById(transactionId);
            if (transactionOptional.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Transaction not found.");
                return;
            }

            Transaction transaction = transactionOptional.get();
            if (transaction.getUserId() != userId.longValue()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot return a book for another user.");
                return;
            }
            if (transaction.getReturnedAt() != null || "RETURNED".equalsIgnoreCase(transaction.getStatus())) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "This transaction has already been closed.");
                return;
            }

            boolean returned = transactionDAO.returnBook(transactionId, java.time.LocalDateTime.now(), fineAmount, notes);
            if (!returned) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to return the book.");
                return;
            }

            writeJson(response, HttpServletResponse.SC_OK, """
                    {
                      "message":"Book returned successfully.",
                      "transactionId":%d,
                      "bookId":%d,
                      "fineAmount":%s
                    }
                    """.formatted(transaction.getId(), transaction.getBookId(), fineAmount.toPlainString()));
        } catch (DAOException exception) {
            throw new ServletException("Unable to return book.", exception);
        }
    }

    private String validateReturnInput(Long transactionId, BigDecimal fineAmount, String notes) {
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

    private BigDecimal parseFineAmount(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
