package com.taskmanager.servlet;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "ManagerTaskServlet", urlPatterns = { "/manager/tasks" })
public class ManagerTaskServlet extends HttpServlet {
    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(request);
        if (manager == null || !manager.isManager()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String action = request.getParameter("action");
            if ("edit".equals(action)) {
                int id = parseId(request.getParameter("id"));
                Task task = taskDAO.findByIdForManager(id, manager.getId());
                if (task != null) {
                    request.setAttribute("editTask", task);
                }
            } else if ("assignToEmployee".equals(action)) {
                int employeeId = parseId(request.getParameter("employeeId"));
                if (employeeId > 0) {
                    response.sendRedirect(request.getContextPath() + "/manager/create-task?preselectedEmployeeId=" + employeeId);
                    return;
                }
            } else if ("editEmployee".equals(action)) {
                int employeeId = parseId(request.getParameter("employeeId"));
                if (employeeId > 0) {
                    User employee = userDAO.findById(employeeId);
                    if (employee != null && employee.isEmployee()) {
                        request.setAttribute("editEmployee", employee);
                    }
                }
            } else if ("viewEmployee".equals(action)) {
                int employeeId = parseId(request.getParameter("employeeId"));
                if (employeeId > 0) {
                    User employee = userDAO.findById(employeeId);
                    if (employee != null && employee.isEmployee()) {
                        request.setAttribute("viewEmployee", employee);
                        request.setAttribute("employeeTasks", taskDAO.findForEmployee(employeeId, null));
                        request.setAttribute("employeeTaskCounts", taskDAO.countForEmployee(employeeId));
                    }
                }
            }

            String search = param(request, "search");
            String status = param(request, "status");
            String employeeSearch = param(request, "employeeSearch");

            List<Task> tasks = taskDAO.findForManager(manager.getId(), search, status.isEmpty() ? null : status);
            request.setAttribute("tasks", tasks);
            request.setAttribute("employees", userDAO.findEmployeesWithTaskCount(employeeSearch.isEmpty() ? null : employeeSearch));
            request.setAttribute("overdueTasks", taskDAO.findOverdueTasks(manager.getId()));
            request.setAttribute("search", search);
            request.setAttribute("employeeSearch", employeeSearch);
            request.setAttribute("statusFilter", status);
            setCounts(request, taskDAO.countForManager(manager.getId()));
            request.setAttribute("user", manager);
            request.setAttribute("employeeCount", userDAO.countEmployees());
            if ("1".equals(request.getParameter("employeeAdded"))) {
                request.setAttribute("success", "Employee account created.");
            }

            request.getRequestDispatcher("/WEB-INF/jsp/manager/tasks.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
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
        String action = param(request, "action");

        try {
            switch (action) {
                case "create" -> createTask(request, manager);
                case "update" -> updateTask(request, manager);
                case "delete" -> deleteTask(request, manager);
                case "updateEmployeeStatus" -> updateEmployeeStatus(request);
                case "deleteEmployee" -> deleteEmployee(request);
                case "updateEmployee" -> updateEmployee(request);
                default -> { }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        if (("create".equals(action) || "update".equals(action)) && request.getAttribute("taskError") != null) {
            request.setAttribute("editTask", buildTask(request));
            doGet(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/manager/tasks");
    }

    private void createTask(HttpServletRequest request, User manager) throws SQLException {
        Task task = buildTask(request);
        if (task.getTitle().isBlank() || task.getAssigneeIds().isEmpty()) {
            return;
        }
        if (task.getDueDate() != null && isPastDate(task.getDueDate())) {
            request.setAttribute("taskError", "Due date cannot be in the past.");
            return;
        }
        task.setCreatedBy(manager.getId());
        task.setStatus("assigned");
        taskDAO.insert(task);
    }

    private void updateTask(HttpServletRequest request, User manager) throws SQLException {
        int id = parseId(request.getParameter("id"));
        Task existing = taskDAO.findByIdForManager(id, manager.getId());
        if (existing == null) {
            return;
        }
        Task task = buildTask(request);
        task.setId(id);
        task.setCreatedBy(manager.getId());
        if (task.getDueDate() != null && isPastDate(task.getDueDate())) {
            request.setAttribute("taskError", "Due date cannot be in the past.");
            return;
        }
        if (!task.getTitle().isBlank() && !task.getAssigneeIds().isEmpty()) {
            taskDAO.update(task);
        }
    }

    private void deleteTask(HttpServletRequest request, User manager) throws SQLException {
        int id = parseId(request.getParameter("id"));
        taskDAO.delete(id, manager.getId());
    }

    private void updateEmployeeStatus(HttpServletRequest request) throws SQLException {
        int employeeId = parseId(request.getParameter("employeeId"));
        String status = param(request, "status");
        if (employeeId > 0 && !status.isEmpty()) {
            userDAO.updateStatus(employeeId, status);
        }
    }

    private void deleteEmployee(HttpServletRequest request) throws SQLException {
        int employeeId = parseId(request.getParameter("employeeId"));
        if (employeeId > 0) {
            userDAO.delete(employeeId);
        }
    }

    private void updateEmployee(HttpServletRequest request) throws SQLException {
        int employeeId = parseId(request.getParameter("employeeId"));
        String fullName = param(request, "fullName");
        String email = param(request, "email");
        String status = param(request, "status");

        if (employeeId > 0 && !fullName.isEmpty()) {
            User employee = userDAO.findById(employeeId);
            if (employee != null && employee.isEmployee()) {
                employee.setFullName(fullName);
                employee.setEmail(email);
                employee.setStatus(status);
                userDAO.update(employee);
            }
        }
    }

    private Task buildTask(HttpServletRequest request) {
        Task task = new Task();
        task.setTitle(param(request, "title"));
        task.setDescription(param(request, "description"));
        task.setPriority(normalizePriority(request.getParameter("priority")));
        String due = request.getParameter("dueDate");
        task.setDueDate(due != null && !due.isBlank() ? due : null);
        
        String[] assignedToIds = request.getParameterValues("assignedTo");
        if (assignedToIds != null && assignedToIds.length > 0) {
            for (String idStr : assignedToIds) {
                task.addAssigneeId(Integer.parseInt(idStr));
            }
        }
        
        task.setStatus("assigned");
        return task;
    }

    private boolean isPastDate(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDate due = java.time.LocalDate.parse(dueDate);
            return due.isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private void setCounts(HttpServletRequest request, int[] counts) {
        request.setAttribute("countAssigned", counts[0]);
        request.setAttribute("countInProgress", counts[1]);
        request.setAttribute("countDone", counts[2]);
        request.setAttribute("countTotal", counts[3]);
    }

    private String param(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v != null ? v.trim() : "";
    }

    private int parseId(String idParam) {
        try {
            return Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String normalizePriority(String priority) {
        if ("low".equals(priority) || "high".equals(priority)) {
            return priority;
        }
        return "medium";
    }
}
