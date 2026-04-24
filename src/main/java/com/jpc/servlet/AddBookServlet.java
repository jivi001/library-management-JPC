package com.jpc.servlet;

import com.jpc.dao.BookDAO;
import com.jpc.dao.DAOException;
import com.jpc.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Year;

@WebServlet("/books/add")
public class AddBookServlet extends AuthenticatedServlet {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_AUTHOR_LENGTH = 255;
    private static final int MAX_PUBLISHER_LENGTH = 255;
    private static final int MAX_CATEGORY_LENGTH = 100;
    private static final int MAX_SHELF_LENGTH = 50;
    private static final int MAX_ISBN_LENGTH = 20;

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAuthenticatedUserId(request, response) == null) {
            return;
        }

        String isbn = normalizeOptional(request.getParameter("isbn"));
        String title = normalize(request.getParameter("title"));
        String author = normalize(request.getParameter("author"));
        String publisher = normalizeOptional(request.getParameter("publisher"));
        String category = normalizeOptional(request.getParameter("category"));
        String shelfLocation = normalizeOptional(request.getParameter("shelfLocation"));
        Integer publishedYear = parseNullableYear(request.getParameter("publishedYear"));
        Integer totalCopies = parsePositiveInt(request.getParameter("totalCopies"));

        String validationError = validateBookInput(isbn, title, author, publisher, category, shelfLocation,
                publishedYear, totalCopies);
        if (validationError != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
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
            writeJson(response, HttpServletResponse.SC_CREATED, """
                    {
                      "message":"Book added successfully.",
                      "bookId":%d,
                      "availableCopies":%d
                    }
                    """.formatted(savedBook.getId(), savedBook.getAvailableCopies()));
        } catch (DAOException exception) {
            throw new ServletException("Unable to add book.", exception);
        }
    }

    private String validateBookInput(String isbn, String title, String author, String publisher, String category,
                                     String shelfLocation, Integer publishedYear, Integer totalCopies) {
        if (isbn != null && isbn.length() > MAX_ISBN_LENGTH) {
            return "ISBN must not exceed 20 characters.";
        }
        if (title == null || title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
            return "Title is required and must not exceed 255 characters.";
        }
        if (author == null || author.isBlank() || author.length() > MAX_AUTHOR_LENGTH) {
            return "Author is required and must not exceed 255 characters.";
        }
        if (publisher != null && publisher.length() > MAX_PUBLISHER_LENGTH) {
            return "Publisher must not exceed 255 characters.";
        }
        if (category != null && category.length() > MAX_CATEGORY_LENGTH) {
            return "Category must not exceed 100 characters.";
        }
        if (shelfLocation != null && shelfLocation.length() > MAX_SHELF_LENGTH) {
            return "Shelf location must not exceed 50 characters.";
        }
        if (publishedYear != null) {
            int currentYear = Year.now().getValue();
            if (publishedYear < 1000 || publishedYear > currentYear + 1) {
                return "Published year is out of range.";
            }
        }
        if (totalCopies == null) {
            return "Total copies must be a positive whole number.";
        }
        return null;
    }

    private Integer parseNullableYear(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }
}
