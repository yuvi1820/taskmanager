<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manager - TaskManager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<header class="topbar topbar-manager">
    <div class="topbar-brand">
        <h1>TaskManager</h1>
        <span class="role-badge role-manager">Manager</span>
    </div>
    <div class="topbar-user">
        <a href="${pageContext.request.contextPath}/manager/add-employee" class="btn btn-primary" style="font-weight: 600; padding: 0.5rem 1rem; font-size: 0.9rem;">+ Add Employee</a>
        <span>Hello, <strong><c:out value="${user.fullName}"/></strong></span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
    </div>
</header>

<main class="container">
    <c:if test="${not empty success}">
        <div class="alert alert-success page-alert"><c:out value="${success}"/></div>
    </c:if>
    <c:if test="${not empty employeeError}">
        <div class="alert alert-error page-alert"><c:out value="${employeeError}"/></div>
    </c:if>
    <c:if test="${not empty taskError}">
        <div class="alert alert-error page-alert"><c:out value="${taskError}"/></div>
    </c:if>

    <p class="employee-count-banner">${employeeCount} Employees</p>

    <section class="stats-cards" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
        <div class="stat-card" style="background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); padding: 1.25rem; border-radius: 12px; border: 1px solid #93c5fd;">
            <div style="font-size: 0.85rem; font-weight: 600; color: #1e40af; margin-bottom: 0.5rem;">Total Employees</div>
            <div style="font-size: 2rem; font-weight: 700; color: #1e3a8a;">${employeeCount}</div>
        </div>
        <div class="stat-card" style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); padding: 1.25rem; border-radius: 12px; border: 1px solid #fcd34d;">
            <div style="font-size: 0.85rem; font-weight: 600; color: #92400e; margin-bottom: 0.5rem;">Assigned Tasks</div>
            <div style="font-size: 2rem; font-weight: 700; color: #78350f;">${countAssigned}</div>
        </div>
        <div class="stat-card" style="background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%); padding: 1.25rem; border-radius: 12px; border: 1px solid #86efac;">
            <div style="font-size: 0.85rem; font-weight: 600; color: #166534; margin-bottom: 0.5rem;">Completed Tasks</div>
            <div style="font-size: 2rem; font-weight: 700; color: #14532d;">${countDone}</div>
        </div>
        <div class="stat-card" style="background: linear-gradient(135deg, #fef9c3 0%, #fef08a 100%); padding: 1.25rem; border-radius: 12px; border: 1px solid #fde047;">
            <div style="font-size: 0.85rem; font-weight: 600; color: #854d0e; margin-bottom: 0.5rem;">In Progress</div>
            <div style="font-size: 2rem; font-weight: 700; color: #713f12;">${countInProgress}</div>
        </div>
        <div class="stat-card" style="background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); padding: 1.25rem; border-radius: 12px; border: 1px solid #fca5a5;">
            <div style="font-size: 0.85rem; font-weight: 600; color: #991b1b; margin-bottom: 0.5rem;">Overdue Tasks</div>
            <div style="font-size: 2rem; font-weight: 700; color: #7f1d1d;">${not empty overdueTasks ? overdueTasks.size() : 0}</div>
        </div>
    </section>

    <section class="card" style="margin-bottom: 1.25rem;">
        <div class="card-head">
            <h2>Task Overview Chart</h2>
        </div>
        <div style="height: 300px; display: flex; align-items: center; justify-content: center;">
            <canvas id="taskChart" style="max-height: 300px; max-width: 100%;"></canvas>
        </div>
    </section>

    <c:if test="${not empty editEmployee}">
        <section class="card" style="margin-bottom: 1.25rem; background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);">
            <div class="card-head">
                <h2>Edit Employee</h2>
                <a href="${pageContext.request.contextPath}/manager/tasks" class="btn btn-outline btn-sm">Cancel</a>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/manager/tasks">
                <input type="hidden" name="action" value="updateEmployee">
                <input type="hidden" name="employeeId" value="${editEmployee.id}">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                    <div>
                        <label style="display: block; margin-bottom: 0.5rem; font-weight: 600;">Full Name *</label>
                        <input type="text" name="fullName" value="${editEmployee.fullName}" required style="width: 100%; padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px;">
                    </div>
                    <div>
                        <label style="display: block; margin-bottom: 0.5rem; font-weight: 600;">Username</label>
                        <input type="text" value="${editEmployee.username}" disabled style="width: 100%; padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px; background: var(--bg-light);">
                    </div>
                </div>
                <div style="margin-bottom: 1rem;">
                    <label style="display: block; margin-bottom: 0.5rem; font-weight: 600;">Email</label>
                    <input type="email" name="email" value="${not empty editEmployee.email ? editEmployee.email : ''}" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px;">
                </div>
                <div style="margin-bottom: 1rem;">
                    <label style="display: block; margin-bottom: 0.5rem; font-weight: 600;">Status</label>
                    <select name="status" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px;">
                        <option value="active" ${editEmployee.status == 'active' || empty editEmployee.status ? 'selected' : ''}>Active</option>
                        <option value="busy" ${editEmployee.status == 'busy' ? 'selected' : ''}>Busy</option>
                        <option value="on_leave" ${editEmployee.status == 'on_leave' ? 'selected' : ''}>On Leave</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Update Employee</button>
            </form>
        </section>
    </c:if>

    <c:if test="${not empty viewEmployee}">
        <section class="card" style="margin-bottom: 1.25rem; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);">
            <div class="card-head">
                <h2>Employee Details</h2>
                <a href="${pageContext.request.contextPath}/manager/tasks" class="btn btn-outline btn-sm">Close</a>
            </div>
            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem;">
                <div>
                    <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 1rem;">${viewEmployee.fullName}</h3>
                    <p style="margin-bottom: 0.5rem;"><strong>Username:</strong> @${viewEmployee.username}</p>
                    <p style="margin-bottom: 0.5rem;"><strong>Email:</strong> ${not empty viewEmployee.email ? viewEmployee.email : '—'}</p>
                    <p style="margin-bottom: 0.5rem;"><strong>Role:</strong> ${viewEmployee.role}</p>
                    <div style="margin-top: 1rem;">
                        <form method="post" action="${pageContext.request.contextPath}/manager/tasks">
                            <input type="hidden" name="action" value="updateEmployeeStatus">
                            <input type="hidden" name="employeeId" value="${viewEmployee.id}">
                            <div style="display: flex; gap: 0.5rem; align-items: center;">
                                <label style="font-size: 0.8rem; font-weight: 600; color: var(--muted);">Status:</label>
                                <select name="status" style="padding: 0.4rem 0.6rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.85rem;">
                                    <option value="active" ${viewEmployee.status == 'active' || empty viewEmployee.status ? 'selected' : ''}>Active</option>
                                    <option value="busy" ${viewEmployee.status == 'busy' ? 'selected' : ''}>Busy</option>
                                    <option value="on_leave" ${viewEmployee.status == 'on_leave' ? 'selected' : ''}>On Leave</option>
                                </select>
                                <button type="submit" class="btn btn-outline btn-sm">Update</button>
                            </div>
                        </form>
                    </div>
                </div>
                <div>
                    <h4 style="font-size: 1rem; font-weight: 600; margin-bottom: 0.75rem;">Task Statistics</h4>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                        <div style="background: #fff; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border);">
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--manager);">${employeeTaskCounts[0]}</div>
                            <div style="font-size: 0.8rem; color: var(--muted);">Assigned</div>
                        </div>
                        <div style="background: #fff; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border);">
                            <div style="font-size: 1.5rem; font-weight: 700; color: #d97706;">${employeeTaskCounts[1]}</div>
                            <div style="font-size: 0.8rem; color: var(--muted);">In Progress</div>
                        </div>
                        <div style="background: #fff; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border);">
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--employee);">${employeeTaskCounts[2]}</div>
                            <div style="font-size: 0.8rem; color: var(--muted);">Completed</div>
                        </div>
                        <div style="background: #fff; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border);">
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--text);">${employeeTaskCounts[3]}</div>
                            <div style="font-size: 0.8rem; color: var(--muted);">Total</div>
                        </div>
                    </div>
                    <c:if test="${employeeTaskCounts[3] > 0}">
                        <div style="margin-top: 1rem; background: #fff; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border);">
                            <div style="font-size: 0.9rem; font-weight: 600; margin-bottom: 0.5rem;">Completion Rate</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--employee);">
                                <c:set var="completionRate" value="${employeeTaskCounts[2] * 100 / employeeTaskCounts[3]}"/>
                                ${completionRate}%
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${employeeTaskCounts[4] > 0}">
                        <div style="margin-top: 0.75rem; background: #fef2f2; padding: 0.75rem; border-radius: 8px; border: 1px solid #fecaca;">
                            <div style="font-size: 0.9rem; font-weight: 600; margin-bottom: 0.5rem; color: #b91c1c;">Late Tasks</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: #b91c1c;">${employeeTaskCounts[4]}</div>
                        </div>
                    </c:if>
                </div>
            </div>
        </section>
    </c:if>

    <section class="card" style="margin-bottom: 1.25rem;">
        <div class="card-head">
            <h2>Employees</h2>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
                <a href="${pageContext.request.contextPath}/manager/create-task" class="btn btn-primary" style="font-weight: 600; padding: 0.5rem 1rem; font-size: 0.9rem;">+ Create Task</a>
                <form class="search-bar" method="get" action="${pageContext.request.contextPath}/manager/tasks" style="margin: 0;">
                    <input type="text" name="employeeSearch" placeholder="Search employees..." value="${employeeSearch}">
                    <button type="submit" class="btn btn-outline">Search</button>
                </form>
            </div>
        </div>
        <c:if test="${empty employees}">
            <p class="empty-msg">No employees registered yet. Add employees below or have them sign up.</p>
        </c:if>
        <div class="task-table-wrap">
            <table class="task-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Tasks Assigned</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="emp" items="${employees}">
                        <tr>
                            <td><strong><c:out value="${emp.fullName}"/></strong></td>
                            <td>@<c:out value="${emp.username}"/></td>
                            <td><c:out value="${not empty emp.email ? emp.email : '—'}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty emp.status || emp.status == 'active'}">
                                        <span class="pill pill-done">Active</span>
                                    </c:when>
                                    <c:when test="${emp.status == 'busy'}">
                                        <span class="pill pill-in_progress">Busy</span>
                                    </c:when>
                                    <c:when test="${emp.status == 'on_leave'}">
                                        <span class="pill pill-low">On Leave</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="pill pill-low"><c:out value="${emp.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${emp.taskCount != null ? emp.taskCount : 0}"/></td>
                            <td class="actions">
                                <a href="${pageContext.request.contextPath}/manager/tasks?action=assignToEmployee&employeeId=${emp.id}" class="btn btn-primary btn-sm">Assign Task</a>
                                <a href="${pageContext.request.contextPath}/manager/tasks?action=editEmployee&employeeId=${emp.id}" class="btn btn-outline btn-sm">Edit</a>
                                <a href="${pageContext.request.contextPath}/manager/tasks?action=viewEmployee&employeeId=${emp.id}" class="btn btn-outline btn-sm">View</a>
                                <form method="post" action="${pageContext.request.contextPath}/manager/tasks" style="display:inline" onsubmit="return confirm('Delete this employee?');">
                                    <input type="hidden" name="action" value="deleteEmployee">
                                    <input type="hidden" name="employeeId" value="${emp.id}">
                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <c:if test="${not empty overdueTasks}">
        <section class="card" style="margin-bottom: 1.25rem; background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%); border: 1px solid #fecaca;">
            <div class="card-head">
                <h2 style="color: #b91c1c;">⚠️ Overdue Tasks</h2>
                <span style="font-size: 0.85rem; color: #b91c1c; font-weight: 600;">${overdueTasks.size()} overdue</span>
            </div>
            <div class="task-table-wrap">
                <table class="task-table">
                    <thead>
                        <tr>
                            <th>Task</th>
                            <th>Assigned To</th>
                            <th>Due Date</th>
                            <th>Days Overdue</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${overdueTasks}">
                            <tr>
                                <td>
                                    <strong><c:out value="${t.title}"/></strong>
                                    <c:if test="${not empty t.description}">
                                        <br><small><c:out value="${t.description}"/></small>
                                    </c:if>
                                </td>
                                <td><c:out value="${t.assigneeName}"/></td>
                                <td style="color: #b91c1c; font-weight: 600;"><c:out value="${t.dueDate}"/></td>
                                <td style="color: #b91c1c; font-weight: 600;">Overdue</td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/manager/tasks?action=edit&id=${t.id}" class="btn btn-outline btn-sm">Edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </c:if>

    <section class="stats">
        <a href="${pageContext.request.contextPath}/manager/tasks" class="stat ${empty statusFilter ? 'active' : ''}">
            <span class="stat-num">${countTotal}</span><span class="stat-label">All</span>
        </a>
        <a href="${pageContext.request.contextPath}/manager/tasks?status=assigned" class="stat ${statusFilter == 'assigned' ? 'active' : ''}">
            <span class="stat-num">${countAssigned}</span><span class="stat-label">Assigned</span>
        </a>
        <a href="${pageContext.request.contextPath}/manager/tasks?status=in_progress" class="stat ${statusFilter == 'in_progress' ? 'active' : ''}">
            <span class="stat-num">${countInProgress}</span><span class="stat-label">In Progress</span>
        </a>
        <a href="${pageContext.request.contextPath}/manager/tasks?status=done" class="stat ${statusFilter == 'done' ? 'active' : ''}">
            <span class="stat-num">${countDone}</span><span class="stat-label">Completed</span>
        </a>
    </section>

    <c:if test="${not empty editTask}">
        <div class="layout">
            <aside class="card">
                <h2>Edit Task</h2>
                <form method="post" action="${pageContext.request.contextPath}/manager/tasks">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" value="${editTask.id}">

                    <div class="form-group">
                        <label for="title">Task Title *</label>
                        <input type="text" id="title" name="title" required maxlength="255"
                               value="${editTask.title}" placeholder="Enter task title">
                    </div>
                    <div class="form-group">
                        <label for="description">Description</label>
                        <textarea id="description" name="description" rows="3" maxlength="2000"
                                  placeholder="Task details">${editTask.description}</textarea>
                    </div>
                    <div class="form-group">
                        <label for="remarks">Remarks</label>
                        <textarea id="remarks" name="remarks" rows="3" maxlength="2000"
                                  placeholder="Employee remarks">${editTask.remarks}</textarea>
                    </div>
                    <div class="form-group">
                        <label for="assignedTo">Assign To Employees *</label>
                        <div style="max-height: 200px; overflow-y: auto; border: 1px solid var(--border); border-radius: 6px; padding: 0.75rem; background: #f8fafc;">
                            <c:forEach var="emp" items="${employees}">
                                <div style="margin-bottom: 0.5rem;">
                                    <label style="display: flex; align-items: center; cursor: pointer;">
                                        <input type="checkbox" name="assignedTo" value="${emp.id}"
                                            ${not empty editTask.assigneeIds && editTask.assigneeIds.contains(emp.id) ? 'checked' : ''}
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
                                <option value="low" ${editTask.priority == 'low' ? 'selected' : ''}>Low</option>
                                <option value="medium" ${editTask.priority == 'medium' ? 'selected' : ''}>Medium</option>
                                <option value="high" ${editTask.priority == 'high' ? 'selected' : ''}>High</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="dueDate">Due Date</label>
                            <input type="date" id="dueDate" name="dueDate"
                                   value="${editTask.dueDate != null ? editTask.dueDate : ''}">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Update Task</button>
                    <a href="${pageContext.request.contextPath}/manager/tasks" class="link-cancel">Cancel edit</a>
                </form>
            </aside>

            <section class="card">
            <div class="card-head">
                <h2>All Tasks</h2>
                <form class="search-bar" method="get" action="${pageContext.request.contextPath}/manager/tasks">
                    <c:if test="${not empty statusFilter}">
                        <input type="hidden" name="status" value="${statusFilter}">
                    </c:if>
                    <input type="text" name="search" placeholder="Search tasks..." value="${search}">
                    <button type="submit" class="btn btn-outline">Search</button>
                </form>
            </div>

            <c:if test="${empty tasks}">
                <p class="empty-msg">No tasks yet. Create and assign a task to an employee.</p>
            </c:if>

            <div class="task-table-wrap">
                <table class="task-table">
                    <thead>
                        <tr>
                            <th>Task</th>
                            <th>Assigned To</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <th>Due</th>
                            <th>Remarks</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${tasks}">
                            <tr>
                                <td>
                                    <strong><c:out value="${t.title}"/></strong>
                                    <c:if test="${not empty t.description}">
                                        <br><small><c:out value="${t.description}"/></small>
                                    </c:if>
                                </td>
                                <td><c:out value="${t.assigneeName}"/></td>
                                <td><span class="pill pill-${t.priority}"><c:out value="${t.priority}"/></span></td>
                                <td><span class="pill pill-${t.status}">${t.status}</span></td>
                                <td><c:out value="${not empty t.dueDate ? t.dueDate : '—'}"/></td>
                                <td>
                                    <c:if test="${not empty t.remarks}">
                                        <small style="color: var(--muted); font-style: italic;"><c:out value="${t.remarks}"/></small>
                                    </c:if>
                                    <c:if test="${empty t.remarks}">
                                        <small style="color: var(--muted);">—</small>
                                    </c:if>
                                </td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/manager/tasks?action=edit&id=${t.id}" class="btn btn-outline btn-sm">Edit</a>
                                    <form method="post" action="${pageContext.request.contextPath}/manager/tasks" style="display:inline"
                                          onsubmit="return confirm('Delete this task?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.id}">
                                        <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
    </c:if>

    <c:if test="${empty editTask}">
        <section class="card">
            <div class="card-head">
                <h2>All Tasks</h2>
                <form class="search-bar" method="get" action="${pageContext.request.contextPath}/manager/tasks">
                    <c:if test="${not empty statusFilter}">
                        <input type="hidden" name="status" value="${statusFilter}">
                    </c:if>
                    <input type="text" name="search" placeholder="Search tasks..." value="${search}">
                    <button type="submit" class="btn btn-outline">Search</button>
                </form>
            </div>
            <c:if test="${empty tasks}">
                <p class="empty-msg">No tasks found.</p>
            </c:if>
            <div class="task-table-wrap">
                <table class="task-table">
                    <thead>
                        <tr>
                            <th>Task</th>
                            <th>Assigned To</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <th>Remarks</th>
                            <th>Due Date</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${tasks}">
                            <tr>
                                <td>
                                    <strong><c:out value="${t.title}"/></strong>
                                    <c:if test="${not empty t.description}">
                                        <br><small><c:out value="${t.description}"/></small>
                                    </c:if>
                                </td>
                                <td><c:out value="${t.assigneeName}"/></td>
                                <td><span class="pill pill-${t.priority}">${t.priority}</span></td>
                                <td><span class="pill pill-${t.status}">${t.status}</span></td>
                                <td><c:out value="${not empty t.remarks ? t.remarks : '—'}"/></td>
                                <td><c:out value="${not empty t.dueDate ? t.dueDate : '—'}"/></td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/manager/tasks?action=edit&id=${t.id}" class="btn btn-outline btn-sm">Edit</a>
                                    <form method="post" action="${pageContext.request.contextPath}/manager/tasks" style="display:inline" onsubmit="return confirm('Delete this task?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.id}">
                                        <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </c:if>
</main>
<script>
    const ctx = document.getElementById('taskChart');
    if (ctx) {
        const countAssigned = ${countAssigned};
        const countInProgress = ${countInProgress};
        const countDone = ${countDone};
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Assigned', 'In Progress', 'Completed'],
                datasets: [{
                    data: [countAssigned, countInProgress, countDone],
                    backgroundColor: ['#3b82f6', '#f59e0b', '#22c55e'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            font: {
                                size: 14
                            }
                        }
                    }
                }
            }
        });
    }
</script>
</body>
</html>
