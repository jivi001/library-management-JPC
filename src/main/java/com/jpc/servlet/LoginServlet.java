package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import com.jpc.model.User;
import com.jpc.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String email = normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");

        String validationError = validateLogin(email, password);
        if (validationError != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
            return;
        }

        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
                return;
            }

            User user = existingUser.get();
            if (!user.isActive()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is inactive.");
                return;
            }
            if (!user.isVerified()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Please verify your email before logging in.");
                return;
            }
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
                return;
            }

            Optional<User> verifiedUser = userDAO.findVerifiedActiveUserByEmail(email);
            if (verifiedUser.isEmpty()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is not available for login.");
                return;
            }

            createSecureSession(request, verifiedUser.get());
            userDAO.updateLastLogin(verifiedUser.get().getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Login successful.");
        } catch (DAOException exception) {
            throw new ServletException("Unable to complete login.", exception);
        }
    }

    private void createSecureSession(HttpServletRequest request, User user) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getFullName());
    }

    private String validateLogin(String email, String password) {
        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 255) {
            return "Please provide a valid email address.";
        }
        if (password == null || password.isBlank()) {
            return "Password is required.";
        }
        return null;
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
