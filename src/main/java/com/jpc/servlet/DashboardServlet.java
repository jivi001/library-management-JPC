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
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends AuthenticatedServlet {

    private static final String DASHBOARD_VIEW = "/dashboard.jsp";

    private final BookDAO bookDAO = new BookDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = requireAuthenticatedUserId(request, response);
        if (userId == null) {
            return;
        }

        try {
            List<Book> books = bookDAO.findAllActive();
            List<Transaction> transactions = transactionDAO.findByUserId(userId);

            long activeLoans = transactions.stream()
                    .filter(this::isOpenTransaction)
                    .count();
            long overdueReturns = transactions.stream()
                    .filter(this::isOverdueTransaction)
                    .count();

            request.setAttribute("booksCount", books.size());
            request.setAttribute("availableBooks", books.stream().mapToInt(Book::getAvailableCopies).sum());
            request.setAttribute("activeLoans", activeLoans);
            request.setAttribute("pendingReturns", overdueReturns);
            request.setAttribute("recentTransactions", transactions.stream().limit(5).toList());
            request.setAttribute("message", ValidationUtil.normalizeOptional(request.getParameter("message")));
            request.setAttribute("errorMessage", ValidationUtil.normalizeOptional(request.getParameter("error")));

            forwardToView(request, response, DASHBOARD_VIEW);
        } catch (DAOException exception) {
            throw new ServletException("Unable to load dashboard.", exception);
        }
    }

    private boolean isOpenTransaction(Transaction transaction) {
        return transaction.getReturnedAt() == null
                && ("BORROWED".equalsIgnoreCase(transaction.getStatus())
                || "OVERDUE".equalsIgnoreCase(transaction.getStatus()));
    }

    private boolean isOverdueTransaction(Transaction transaction) {
        return transaction.getReturnedAt() == null
                && transaction.getDueAt() != null
                && transaction.getDueAt().isBefore(LocalDateTime.now());
    }
}
