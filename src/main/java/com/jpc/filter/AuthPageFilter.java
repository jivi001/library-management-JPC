package com.jpc.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/dashboard.jsp", "/books.jsp"})
public class AuthPageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean authenticated = false;
        if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj instanceof Number) {
                Number userId = (Number) userIdObj;
                authenticated = userId.longValue() > 0L;
            }
        }

        if (!authenticated) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?session=expired");
            return;
        }

        chain.doFilter(request, response);
    }
}
