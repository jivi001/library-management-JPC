package com.jpc.servlet;

import com.jpc.dao.BookDAO;
import com.jpc.dao.DAOException;
import com.jpc.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/books")
public class ViewBooksServlet extends AuthenticatedServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAuthenticatedUserId(request, response) == null) {
            return;
        }

        try {
            List<Book> books = bookDAO.findAllActive();
            StringBuilder json = new StringBuilder();
            json.append("{\"books\":[");

            for (int index = 0; index < books.size(); index++) {
                Book book = books.get(index);
                if (index > 0) {
                    json.append(',');
                }

                json.append("{")
                        .append("\"id\":").append(book.getId()).append(',')
                        .append("\"isbn\":\"").append(escapeJson(book.getIsbn())).append("\",")
                        .append("\"title\":\"").append(escapeJson(book.getTitle())).append("\",")
                        .append("\"author\":\"").append(escapeJson(book.getAuthor())).append("\",")
                        .append("\"publisher\":\"").append(escapeJson(book.getPublisher())).append("\",")
                        .append("\"category\":\"").append(escapeJson(book.getCategory())).append("\",")
                        .append("\"publishedYear\":").append(book.getPublishedYear() == null ? "null" : book.getPublishedYear()).append(',')
                        .append("\"totalCopies\":").append(book.getTotalCopies()).append(',')
                        .append("\"availableCopies\":").append(book.getAvailableCopies()).append(',')
                        .append("\"shelfLocation\":\"").append(escapeJson(book.getShelfLocation())).append("\",")
                        .append("\"active\":").append(book.isActive())
                        .append("}");
            }

            json.append("]}");
            writeJson(response, HttpServletResponse.SC_OK, json.toString());
        } catch (DAOException exception) {
            throw new ServletException("Unable to load books.", exception);
        }
    }
}
