<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Task - TaskManager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="topbar topbar-manager">
    <div class="topbar-brand">
        <h1>TaskManager</h1>
        <span class="role-badge role-manager">Manager</span>
    </div>
    <div class="topbar-user">
        <span>Hello, <strong><c:out value="${user.fullName}"/></strong></span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
    </div>
</header>

<main class="container">
    <div class="auth-box">
        <h1>Create & Assign Task</h1>
        <p>Create a new task and assign it to an employee</p>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success"><c:out value="${success}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/create-task" class="login-form">
        <div class="form-group">
            <label for="title">Task Title *</label>
            <input type="text" id="title" name="title" required maxlength="255"
                   value="${title}" placeholder="Enter task title">
        </div>
        <div class="form-group">
            <label for="description">Description</label>
            <textarea id="description" name="description" rows="3" maxlength="2000"
                      placeholder="Task details">${description}</textarea>
        </div>
        <div class="form-group">
            <label for="assignedTo">Assign To Employees *</label>
            <div style="max-height: 200px; overflow-y: auto; border: 1px solid var(--border); border-radius: 6px; padding: 0.75rem; background: #f8fafc;">
                <c:forEach var="emp" items="${employees}">
                    <div style="margin-bottom: 0.5rem;">
                        <label style="display: flex; align-items: center; cursor: pointer;">
                            <input type="checkbox" name="assignedTo" value="${emp.id}"
                                ${not empty preselectedEmployeeId && preselectedEmployeeId == emp.id ? 'checked' : ''}
                                style="margin-right: 0.5rem;">
                            <c:out value="${emp.fullName}"/> (@<c:out value="${emp.username}"/>)
                        </label>
                    </div>
                </c:forEach>
            </div>
            <small style="color: var(--muted);">Select one or more employees to assign this task to</small>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label for="priority">Priority</label>
                <select id="priority" name="priority">
                    <option value="low" ${priority == 'low' ? 'selected' : ''}>Low</option>
                    <option value="medium" ${empty priority || priority == 'medium' ? 'selected' : ''}>Medium</option>
                    <option value="high" ${priority == 'high' ? 'selected' : ''}>High</option>
                </select>
            </div>
            <div class="form-group">
                <label for="dueDate">Due Date</label>
                <input type="date" id="dueDate" name="dueDate" value="${dueDate}">
            </div>
        </div>
        <button type="submit" class="btn btn-primary btn-block">Create & Assign Task</button>
    </form>

    <p class="auth-footer">
        <a href="${pageContext.request.contextPath}/manager/tasks">Back to Dashboard</a>
    </p>
</div>
</body>
</html>
