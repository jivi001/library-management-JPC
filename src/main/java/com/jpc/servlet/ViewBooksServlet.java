package com.jpc.servlet;

import com.jpc.dao.BookDAO;
import com.jpc.dao.DAOException;
import com.jpc.model.Book;
import com.jpc.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/books")
public class ViewBooksServlet extends AuthenticatedServlet {

    private static final String BOOKS_VIEW = "/books.jsp";
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAuthenticatedUserId(request, response) == null) {
            return;
        }

        try {
            List<Book> books = bookDAO.findAllActive();
            request.setAttribute("books", books);
            request.setAttribute("message", ValidationUtil.normalizeOptional(request.getParameter("message")));
            request.setAttribute("errorMessage", ValidationUtil.normalizeOptional(request.getParameter("error")));
            forwardToView(request, response, BOOKS_VIEW);
        } catch (DAOException exception) {
            throw new ServletException("Unable to load books.", exception);
        }
    }
}
