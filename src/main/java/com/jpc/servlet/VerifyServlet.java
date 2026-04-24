package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/verify")
public class VerifyServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String token = normalize(request.getParameter("token"));
        if (token == null || token.length() != 64 || !token.matches("[A-Fa-f0-9]{64}")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid verification token.");
            return;
        }

        try {
            boolean verified = userDAO.verifyUser(token);
            if (!verified) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Verification token is invalid or expired.");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Account verified successfully. You can now log in.");
        } catch (DAOException exception) {
            throw new ServletException("Unable to verify account.", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
