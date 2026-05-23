package com.taskmanager.servlet;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.DatabaseUtil;
import com.taskmanager.util.EmailService;
import com.taskmanager.util.OTPUtil;
import com.taskmanager.util.PasswordUtil;
import com.taskmanager.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "ManagerAddEmployeeServlet", urlPatterns = { "/manager/add-employee" })
public class ManagerAddEmployeeServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(request);
        if (manager == null || !manager.isManager()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(request);
        if (manager == null || !manager.isManager()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("sendOTP".equals(action)) {
            handleSendOTP(request, response);
            return;
        }

        if ("checkUsername".equals(action)) {
            handleCheckUsername(request, response);
            return;
        }

        if ("checkEmail".equals(action)) {
            try {
                handleCheckEmail(request, response);
            } catch (SQLException e) {
                throw new ServletException("Database error", e);
            }
            return;
        }

        if ("verifyEmailOTP".equals(action)) {
            try {
                handleVerifyEmailOTP(request, response);
            } catch (SQLException e) {
                throw new ServletException("Database error", e);
            }
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String username = trim(request.getParameter("username")).toLowerCase();
        String email = (String) request.getSession().getAttribute("email");
        String password = trim(request.getParameter("password"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));

        if (email == null) {
            request.setAttribute("error", "Email verification required. Please start over.");
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
            return;
        }

        String error = validateEmployee(fullName, username, password, confirmPassword);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("fullName", fullName);
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
            return;
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                request.setAttribute("error", "Username already taken. Choose another.");
                request.setAttribute("fullName", fullName);
                request.setAttribute("username", username);
                request.setAttribute("email", email);
                request.setAttribute("emailVerified", true);
                request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
                return;
            }

            // Create the employee directly since email is already verified
            User employee = new User();
            employee.setUsername(username);
            employee.setFullName(fullName);
            employee.setEmail(email);
            employee.setRole("employee");
            employee.setPasswordHash(PasswordUtil.hash(password));
            employee.setStatus("active");
            userDAO.insert(employee);

            // Clear session data
            request.getSession().removeAttribute("emailVerified");
            request.getSession().removeAttribute("email");

            response.sendRedirect(request.getContextPath() + "/manager/tasks?employeeAdded=1");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void handleSendOTP(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trim(request.getParameter("email"));

        if (email.isEmpty()) {
            request.setAttribute("error", "Email is required to send OTP.");
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
            return;
        }

        try {
            String otp = OTPUtil.generateOTP();
            request.getSession().setAttribute("pendingEmail", email);
            request.getSession().setAttribute("pendingOTP", otp);
            request.getSession().setAttribute("otpExpiration", OTPUtil.getOTPExpiration());
            EmailService.sendOTP(email, otp);
            request.setAttribute("otpSent", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
        } catch (MessagingException e) {
            request.setAttribute("error", "Failed to send OTP. Please check email configuration.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
        }
    }

    private void handleCheckUsername(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username")).toLowerCase();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            boolean exists = userDAO.findByUsername(username) != null;
            response.getWriter().write("{\"available\": " + !exists + "}");
        } catch (SQLException e) {
            response.getWriter().write("{\"available\": false, \"error\": \"Database error\"}");
        }
    }

    private void handleCheckEmail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String email = trim(request.getParameter("email"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check if email already exists
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                boolean exists = rs.getInt(1) > 0;
                response.getWriter().write("{\"available\": " + !exists + "}");
            }
        }
    }

    private void handleVerifyEmailOTP(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String otp = trim(request.getParameter("otp"));
        String email = (String) request.getSession().getAttribute("pendingEmail");
        String pendingOTP = (String) request.getSession().getAttribute("pendingOTP");
        java.sql.Timestamp otpExpiration = (java.sql.Timestamp) request.getSession().getAttribute("otpExpiration");

        if (email == null || otp.isEmpty()) {
            request.setAttribute("error", "Invalid request. Please start over.");
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
            return;
        }

        if (OTPUtil.validateOTP(otp, pendingOTP, otpExpiration)) {
            // Email verified successfully
            request.getSession().setAttribute("emailVerified", true);
            request.getSession().setAttribute("email", email);
            request.getSession().removeAttribute("pendingOTP");
            request.getSession().removeAttribute("otpExpiration");
            request.setAttribute("emailVerified", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Invalid or expired OTP. Please try again.");
            request.setAttribute("otpSent", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/add-employee.jsp").forward(request, response);
        }
    }

    private String validateEmployee(String fullName, String username, String password, String confirmPassword) {
        if (fullName.isEmpty()) {
            return "Full name is required.";
        }
        if (username.length() < 3) {
            return "Username must be at least 3 characters.";
        }
        if (!username.matches("[a-z0-9._-]+")) {
            return "Username may only use letters, numbers, dot, dash, underscore.";
        }
        if (password.length() < 4) {
            return "Password must be at least 4 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }

    private String trim(String value) {
        return value != null ? value.trim() : "";
    }
}
