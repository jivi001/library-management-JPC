package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import com.jpc.model.User;
import com.jpc.util.EmailUtil;
import com.jpc.util.PasswordUtil;
import com.jpc.util.TokenUtil;
import com.jpc.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final String SIGNUP_VIEW = "/signup.jsp";
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean authenticated = false;
        if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj instanceof Number) {
                Number userId = (Number) userIdObj;
                authenticated = userId.longValue() > 0L;
            }
        }
        
        if (authenticated) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher(SIGNUP_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = ValidationUtil.normalize(request.getParameter("fullName"));
        String email = ValidationUtil.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String validationError = ValidationUtil.validateSignup(fullName, email, password, confirmPassword);
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            request.getRequestDispatcher(SIGNUP_VIEW).forward(request, response);
            return;
        }

        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isPresent()) {
                request.setAttribute("errorMessage", "An account with this email already exists.");
                request.getRequestDispatcher(SIGNUP_VIEW).forward(request, response);
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

            response.sendRedirect(request.getContextPath() + "/login?signup=success");
        } catch (DAOException | IllegalStateException exception) {
            throw new ServletException("Unable to complete signup.", exception);
        }
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
}
