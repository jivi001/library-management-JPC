package com.jpc.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

abstract class AuthenticatedServlet extends HttpServlet {

    protected static final String USER_ID_SESSION_KEY = "userId";
    protected static final String USER_EMAIL_SESSION_KEY = "userEmail";
    protected static final String USER_NAME_SESSION_KEY = "userName";
    protected static final String AUTHENTICATED_SESSION_KEY = "authenticated";
    protected static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    protected Long requireAuthenticatedUserId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectToLogin(request, response, "expired");
            return null;
        }

        Object authenticatedAttribute = session.getAttribute(AUTHENTICATED_SESSION_KEY);
        Object userIdAttribute = session.getAttribute(USER_ID_SESSION_KEY);
        
        boolean authenticated = authenticatedAttribute instanceof Boolean && (Boolean) authenticatedAttribute;
        boolean validUserId = userIdAttribute instanceof Number && ((Number) userIdAttribute).longValue() > 0L;

        if (!authenticated || !validUserId) {
            session.invalidate();
            redirectToLogin(request, response, "expired");
            return null;
        }

        applyNoCacheHeaders(response);
        return ((Number) userIdAttribute).longValue();
    }

    protected String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    protected String normalizeOptional(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    protected Long parsePositiveLong(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        try {
            long parsedValue = Long.parseLong(normalized);
            return parsedValue > 0L ? parsedValue : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    protected Integer parsePositiveInt(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        try {
            int parsedValue = Integer.parseInt(normalized);
            return parsedValue > 0 ? parsedValue : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    protected void writeJson(HttpServletResponse response, int status, String body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(body);
    }

    protected boolean isAuthenticatedSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object authenticatedAttribute = session.getAttribute(AUTHENTICATED_SESSION_KEY);
        Object userIdAttribute = session.getAttribute(USER_ID_SESSION_KEY);
        
        return authenticatedAttribute instanceof Boolean 
                && (Boolean) authenticatedAttribute
                && userIdAttribute instanceof Number 
                && ((Number) userIdAttribute).longValue() > 0L;
    }

    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirectWithQuery(HttpServletRequest request, HttpServletResponse response, String path,
                                     String parameterName, String value) throws IOException {
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path + "?" + parameterName + "=" + encodedValue);
    }

    protected void redirectWithTwoQueries(HttpServletRequest request, HttpServletResponse response, String path,
                                          String firstParameterName, String firstValue,
                                          String secondParameterName, String secondValue) throws IOException {
        String encodedFirst = URLEncoder.encode(firstValue, StandardCharsets.UTF_8);
        String encodedSecond = URLEncoder.encode(secondValue, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path
                + "?" + firstParameterName + "=" + encodedFirst
                + "&" + secondParameterName + "=" + encodedSecond);
    }

    protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String reason)
            throws IOException {
        redirectWithQuery(request, response, "/login", "session", reason);
    }

    protected void applyNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    protected String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char current : value.toCharArray()) {
            switch (current) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (current < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) current));
                    } else {
                        escaped.append(current);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
