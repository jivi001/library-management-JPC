package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import com.jpc.model.User;
import com.jpc.util.PasswordUtil;
import com.jpc.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends AuthenticatedServlet {

    private static final String LOGIN_VIEW = "/login.jsp";
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        applyNoCacheHeaders(response);
        if (isAuthenticatedSession(request)) {
            redirect(request, response, "/dashboard");
            return;
        }
        forwardToView(request, response, LOGIN_VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = ValidationUtil.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");

        String validationError = ValidationUtil.validateLogin(email, password);
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            forwardToView(request, response, LOGIN_VIEW);
            return;
        }

        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isEmpty()) {
                PasswordUtil.performDummyVerification(password);
                request.setAttribute("errorMessage", "Invalid email or password.");
                forwardToView(request, response, LOGIN_VIEW);
                return;
            }

            User user = existingUser.get();
            boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
            if (!passwordMatches) {
                request.setAttribute("errorMessage", "Invalid email or password.");
                forwardToView(request, response, LOGIN_VIEW);
                return;
            }

            if (!user.isActive() || !user.isVerified()) {
                PasswordUtil.performDummyVerification(password);
                request.setAttribute("errorMessage", "Invalid email or password.");
                forwardToView(request, response, LOGIN_VIEW);
                return;
            }

            if (PasswordUtil.needsRehash(user.getPasswordHash())) {
                String upgradedHash = PasswordUtil.hashPassword(password);
                userDAO.updatePasswordHash(user.getId(), upgradedHash);
            }

            createSecureSession(request, user);
            userDAO.updateLastLogin(user.getId());
            redirect(request, response, "/dashboard");
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
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute(AUTHENTICATED_SESSION_KEY, Boolean.TRUE);
        session.setAttribute(USER_ID_SESSION_KEY, user.getId());
        session.setAttribute(USER_EMAIL_SESSION_KEY, user.getEmail());
        session.setAttribute(USER_NAME_SESSION_KEY, user.getFullName());
        session.setAttribute("authenticatedAt", System.currentTimeMillis());
    }
}
