<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manager Sign Up - TaskManager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-page">
<div class="login-card login-card-wide">
    <div class="login-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <line x1="20" y1="8" x2="20" y2="14"></line>
            <line x1="23" y1="11" x2="17" y2="11"></line>
        </svg>
    </div>
    <div class="login-header">
        <h1>Manager Sign Up</h1>
        <p>Create your manager account</p>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/signup" class="login-form">
        <div class="form-group">
            <label for="fullName">Full Name *</label>
            <input type="text" id="fullName" name="fullName" required maxlength="100"
                   value="${fullName}" placeholder="Your full name">
        </div>
        <div class="form-group">
            <label for="username">Username *</label>
            <input type="text" id="username" name="username" required maxlength="50"
                   pattern="[a-zA-Z0-9._-]{3,50}" title="At least 3 characters: letters, numbers, . _ -"
                   value="${username}" placeholder="e.g. john.doe">
        </div>
        <div class="form-group">
            <label for="email">Email ID *</label>
            <input type="email" id="email" name="email" required maxlength="100"
                   value="${email}" placeholder="e.g. john.doe@example.com">
        </div>
        <div class="form-group">
            <label for="password">Password *</label>
            <input type="password" id="password" name="password" required minlength="4"
                   placeholder="At least 4 characters">
        </div>
        <div class="form-group">
            <label for="confirmPassword">Confirm Password *</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required minlength="4"
                   placeholder="Re-enter password">
        </div>
        <button type="submit" class="btn btn-primary btn-block">Create Manager Account</button>
    </form>

    <p class="auth-footer">
        Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a>
    </p>
    <p class="auth-note">Managers can create employee accounts from their dashboard after signing in.</p>
</div>
</body>
</html>

