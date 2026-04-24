package com.jpc.servlet;

import com.jpc.dao.DAOException;
import com.jpc.dao.UserDAO;
import com.jpc.util.ValidationUtil;
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
        String token = ValidationUtil.normalizeOptional(request.getParameter("token"));
        if (!ValidationUtil.isValidVerificationToken(token)) {
            response.sendRedirect(request.getContextPath() + "/login?verified=invalid");
            return;
        }

        try {
            boolean verified = userDAO.verifyUser(token);
            if (!verified) {
                response.sendRedirect(request.getContextPath() + "/login?verified=invalid");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/login?verified=success");
        } catch (DAOException exception) {
            throw new ServletException("Unable to verify account.", exception);
        }
    }
}
