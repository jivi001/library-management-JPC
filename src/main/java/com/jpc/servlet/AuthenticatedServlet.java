package com.jpc.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

abstract class AuthenticatedServlet extends HttpServlet {

    protected Long requireAuthenticatedUserId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.");
            return null;
        }

        Object userIdAttribute = session.getAttribute("userId");
        if (!(userIdAttribute instanceof Number userIdNumber) || userIdNumber.longValue() <= 0L) {
            session.invalidate();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your session is invalid. Please log in again.");
            return null;
        }

        return userIdNumber.longValue();
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

    protected String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char current : value.toCharArray()) {
            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (current < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) current));
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
