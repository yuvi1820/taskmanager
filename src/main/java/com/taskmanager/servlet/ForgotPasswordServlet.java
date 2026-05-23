package com.taskmanager.servlet;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.EmailService;
import com.taskmanager.util.OTPUtil;
import com.taskmanager.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = { "/forgot-password" })
public class ForgotPasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("sendOTP".equals(action)) {
            handleSendOTP(request, response);
            return;
        }

        if ("verifyOTP".equals(action)) {
            try {
                handleVerifyOTP(request, response);
            } catch (SQLException e) {
                throw new ServletException("Database error", e);
            }
            return;
        }

        if ("resetPassword".equals(action)) {
            try {
                handleResetPassword(request, response);
            } catch (SQLException e) {
                throw new ServletException("Database error", e);
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void handleSendOTP(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trim(request.getParameter("email"));

        if (email.isEmpty()) {
            request.setAttribute("error", "Email is required.");
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            // Check if email exists in database
            User user = userDAO.findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "No account found with this email address.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
                return;
            }

            String otp = OTPUtil.generateOTP();
            request.getSession().setAttribute("resetEmail", email);
            request.getSession().setAttribute("resetOTP", otp);
            request.getSession().setAttribute("otpExpiration", OTPUtil.getOTPExpiration());
            EmailService.sendOTP(email, otp);

            request.setAttribute("otpSent", true);
            request.setAttribute("email", email);
            request.setAttribute("success", "Reset code sent to " + email + ". Please enter it below.");
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
        } catch (SQLException | MessagingException e) {
            request.setAttribute("error", "Failed to send reset code. Please try again.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
        }
    }

    private void handleVerifyOTP(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String otp = trim(request.getParameter("otp"));
        String email = (String) request.getSession().getAttribute("resetEmail");
        String resetOTP = (String) request.getSession().getAttribute("resetOTP");
        java.sql.Timestamp otpExpiration = (java.sql.Timestamp) request.getSession().getAttribute("otpExpiration");

        if (email == null || otp.isEmpty()) {
            request.setAttribute("error", "Invalid request. Please start over.");
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!OTPUtil.validateOTP(otp, resetOTP, otpExpiration)) {
            request.setAttribute("error", "Invalid or expired reset code. Please try again.");
            request.setAttribute("otpSent", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        // OTP verified successfully
        request.getSession().setAttribute("otpVerified", true);
        request.setAttribute("otpVerified", true);
        request.setAttribute("email", email);
        request.setAttribute("success", "Code verified successfully. Please enter your new password.");
        request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));
        String email = (String) request.getSession().getAttribute("resetEmail");

        if (email == null) {
            request.setAttribute("error", "Invalid request. Please start over.");
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 4) {
            request.setAttribute("error", "Password must be at least 4 characters.");
            request.setAttribute("otpVerified", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.setAttribute("otpVerified", true);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(request, response);
            return;
        }

        // Update password
        User user = userDAO.findByEmail(email);
        if (user != null) {
            user.setPasswordHash(PasswordUtil.hash(newPassword));
            userDAO.update(user);
        }

        // Clear session data
        request.getSession().removeAttribute("resetEmail");
        request.getSession().removeAttribute("resetOTP");
        request.getSession().removeAttribute("otpExpiration");
        request.getSession().removeAttribute("otpVerified");

        request.setAttribute("success", "Password reset successfully. Please login with your new password.");
        response.sendRedirect(request.getContextPath() + "/login?passwordReset=1");
    }

    private String trim(String value) {
        return value != null ? value.trim() : "";
    }
}
