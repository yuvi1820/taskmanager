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

@WebServlet(name = "EmployeeTaskServlet", urlPatterns = { "/employee/tasks" })
public class EmployeeTaskServlet extends HttpServlet {
    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(request);
        if (employee == null || !employee.isEmployee()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String status = param(request, "status");
            request.setAttribute("tasks", taskDAO.findForEmployee(employee.getId(), status.isEmpty() ? null : status));
            request.setAttribute("statusFilter", status);
            setCounts(request, taskDAO.countForEmployee(employee.getId()));
            request.setAttribute("user", employee);

            request.getRequestDispatcher("/WEB-INF/jsp/employee/tasks.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(request);
        if (employee == null || !employee.isEmployee()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = param(request, "action");

        try {
            if ("complete".equals(action)) {
                int id = parseId(request.getParameter("id"));
                Task task = taskDAO.findByIdForEmployee(id, employee.getId());
                if (task != null) {
                    taskDAO.updateStatus(id, "done");
                }
            } else if ("start".equals(action)) {
                int id = parseId(request.getParameter("id"));
                Task task = taskDAO.findByIdForEmployee(id, employee.getId());
                if (task != null && "assigned".equals(task.getStatus())) {
                    taskDAO.updateStatus(id, "in_progress");
                }
            } else if ("updateRemarks".equals(action)) {
                int id = parseId(request.getParameter("id"));
                String remarks = param(request, "remarks");
                Task task = taskDAO.findByIdForEmployee(id, employee.getId());
                if (task != null) {
                    task.setRemarks(remarks);
                    taskDAO.update(task);
                }
            } else if ("updateStatus".equals(action)) {
                String newStatus = param(request, "status");
                if (!newStatus.isEmpty()) {
                    employee.setStatus(newStatus);
                    userDAO.update(employee);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        String status = param(request, "redirectStatus");
        String url = request.getContextPath() + "/employee/tasks";
        if (!status.isEmpty()) {
            url += "?status=" + status;
        }
        response.sendRedirect(url);
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
}
