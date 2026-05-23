<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Employee - TaskManager</title>
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
        <h1>Add Employee</h1>
        <p>Create a new employee account</p>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success"><c:out value="${success}"/></div>
    </c:if>

    <c:if test="${emailVerified}">
        <div class="alert alert-success">Email verified successfully! Please complete the employee details.</div>
    </c:if>

    <c:if test="${not emailVerified and not otpSent}">
        <form method="post" action="${pageContext.request.contextPath}/manager/add-employee" class="login-form">
            <div class="form-group">
                <label for="email">Email ID *</label>
                <div style="display: flex; gap: 0.5rem;">
                    <input type="email" id="email" name="email" required maxlength="100"
                           value="${email}" placeholder="e.g. john.doe@example.com" style="flex: 1;">
                    <button type="button" onclick="verifyEmail()" class="btn btn-outline btn-sm">Verify</button>
                </div>
                <div id="emailError" class="alert alert-error" style="display: none; margin-top: 0.5rem;"></div>
                <div id="emailSuccess" class="alert alert-success" style="display: none; margin-top: 0.5rem;"></div>
            </div>
            <button type="submit" name="action" value="sendOTP" class="btn btn-primary btn-block">Send OTP</button>
        </form>
    </c:if>

    <c:if test="${otpSent and not emailVerified}">
        <form method="post" action="${pageContext.request.contextPath}/manager/add-employee" class="login-form">
            <div class="form-group">
                <label for="otp">Enter OTP *</label>
                <input type="text" id="otp" name="otp" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="Enter 6-digit OTP">
                <small style="color: var(--muted);">OTP sent to <c:out value="${email}"/></small>
            </div>
            <button type="submit" name="action" value="verifyEmailOTP" class="btn btn-primary btn-block">Verify OTP</button>
            <a href="${pageContext.request.contextPath}/manager/add-employee" class="btn btn-outline btn-block" style="margin-top: 0.5rem;">Cancel</a>
        </form>
    </c:if>

    <c:if test="${emailVerified}">
        <form method="post" action="${pageContext.request.contextPath}/manager/add-employee" class="login-form">
            <input type="hidden" name="email" value="${email}">
            <div class="form-group">
                <label for="fullName">Full Name *</label>
                <input type="text" id="fullName" name="fullName" required maxlength="100"
                       value="${fullName}" placeholder="Employee full name">
            </div>
            <div class="form-group">
                <label for="username">Username *</label>
                <div style="display: flex; gap: 0.5rem;">
                    <input type="text" id="username" name="username" required maxlength="50"
                           pattern="[a-zA-Z0-9._-]{3,50}" title="At least 3 characters: letters, numbers, . _ -"
                           value="${username}" placeholder="e.g. john.doe" style="flex: 1;">
                    <button type="button" onclick="verifyUsername()" class="btn btn-outline btn-sm">Verify</button>
                </div>
                <div id="usernameError" class="alert alert-error" style="display: none; margin-top: 0.5rem;"></div>
                <div id="usernameSuccess" class="alert alert-success" style="display: none; margin-top: 0.5rem;"></div>
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
            <button type="submit" name="action" value="create" class="btn btn-primary btn-block">Create Employee Account</button>
        </form>
    </c:if>

    <p class="auth-footer">
        <a href="${pageContext.request.contextPath}/manager/tasks">Back to Dashboard</a>
    </p>
</div>

<script>
const usernameInput = document.getElementById('username');
const emailInput = document.getElementById('email');
const usernameError = document.getElementById('usernameError');
const usernameSuccess = document.getElementById('usernameSuccess');
const emailError = document.getElementById('emailError');
const emailSuccess = document.getElementById('emailSuccess');

function verifyUsername() {
    const username = usernameInput.value.toLowerCase();
    
    if (username.length < 3) {
        usernameError.textContent = 'Username must be at least 3 characters.';
        usernameError.style.display = 'block';
        usernameSuccess.style.display = 'none';
        return;
    }
    
    const url = '${pageContext.request.contextPath}/manager/add-employee?action=checkUsername&username=' + encodeURIComponent(username);
    
    console.log('Checking username:', username);
    console.log('Request URL:', url);
    
    fetch(url, { method: 'POST' })
        .then(response => {
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);
            return response.json();
        })
        .then(data => {
            console.log('Response data:', data);
            if (!data.available) {
                usernameError.textContent = 'Username already taken. Choose another.';
                usernameError.style.display = 'block';
                usernameSuccess.style.display = 'none';
            } else {
                usernameError.style.display = 'none';
                usernameSuccess.textContent = 'Username is available!';
                usernameSuccess.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error checking username:', error);
            console.error('Error details:', error.message);
            usernameError.textContent = 'Error checking username. Please try again.';
            usernameError.style.display = 'block';
            usernameSuccess.style.display = 'none';
        });
}

function verifyEmail() {
    const email = emailInput.value;
    
    if (!email || !email.includes('@')) {
        emailError.textContent = 'Please enter a valid email address.';
        emailError.style.display = 'block';
        emailSuccess.style.display = 'none';
        return;
    }
    
    const url = '${pageContext.request.contextPath}/manager/add-employee?action=checkEmail&email=' + encodeURIComponent(email);
    
    fetch(url, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            if (!data.available) {
                emailError.textContent = 'Email already registered. Please use a different email.';
                emailError.style.display = 'block';
                emailSuccess.style.display = 'none';
            } else {
                emailError.style.display = 'none';
                emailSuccess.textContent = 'Email is available! OTP will be sent for verification.';
                emailSuccess.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error checking email:', error);
            emailError.textContent = 'Error checking email. Please try again.';
            emailError.style.display = 'block';
            emailSuccess.style.display = 'none';
        });
}
</script>
</body>
</html>
