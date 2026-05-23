<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee - TaskManager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="topbar topbar-employee">
    <div class="topbar-brand">
        <h1>TaskManager</h1>
        <span class="role-badge role-employee">Employee</span>
    </div>
    <div class="topbar-user">
        <span>Hello, <strong><c:out value="${user.fullName}"/></strong></span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
    </div>
</header>

<main class="container">
  <p class="page-desc">View tasks assigned to you by your manager.</p>

    <section class="card">
        <div class="card-head">
            <h2>My Status</h2>
            <span class="pill pill-${user.status == 'active' ? 'done' : user.status == 'busy' ? 'in_progress' : 'low'}"><c:out value="${user.status}"/></span>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/employee/tasks">
            <input type="hidden" name="action" value="updateStatus">
            <div class="form-group">
                <select name="status">
                    <option value="active" ${user.status == 'active' ? 'selected' : ''}>🟢 Active</option>
                    <option value="busy" ${user.status == 'busy' ? 'selected' : ''}>🟡 Busy</option>
                    <option value="on_leave" ${user.status == 'on_leave' ? 'selected' : ''}>🔵 On Leave</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary btn-sm">Update Status</button>
        </form>
    </section>

    <section class="stats">
        <a href="${pageContext.request.contextPath}/employee/tasks" class="stat ${empty statusFilter ? 'active' : ''}">
            <span class="stat-num">${countTotal}</span><span class="stat-label">All Tasks</span>
        </a>
        <a href="${pageContext.request.contextPath}/employee/tasks?status=assigned" class="stat ${statusFilter == 'assigned' ? 'active' : ''}">
            <span class="stat-num">${countAssigned}</span><span class="stat-label">Assigned</span>
        </a>
        <a href="${pageContext.request.contextPath}/employee/tasks?status=in_progress" class="stat ${statusFilter == 'in_progress' ? 'active' : ''}">
            <span class="stat-num">${countInProgress}</span><span class="stat-label">In Progress</span>
        </a>
        <a href="${pageContext.request.contextPath}/employee/tasks?status=done" class="stat ${statusFilter == 'done' ? 'active' : ''}">
            <span class="stat-num">${countDone}</span><span class="stat-label">Completed</span>
        </a>
    </section>

    <section class="card">
        <h2>My Assigned Tasks</h2>

        <c:if test="${empty tasks}">
            <p class="empty-msg">No tasks assigned to you right now.</p>
        </c:if>

        <div class="task-list">
            <c:forEach var="t" items="${tasks}">
                <article class="task-item ${t.status == 'done' ? 'done' : ''}">
                    <div class="task-item-head">
                        <h3><c:out value="${t.title}"/></h3>
                        <span class="pill pill-${t.status}">${t.status}</span>
                    </div>
                    <c:if test="${not empty t.description}">
                        <p class="task-desc"><c:out value="${t.description}"/></p>
                    </c:if>
                    <div class="task-meta">
                        <span>From: <c:out value="${t.managerName}"/></span>
                        <span>Priority: <c:out value="${t.priority}"/></span>
                        <c:if test="${not empty t.dueDate}">
                            <span>Due: <c:out value="${t.dueDate}"/></span>
                        </c:if>
                    </div>
                    <c:if test="${not empty t.remarks}">
                        <div class="task-remarks">
                            <small>Your Remarks:</small>
                            <p><c:out value="${t.remarks}"/></p>
                        </div>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/employee/tasks">
                        <input type="hidden" name="action" value="updateRemarks">
                        <input type="hidden" name="id" value="${t.id}">
                        <input type="hidden" name="redirectStatus" value="${statusFilter}">
                        <div class="form-group">
                            <textarea name="remarks" placeholder="Add or update remarks about your work..." rows="2"><c:out value="${t.remarks}"/></textarea>
                        </div>
                        <button type="submit" class="btn btn-outline btn-sm">Update Remarks</button>
                    </form>
                    <c:if test="${t.status != 'done'}">
                        <div class="task-actions">
                            <c:if test="${t.status == 'assigned'}">
                                <form method="post" action="${pageContext.request.contextPath}/employee/tasks">
                                    <input type="hidden" name="action" value="start">
                                    <input type="hidden" name="id" value="${t.id}">
                                    <input type="hidden" name="redirectStatus" value="${statusFilter}">
                                    <button type="submit" class="btn btn-outline btn-sm">Start Working</button>
                                </form>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/employee/tasks">
                                <input type="hidden" name="action" value="complete">
                                <input type="hidden" name="id" value="${t.id}">
                                <input type="hidden" name="redirectStatus" value="${statusFilter}">
                                <button type="submit" class="btn btn-primary btn-sm">Mark as Completed</button>
                            </form>
                        </div>
                    </c:if>
                    <c:if test="${t.status == 'done'}">
                        <p class="completed-label">Completed</p>
                    </c:if>
                </article>
            </c:forEach>
        </div>
    </section>
</main>
</body>
</html>
