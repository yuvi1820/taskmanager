package com.taskmanager.dao;

import com.taskmanager.model.Task;
import com.taskmanager.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    private static final String SELECT_BASE = """
        SELECT t.*, m.full_name AS manager_name
        FROM tasks t
        JOIN users m ON t.created_by = m.id
        """;

    public List<Task> findForManager(int managerId, String search, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE t.created_by = ?");
        List<Object> params = new ArrayList<>();
        params.add(managerId);
        appendFilters(sql, params, search, statusFilter);
        sql.append(" ORDER BY t.due_date ASC, t.created_at DESC");
        List<Task> tasks = queryList(sql.toString(), params);
        for (Task task : tasks) {
            loadAssignees(task);
        }
        return tasks;
    }

    public List<Task> findOverdueTasks(int managerId) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.created_by = ? AND t.status != 'done' AND t.due_date < CURDATE() ORDER BY t.due_date ASC";
        List<Task> tasks = queryList(sql, List.of(managerId));
        for (Task task : tasks) {
            loadAssignees(task);
        }
        return tasks;
    }

    public List<Task> findForEmployee(int employeeId, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, m.full_name AS manager_name
            FROM tasks t
            JOIN task_assignments ta ON t.id = ta.task_id
            JOIN users m ON t.created_by = m.id
            WHERE ta.user_id = ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(employeeId);
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql.append(" AND t.status = ?");
            params.add(statusFilter);
        }
        sql.append("""
             ORDER BY CASE t.status
               WHEN 'assigned' THEN 0
               WHEN 'in_progress' THEN 1
               ELSE 2 END,
             t.due_date IS NULL, t.due_date ASC, t.id DESC
            """);
        List<Task> tasks = queryList(sql.toString(), params);
        for (Task task : tasks) {
            loadAssignees(task);
        }
        return tasks;
    }

    public Task findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.id = ?";
        List<Object> params = List.of(id);
        List<Task> tasks = queryList(sql.toString(), params);
        if (!tasks.isEmpty()) {
            Task task = tasks.get(0);
            loadAssignees(task);
            return task;
        }
        return null;
    }

    public Task findByIdForManager(int id, int managerId) throws SQLException {
        Task task = findById(id);
        if (task != null && task.getCreatedBy() == managerId) {
            return task;
        }
        return null;
    }

    public Task findByIdForEmployee(int id, int employeeId) throws SQLException {
        Task task = findById(id);
        if (task != null && task.getAssigneeIds().contains(employeeId)) {
            return task;
        }
        return null;
    }

    public void insert(Task task) throws SQLException {
        String sql = """
            INSERT INTO tasks (title, description, status, priority, due_date, remarks, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int taskId;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, task.getTitle().trim());
                    ps.setString(2, task.getDescription() != null ? task.getDescription().trim() : "");
                    ps.setString(3, task.getStatus());
                    ps.setString(4, task.getPriority());
                    setDateOrNull(ps, 5, task.getDueDate());
                    ps.setString(6, task.getRemarks() != null ? task.getRemarks().trim() : "");
                    ps.setInt(7, task.getCreatedBy());
                    ps.executeUpdate();
                    
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        taskId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated task ID");
                    }
                }
                
                if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
                    insertAssignments(conn, taskId, task.getAssigneeIds());
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean update(Task task) throws SQLException {
        String sql = """
            UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?,
            due_date = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, task.getTitle().trim());
                    ps.setString(2, task.getDescription() != null ? task.getDescription().trim() : "");
                    ps.setString(3, task.getStatus());
                    ps.setString(4, task.getPriority());
                    setDateOrNull(ps, 5, task.getDueDate());
                    ps.setString(6, task.getRemarks() != null ? task.getRemarks().trim() : "");
                    ps.setInt(7, task.getId());
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                
                deleteAssignments(conn, task.getId());
                if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
                    insertAssignments(conn, task.getId(), task.getAssigneeIds());
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void insertAssignments(Connection conn, int taskId, List<Integer> assigneeIds) throws SQLException {
        String sql = "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer assigneeId : assigneeIds) {
                ps.setInt(1, taskId);
                ps.setInt(2, assigneeId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteAssignments(Connection conn, int taskId) throws SQLException {
        String sql = "DELETE FROM task_assignments WHERE task_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    private void loadAssignees(Task task) throws SQLException {
        String sql = "SELECT user_id FROM task_assignments WHERE task_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> assigneeIds = new ArrayList<>();
                while (rs.next()) {
                    assigneeIds.add(rs.getInt("user_id"));
                }
                task.setAssigneeIds(assigneeIds);
                
                if (!assigneeIds.isEmpty()) {
                    task.setAssigneeName(getAssigneeNames(assigneeIds));
                }
            }
        }
    }

    private String getAssigneeNames(List<Integer> assigneeIds) throws SQLException {
        if (assigneeIds.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder("SELECT full_name FROM users WHERE id IN (");
        for (int i = 0; i < assigneeIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < assigneeIds.size(); i++) {
                ps.setInt(i + 1, assigneeIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder names = new StringBuilder();
                while (rs.next()) {
                    if (names.length() > 0) names.append(", ");
                    names.append(rs.getString("full_name"));
                }
                return names.toString();
            }
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int managerId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                deleteAssignments(conn, id);
                String sql = "DELETE FROM tasks WHERE id = ? AND created_by = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setInt(2, managerId);
                    int rows = ps.executeUpdate();
                    conn.commit();
                    return rows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int[] countForManager(int managerId) throws SQLException {
        int assigned = 0, inProgress = 0, done = 0;
        String sql = "SELECT status, COUNT(*) AS cnt FROM tasks WHERE created_by = ? GROUP BY status";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    switch (rs.getString("status")) {
                        case "assigned" -> assigned = rs.getInt("cnt");
                        case "in_progress" -> inProgress = rs.getInt("cnt");
                        case "done" -> done = rs.getInt("cnt");
                        default -> { }
                    }
                }
            }
        }
        return new int[] { assigned, inProgress, done, assigned + inProgress + done };
    }

    public int[] countForEmployee(int employeeId) throws SQLException {
        int assigned = 0, inProgress = 0, done = 0, late = 0;
        String sql = """
            SELECT t.status, COUNT(*) AS cnt
            FROM tasks t
            JOIN task_assignments ta ON t.id = ta.task_id
            WHERE ta.user_id = ?
            GROUP BY t.status
            """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    switch (rs.getString("status")) {
                        case "assigned" -> assigned = rs.getInt("cnt");
                        case "in_progress" -> inProgress = rs.getInt("cnt");
                        case "done" -> done = rs.getInt("cnt");
                        default -> { }
                    }
                }
            }
        }

        // Count late tasks (tasks past due date that are not done)
        String lateSql = """
            SELECT COUNT(*)
            FROM tasks t
            JOIN task_assignments ta ON t.id = ta.task_id
            WHERE ta.user_id = ? AND t.status != 'done' AND t.due_date < CURDATE()
            """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(lateSql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    late = rs.getInt(1);
                }
            }
        }

        return new int[] { assigned, inProgress, done, assigned + inProgress + done, late };
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String search, String statusFilter) {
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(t.title) LIKE ? OR LOWER(t.description) LIKE ?)");
            String term = "%" + search.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql.append(" AND t.status = ?");
            params.add(statusFilter);
        }
    }

    private List<Task> queryList(String sql, List<Object> params) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        }
        return tasks;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setPriority(rs.getString("priority"));
        task.setRemarks(rs.getString("remarks"));
        Date due = rs.getDate("due_date");
        task.setDueDate(due != null ? due.toString() : null);
        task.setCreatedBy(rs.getInt("created_by"));
        task.setManagerName(rs.getString("manager_name"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setUpdatedAt(rs.getTimestamp("updated_at"));
        return task;
    }

    private void setDateOrNull(PreparedStatement ps, int index, String dueDate) throws SQLException {
        if (dueDate == null || dueDate.isBlank()) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(dueDate));
        }
    }
}
