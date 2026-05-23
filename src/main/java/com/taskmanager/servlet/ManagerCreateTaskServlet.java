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

@WebServlet(name = "ManagerCreateTaskServlet", urlPatterns = { "/manager/create-task" })
public class ManagerCreateTaskServlet extends HttpServlet {
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
            String preselectedEmployeeId = request.getParameter("preselectedEmployeeId");
            if (preselectedEmployeeId != null && !preselectedEmployeeId.isEmpty()) {
                request.setAttribute("preselectedEmployeeId", preselectedEmployeeId);
            }
            request.setAttribute("employees", userDAO.findEmployees());
            request.setAttribute("user", manager);
            request.getRequestDispatcher("/WEB-INF/jsp/manager/create-task.jsp").forward(request, response);
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

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String[] assignedToIds = request.getParameterValues("assignedTo");
        String priority = normalizePriority(request.getParameter("priority"));
        String dueDate = request.getParameter("dueDate");

        String error = validateTask(title, assignedToIds);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.setAttribute("priority", priority);
            request.setAttribute("dueDate", dueDate);
            try {
                request.setAttribute("employees", userDAO.findEmployees());
                request.setAttribute("user", manager);
                request.getRequestDispatcher("/WEB-INF/jsp/manager/create-task.jsp").forward(request, response);
            } catch (SQLException e) {
                throw new ServletException("Database error", e);
            }
            return;
        }

        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(priority);
            task.setDueDate(dueDate != null && !dueDate.isEmpty() ? dueDate : null);
            task.setStatus("assigned");
            task.setCreatedBy(manager.getId());
            
            if (assignedToIds != null && assignedToIds.length > 0) {
                for (String idStr : assignedToIds) {
                    task.addAssigneeId(Integer.parseInt(idStr));
                }
            }
            
            taskDAO.insert(task);

            response.sendRedirect(request.getContextPath() + "/manager/tasks?taskCreated=1");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private String validateTask(String title, String[] assignedToIds) {
        if (title.isEmpty()) {
            return "Task title is required.";
        }
        if (assignedToIds == null || assignedToIds.length == 0) {
            return "Please select at least one employee to assign the task to.";
        }
        return null;
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private int parseId(String idParam) {
        try {
            return Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isEmpty()) {
            return "medium";
        }
        return priority.toLowerCase();
    }
}
