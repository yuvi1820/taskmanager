package com.taskmanager.servlet;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = { "/login" })
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = SessionUtil.getUser(request);
        if (user != null) {
            redirectByRole(response, request, user);
            return;
        }
        if ("1".equals(request.getParameter("registered"))) {
            request.setAttribute("success", "Account created successfully! Please sign in.");
        }
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = trim(request.getParameter("username"));
        String password = trim(request.getParameter("password"));

        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                request.setAttribute("error", "Invalid username or password.");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }
            SessionUtil.setUser(request, user);
            redirectByRole(response, request, user);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void redirectByRole(HttpServletResponse response, HttpServletRequest request, User user)
            throws IOException {
        if (user.isManager()) {
            response.sendRedirect(request.getContextPath() + "/manager/tasks");
        } else {
            response.sendRedirect(request.getContextPath() + "/employee/tasks");
        }
    }

    private String trim(String value) {
        return value != null ? value.trim() : "";
    }
}
