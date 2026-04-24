package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import com.jpc.model.User;
import com.jpc.util.EmailUtil;
import com.jpc.util.PasswordUtil;
import com.jpc.util.TokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String fullName = normalize(request.getParameter("fullName"));
        String email = normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String validationError = validateSignup(fullName, email, password, confirmPassword);
        if (validationError != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
            return;
        }

        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isPresent()) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "An account with this email already exists.");
                return;
            }

            String token = TokenUtil.generateVerificationToken();
            LocalDateTime tokenExpiry = TokenUtil.generateExpiry();

            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(PasswordUtil.hashPassword(password));
            user.setVerified(false);
            user.setEmailVerificationToken(token);
            user.setEmailVerificationExpiresAt(tokenExpiry);
            user.setEmailVerifiedAt(null);
            user.setLastLoginAt(null);
            user.setActive(true);

            User savedUser = userDAO.createUser(user);
            sendVerificationEmail(request, savedUser.getEmail(), token);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Signup successful. Please verify your email before logging in.");
        } catch (DAOException | IllegalStateException exception) {
            throw new ServletException("Unable to complete signup.", exception);
        }
    }

    private String validateSignup(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.length() < 2 || fullName.length() > 120) {
            return "Full name must be between 2 and 120 characters.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 255) {
            return "Please provide a valid email address.";
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            return "Password must be between 8 and 72 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Password and confirm password must match.";
        }
        return null;
    }

    private void sendVerificationEmail(HttpServletRequest request, String email, String token) {
        String verificationLink = buildVerificationLink(request, token);
        String message = "Welcome to JPC.\n\n"
                + "Please verify your account using the link below:\n"
                + verificationLink + "\n\n"
                + "If you did not create this account, you can ignore this email.";
        EmailUtil.sendEmail(email, "Verify your JPC account", message);
    }

    private String buildVerificationLink(HttpServletRequest request, String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return request.getScheme()
                + "://"
                + request.getServerName()
                + buildPortSegment(request)
                + request.getContextPath()
                + "/verify?token="
                + encodedToken;
    }

    private String buildPortSegment(HttpServletRequest request) {
        int port = request.getServerPort();
        boolean defaultHttp = "http".equalsIgnoreCase(request.getScheme()) && port == 80;
        boolean defaultHttps = "https".equalsIgnoreCase(request.getScheme()) && port == 443;
        return defaultHttp || defaultHttps ? "" : ":" + port;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }
}
