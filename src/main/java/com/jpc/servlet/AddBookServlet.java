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

@WebServlet("/books/add")
public class AddBookServlet extends AuthenticatedServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAuthenticatedUserId(request, response) == null) {
            return;
        }

        String isbn = ValidationUtil.normalizeOptional(request.getParameter("isbn"));
        String title = ValidationUtil.normalize(request.getParameter("title"));
        String author = ValidationUtil.normalize(request.getParameter("author"));
        String publisher = ValidationUtil.normalizeOptional(request.getParameter("publisher"));
        String category = ValidationUtil.normalizeOptional(request.getParameter("category"));
        String shelfLocation = ValidationUtil.normalizeOptional(request.getParameter("shelfLocation"));
        Integer publishedYear = ValidationUtil.parseNullableYear(request.getParameter("publishedYear"));
        Integer totalCopies = parsePositiveInt(request.getParameter("totalCopies"));

        String validationError = ValidationUtil.validateBookInput(isbn, title, author, publisher, category, shelfLocation,
                publishedYear, totalCopies);
        if (validationError != null) {
            redirectWithQuery(request, response, "/books", "error", validationError);
            return;
        }

        try {
            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublisher(publisher);
            book.setCategory(category);
            book.setPublishedYear(publishedYear);
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(totalCopies);
            book.setShelfLocation(shelfLocation);
            book.setActive(true);

            Book savedBook = bookDAO.createBook(book);
            redirectWithQuery(request, response, "/books", "message",
                    "Book added successfully. Book ID: " + savedBook.getId());
        } catch (DAOException exception) {
            redirectWithQuery(request, response, "/books", "error", "Unable to add the book right now.");
        }
    }
}
