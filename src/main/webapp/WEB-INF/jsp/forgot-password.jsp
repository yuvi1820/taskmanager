<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password - TaskManager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-page">
<div class="login-card">
    <div class="login-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
        </svg>
    </div>
    <div class="login-header">
        <h1>Reset Password</h1>
        <p>Enter your email to receive a reset code</p>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success"><c:out value="${success}"/></div>
    </c:if>

    <c:if test="${not otpSent and not otpVerified}">
        <form method="post" action="${pageContext.request.contextPath}/forgot-password" class="login-form">
            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" name="email" required maxlength="100"
                       value="${email}" placeholder="Enter your registered email">
            </div>
            <button type="submit" name="action" value="sendOTP" class="btn btn-primary btn-block">Send Reset Code</button>
        </form>
    </c:if>

    <c:if test="${otpSent and not otpVerified}">
        <form method="post" action="${pageContext.request.contextPath}/forgot-password" class="login-form">
            <div class="form-group">
                <label for="otp">Reset Code *</label>
                <input type="text" id="otp" name="otp" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="Enter 6-digit code">
                <small style="color: var(--muted);">Code sent to <c:out value="${email}"/></small>
            </div>
            <button type="submit" name="action" value="verifyOTP" class="btn btn-primary btn-block">Verify Code</button>
            <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline btn-block" style="margin-top: 0.5rem;">Cancel</a>
        </form>
    </c:if>

    <c:if test="${otpVerified}">
        <form method="post" action="${pageContext.request.contextPath}/forgot-password" class="login-form">
            <div class="form-group">
                <label for="newPassword">New Password *</label>
                <input type="password" id="newPassword" name="newPassword" required minlength="4"
                       placeholder="At least 4 characters">
            </div>
            <div class="form-group">
                <label for="confirmPassword">Confirm New Password *</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required minlength="4"
                       placeholder="Re-enter new password">
            </div>
            <button type="submit" name="action" value="resetPassword" class="btn btn-primary btn-block">Reset Password</button>
            <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline btn-block" style="margin-top: 0.5rem;">Cancel</a>
        </form>
    </c:if>

    <p class="auth-footer">
        <a href="${pageContext.request.contextPath}/login">Back to Login</a>
    </p>
</div>
</body>
</html>
